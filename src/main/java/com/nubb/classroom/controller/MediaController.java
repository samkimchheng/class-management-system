package com.nubb.classroom.controller;

import com.nubb.classroom.service.StoredMediaService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Serves uploaded lesson MP3 audio and images under /uploads/{category}/{filename}.
 * Automatically restores files from persistent database storage (stored_media) if the
 * container's ephemeral disk was wiped after a system update / Render redeploy.
 * Supports HTTP Range (206 Partial Content) for smooth audio playback & seeking on iOS/Android/Desktop.
 */
@Controller
public class MediaController {

    private static final long MAX_RANGE_CHUNK = 2L * 1024 * 1024; // 2MB max chunk per range request

    private final StoredMediaService storedMediaService;

    public MediaController(StoredMediaService storedMediaService) {
        this.storedMediaService = storedMediaService;
    }

    @RequestMapping(value = "/uploads/{category}/{filename:.+}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headMedia(
            @PathVariable("category") String category,
            @PathVariable("filename") String filename) {

        StoredMediaService.ResolvedMedia media = storedMediaService.resolveMedia(category, filename);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mt = parseMediaType(media.contentType);
        return ResponseEntity.ok()
                .contentType(mt)
                .contentLength(media.contentLength)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                .build();
    }

    @GetMapping("/uploads/{category}/{filename:.+}")
    public ResponseEntity<StreamingResponseBody> getMedia(
            @PathVariable("category") String category,
            @PathVariable("filename") String filename,
            @RequestHeader HttpHeaders headers) {

        StoredMediaService.ResolvedMedia media = storedMediaService.resolveMedia(category, filename);
        if (media == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mt = parseMediaType(media.contentType);
        long contentLength = media.contentLength;
        List<HttpRange> ranges = headers.getRange();

        if (ranges == null || ranges.isEmpty()) {
            StreamingResponseBody body = outputStream -> {
                try (InputStream is = media.resource.getInputStream()) {
                    byte[] buf = new byte[65536];
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        outputStream.write(buf, 0, read);
                    }
                    outputStream.flush();
                }
            };
            return ResponseEntity.ok()
                    .contentType(mt)
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                    .body(body);
        }

        try {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long requestedEnd = range.getRangeEnd(contentLength);
            if (start >= contentLength || start > requestedEnd) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                        .build();
            }
            long rangeLength = Math.min(requestedEnd - start + 1, MAX_RANGE_CHUNK);
            long actualEnd = start + rangeLength - 1;

            StreamingResponseBody body = outputStream -> {
                try (InputStream is = media.resource.getInputStream()) {
                    long toSkip = start;
                    while (toSkip > 0) {
                        long skipped = is.skip(toSkip);
                        if (skipped <= 0) {
                            if (is.read() == -1) break;
                            toSkip--;
                        } else {
                            toSkip -= skipped;
                        }
                    }
                    byte[] buf = new byte[65536];
                    long remaining = rangeLength;
                    while (remaining > 0) {
                        int maxRead = (int) Math.min(buf.length, remaining);
                        int read = is.read(buf, 0, maxRead);
                        if (read == -1) break;
                        outputStream.write(buf, 0, read);
                        remaining -= read;
                    }
                    outputStream.flush();
                }
            };

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mt)
                    .contentLength(rangeLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + actualEnd + "/" + contentLength)
                    .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
                    .body(body);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength)
                    .build();
        }
    }

    private MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
