package com.skillscan.ai.unitTest;

import com.skillscan.ai.exception.ResumeNotFoundException;
import com.skillscan.ai.exception.ResumeParsingException;
import com.skillscan.ai.exception.ResumeTooLargeException;
import com.skillscan.ai.services.impl.ResumeParserServiceImpl;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeParserServiceImplTest {

    private final ResumeParserServiceImpl service = new ResumeParserServiceImpl();

    //  SUCCESS CASE
    @Test
    void extractText_shouldReturnContent_whenValidFile() throws Exception {
        Path tempFile = Files.createTempFile("resume", ".txt");
        Files.writeString(tempFile, "Hello Resume");

        String result = service.extractText(tempFile);

        assertNotNull(result);
        assertTrue(result.contains("Hello"));
    }

    //  FILE NOT FOUND
    @Test
    void extractText_shouldThrow_whenFileNotFound() {
        Path path = Path.of("non-existing-file.txt");

        assertThrows(ResumeNotFoundException.class,
                () -> service.extractText(path));
    }

    //  FILE TOO LARGE
    @Test
    void extractText_shouldThrow_whenFileTooLarge() throws IOException {
        Path tempFile = Files.createTempFile("large", ".txt");

        // create >10MB file
        byte[] largeContent = new byte[11 * 1024 * 1024];
        Files.write(tempFile, largeContent);

        assertThrows(ResumeTooLargeException.class,
                () -> service.extractText(tempFile));
    }

    //  EMPTY CONTENT
    @Test
    void extractText_shouldReturnEmpty_whenContentBlank() throws Exception {
        Path tempFile = Files.createTempFile("empty", ".txt");
        Files.writeString(tempFile, ""); // blank

        String result = service.extractText(tempFile);

        assertEquals("", result);
    }

    //  TIKA FAILURE (MOCKED)
    @Test
    void extractText_shouldThrowParsingException_whenTikaFails() throws Exception {
        ResumeParserServiceImpl spyService = spy(new ResumeParserServiceImpl());

        Tika mockTika = mock(Tika.class);

        Path tempFile = Files.createTempFile("resume", ".txt");
        Files.writeString(tempFile, "data");

        // inject mock tika
        ReflectionTestUtils.setField(spyService, "tika", mockTika);

        when(mockTika.parseToString(tempFile))
                .thenThrow(new IOException("Parsing failed"));

        assertThrows(ResumeParsingException.class,
                () -> spyService.extractText(tempFile));
    }
}