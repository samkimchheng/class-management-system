package com.nubb.classroom.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/pdf")
public class PdfDownloadController {

    private final Map<String, TempPdf> tempStorage = new ConcurrentHashMap<>();

    private static class TempPdf {
        final byte[] data;
        final String filename;
        final long timestamp;

        TempPdf(byte[] data, String filename) {
            this.data = data;
            this.filename = filename;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @PostMapping("/prepare")
    public ResponseEntity<Map<String, String>> preparePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filename", required = false) String filename) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Empty file"));
        }

        String cleanFilename = (filename != null && !filename.trim().isEmpty())
                ? filename.trim().replace("/", "-").replace("\\", "-")
                : "report.pdf";

        String id = UUID.randomUUID().toString();
        tempStorage.put(id, new TempPdf(file.getBytes(), cleanFilename));

        // Auto-cleanup items older than 5 minutes
        long now = System.currentTimeMillis();
        tempStorage.entrySet().removeIf(e -> (now - e.getValue().timestamp) > 300_000);

        String encodedFilename = URLEncoder.encode(cleanFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String downloadUrl = "/api/pdf/download/" + id + "/" + encodedFilename;

        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    @GetMapping("/download/{id}/{filename}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable("id") String id,
            @PathVariable("filename") String filename) {

        TempPdf temp = tempStorage.get(id);
        if (temp == null) {
            return ResponseEntity.notFound().build();
        }

        String cleanFilename = temp.filename;
        String encodedFilename = URLEncoder.encode(cleanFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        String asciiFallback = cleanFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (asciiFallback.replace(".pdf", "").replace("_", "").trim().isEmpty()) {
            asciiFallback = "report.pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentLength(temp.data.length)
                .body(temp.data);
    }
}
