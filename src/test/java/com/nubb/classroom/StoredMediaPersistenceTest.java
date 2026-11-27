package com.nubb.classroom;

import com.nubb.classroom.service.StoredMediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StoredMediaPersistenceTest {

    @Autowired
    private StoredMediaService storedMediaService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void uploadedMp3SurvivesContainerDiskWipeAndSupportsRangeStreaming() throws Exception {
        // Generate a 256KB synthetic MP3 payload
        byte[] payload = new byte[256 * 1024];
        new Random(42L).nextBytes(payload);
        payload[0] = 'I'; payload[1] = 'D'; payload[2] = '3'; // MP3 ID3 header signature

        MockMultipartFile mp3 = new MockMultipartFile(
                "tsAudioFile",
                "unit1_listening.mp3",
                "audio/mpeg",
                payload
        );

        String urlPath = storedMediaService.saveMedia("audio", mp3, 97L, "test");
        assertNotNull(urlPath);
        assertTrue(urlPath.startsWith("/uploads/audio/"));

        String storageKey = urlPath.substring("/uploads/".length());
        File diskFile = new File("./uploads/" + storageKey);
        assertTrue(diskFile.exists(), "Local cache file should exist immediately after upload");

        try {
            // Simulate Render container rebuild / redeploy wiping ephemeral ./uploads/ directory!
            assertTrue(diskFile.delete(), "Should delete local file to simulate Render container wipe");
            assertFalse(diskFile.exists(), "Disk file must be absent before recovery test");

            // 1. HEAD request should auto-restore from DB and return Accept-Ranges: bytes
            mockMvc.perform(head(urlPath))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, payload.length))
                    .andExpect(content().contentType("audio/mpeg"));

            assertTrue(diskFile.exists(), "Disk file should be auto-restored from stored_media DB");

            // 2. Delete again and test direct GET full download auto-restore (async StreamingResponseBody)
            assertTrue(diskFile.delete());
            MvcResult fullAsync = mockMvc.perform(get(urlPath))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            MvcResult fullRes = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(fullAsync))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(content().contentType("audio/mpeg"))
                    .andReturn();
            assertArrayEquals(payload, fullRes.getResponse().getContentAsByteArray(), "Restored MP3 bytes must match 100%");

            // 3. Test HTTP Range (206 Partial Content) used by iOS Safari & Android Chrome seeking
            MvcResult rangeAsync = mockMvc.perform(get(urlPath).header(HttpHeaders.RANGE, "bytes=100-599"))
                    .andExpect(request().asyncStarted())
                    .andReturn();
            MvcResult rangeRes = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(rangeAsync))
                    .andExpect(status().isPartialContent())
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 100-599/" + payload.length))
                    .andReturn();
            assertArrayEquals(
                    Arrays.copyOfRange(payload, 100, 600),
                    rangeRes.getResponse().getContentAsByteArray(),
                    "Range slice bytes must match exact offset"
            );
        } finally {
            // Clean up test artifact from DB and local disk
            if (diskFile.exists()) diskFile.delete();
            jdbcTemplate.update("DELETE FROM stored_media WHERE storage_key = ?", storageKey);
        }
    }
}
