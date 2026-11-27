package com.nubb.classroom.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * Persists uploaded lesson MP3 audio files and images directly into the database (stored_media table)
 * alongside a fast local disk cache (./uploads/{category}/{filename}).
 *
 * On ephemeral cloud platforms like Render, container redeploys wipe ./uploads/; whenever a requested
 * file is missing from disk, this service transparently streams it back from PostgreSQL/H2 to disk
 * using a 64KB buffer (safe for 512MB RAM containers) so uploaded MP3 lessons NEVER disappear again.
 */
@Service
public class StoredMediaService {

    private final JdbcTemplate jdbcTemplate;

    public StoredMediaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String saveMedia(String category, MultipartFile file, Long accountId, String filenamePrefix) throws IOException {
        String safeCategory = sanitizeCategory(category);
        if (safeCategory == null) {
            throw new IllegalArgumentException("Unsupported media category: " + category);
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        String defaultName = "audio".equals(safeCategory) ? "audio.mp3" : "image.jpg";
        String orig = (file.getOriginalFilename() != null && !file.getOriginalFilename().trim().isEmpty())
                ? file.getOriginalFilename().trim()
                : defaultName;
        String cleanName = orig.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleanName.length() > 140) {
            cleanName = cleanName.substring(cleanName.length() - 140);
        }

        String prefix = (filenamePrefix != null && !filenamePrefix.isEmpty())
                ? filenamePrefix.replaceAll("[^a-zA-Z0-9_-]", "_") + "_"
                : "";
        String filename = prefix + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + cleanName;
        String storageKey = safeCategory + "/" + filename;

        // 1. Write to local disk cache via streaming (avoids allocating giant byte[] in heap)
        File dir = new File("./uploads/" + safeCategory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path diskPath = dir.toPath().resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, diskPath, StandardCopyOption.REPLACE_EXISTING);
        }
        File cachedFile = diskPath.toFile();
        long fileSize = cachedFile.length();
        String contentType = resolveContentType(safeCategory, filename, file.getContentType());

        // 2. Persist binary bytes into database (stored_media) using JDBC binary streaming
        persistFileToDatabase(storageKey, safeCategory, orig, contentType, fileSize, accountId, cachedFile);

        return "/uploads/" + storageKey;
    }

    /**
     * If an existing /uploads/{category}/{filename} file sits on local disk and is not yet in
     * stored_media (e.g., uploaded before DB persistence was added), persist it into DB safely.
     */
    public void ensureUrlPersistedToDatabase(String urlPath, Long accountId) {
        if (urlPath == null || !urlPath.startsWith("/uploads/")) return;
        String rel = urlPath.substring("/uploads/".length());
        int slash = rel.indexOf('/');
        if (slash <= 0 || slash >= rel.length() - 1) return;
        String cat = sanitizeCategory(rel.substring(0, slash));
        String fname = rel.substring(slash + 1);
        if (cat == null || !isSafeFilename(fname)) return;

        // Skip legacy static audio bundled inside JAR classpath (always present in repo/image)
        ClassPathResource cp = new ClassPathResource("static/uploads/" + cat + "/" + fname);
        if (cp.exists()) return;

        String storageKey = cat + "/" + fname;
        try {
            Integer existing = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM stored_media WHERE storage_key = ?",
                    Integer.class,
                    storageKey
            );
            if (existing != null && existing > 0) return;

            File local = new File("./uploads/" + cat, fname);
            if (!local.isFile() || local.length() == 0) {
                local = new File("/app/uploads/" + cat, fname);
            }
            if (local.isFile() && local.length() > 0) {
                persistFileToDatabase(storageKey, cat, fname, resolveContentType(cat, fname, null), local.length(), accountId, local);
            }
        } catch (Exception ignored) {}
    }

    public void persistFileToDatabase(
            String storageKey,
            String category,
            String originalFilename,
            String contentType,
            long fileSize,
            Long accountId,
            File sourceFile) throws IOException {

        Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stored_media WHERE storage_key = ?",
                Integer.class,
                storageKey
        );

        if (existing != null && existing > 0) {
            try (InputStream fis = new FileInputStream(sourceFile)) {
                jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "UPDATE stored_media SET content_type = ?, file_size = ?, file_data = ? WHERE storage_key = ?"
                    );
                    ps.setString(1, contentType);
                    ps.setLong(2, fileSize);
                    ps.setBinaryStream(3, fis, fileSize);
                    ps.setString(4, storageKey);
                    return ps;
                });
            }
        } else {
            try (InputStream fis = new FileInputStream(sourceFile)) {
                jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO stored_media (storage_key, category, original_filename, content_type, file_size, uploaded_by_account_id, file_data, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                    );
                    ps.setString(1, storageKey);
                    ps.setString(2, category);
                    ps.setString(3, originalFilename != null && originalFilename.length() > 255 ? originalFilename.substring(0, 255) : originalFilename);
                    ps.setString(4, contentType);
                    ps.setLong(5, fileSize);
                    if (accountId != null) {
                        ps.setLong(6, accountId);
                    } else {
                        ps.setNull(6, java.sql.Types.BIGINT);
                    }
                    ps.setBinaryStream(7, fis, fileSize);
                    ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                    return ps;
                });
            }
        }
    }

    /**
     * Resolves a media file for serving:
     * 1) Local disk cache (./uploads or /app/uploads)
     * 2) Persistent database restore (stored_media -> local disk cache via 64KB streaming)
     * 3) Bundled classpath static fallback (classpath:/static/uploads/)
     */
    public ResolvedMedia resolveMedia(String category, String filename) {
        String safeCategory = sanitizeCategory(category);
        if (safeCategory == null || !isSafeFilename(filename)) {
            return null;
        }

        // 1. Check fast local disk locations first
        File localPrimary = new File("./uploads/" + safeCategory, filename);
        if (localPrimary.isFile() && localPrimary.length() > 0) {
            return new ResolvedMedia(
                    new FileSystemResource(localPrimary),
                    resolveContentType(safeCategory, filename, null),
                    localPrimary.length()
            );
        }

        File appUpload = new File("/app/uploads/" + safeCategory, filename);
        if (appUpload.isFile() && appUpload.length() > 0) {
            return new ResolvedMedia(
                    new FileSystemResource(appUpload),
                    resolveContentType(safeCategory, filename, null),
                    appUpload.length()
            );
        }

        // 2. Restore transparently from persistent database (survives Render container redeploys!)
        String storageKey = safeCategory + "/" + filename;
        ResolvedMedia restored = restoreFromDatabaseToDisk(storageKey, safeCategory, filename);
        if (restored != null) {
            return restored;
        }

        // 3. Fallback to legacy static files bundled inside JAR classpath
        ClassPathResource cp = new ClassPathResource("static/uploads/" + storageKey);
        if (cp.exists() && cp.isReadable()) {
            try {
                return new ResolvedMedia(cp, resolveContentType(safeCategory, filename, null), cp.contentLength());
            } catch (IOException ignored) {}
        }

        return null;
    }

    private ResolvedMedia restoreFromDatabaseToDisk(String storageKey, String category, String filename) {
        synchronized (("CMS_MEDIA_RESTORE_" + storageKey).intern()) {
            File target = new File("./uploads/" + category, filename);
            if (target.isFile() && target.length() > 0) {
                return new ResolvedMedia(
                        new FileSystemResource(target),
                        resolveContentType(category, filename, null),
                        target.length()
                );
            }

            try {
                String dbContentType = jdbcTemplate.query(
                        "SELECT content_type, file_data FROM stored_media WHERE storage_key = ?",
                        ps -> ps.setString(1, storageKey),
                        rs -> {
                            if (!rs.next()) return null;
                            String ct = rs.getString("content_type");
                            try (InputStream is = rs.getBinaryStream("file_data")) {
                                if (is == null) return null;
                                File dir = new File("./uploads/" + category);
                                if (!dir.exists()) dir.mkdirs();
                                File tmp = new File(dir, filename + ".tmp_" + UUID.randomUUID().toString().substring(0, 6));
                                try (FileOutputStream fos = new FileOutputStream(tmp)) {
                                    byte[] buf = new byte[65536]; // 64KB buffer for Render 512MB RAM safety
                                    int n;
                                    while ((n = is.read(buf)) != -1) {
                                        fos.write(buf, 0, n);
                                    }
                                }
                                Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                return ct != null ? ct : resolveContentType(category, filename, null);
                            } catch (IOException ioEx) {
                                System.err.println(">>> [StoredMediaService] Failed writing restored file " + storageKey + ": " + ioEx.getMessage());
                                return null;
                            }
                        }
                );

                if (dbContentType != null && target.isFile() && target.length() > 0) {
                    System.out.println(">>> [StoredMediaService] Restored media from database to disk: /uploads/" + storageKey + " (" + target.length() + " bytes)");
                    return new ResolvedMedia(new FileSystemResource(target), dbContentType, target.length());
                }
            } catch (Exception e) {
                System.err.println(">>> [StoredMediaService] DB lookup warning for " + storageKey + ": " + e.getMessage());
            }
            return null;
        }
    }

    public static String sanitizeCategory(String category) {
        if (category == null) return null;
        String c = category.trim().toLowerCase(Locale.ROOT);
        if ("audio".equals(c) || "images".equals(c)) {
            return c;
        }
        return null;
    }

    public static boolean isSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) return false;
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\") || filename.contains("\0")) {
            return false;
        }
        return filename.length() <= 300;
    }

    public static String resolveContentType(String category, String filename, String suppliedType) {
        if (suppliedType != null && !suppliedType.trim().isEmpty() && !"application/octet-stream".equalsIgnoreCase(suppliedType.trim())) {
            return suppliedType.trim();
        }
        String lower = filename != null ? filename.toLowerCase(Locale.ROOT) : "";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".aac")) return "audio/aac";
        if (lower.endsWith(".webm")) return "audio/webm";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "audio".equals(category) ? "audio/mpeg" : "application/octet-stream";
    }

    public static class ResolvedMedia {
        public final Resource resource;
        public final String contentType;
        public final long contentLength;

        public ResolvedMedia(Resource resource, String contentType, long contentLength) {
            this.resource = resource;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }
    }
}
