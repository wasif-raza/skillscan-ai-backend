package com.skillscan.ai.services.impl;

import com.skillscan.ai.exception.ResumeNotFoundException;
import com.skillscan.ai.exception.ResumeParsingException;
import com.skillscan.ai.exception.ResumeTooLargeException;
import com.skillscan.ai.services.ResumeParserService;
import org.apache.tika.Tika;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;



@Slf4j
@Service
public class ResumeParserServiceImpl implements ResumeParserService {

    private static final long MAX_PARSE_SIZE_BYTES = 10 * 1024 * 1024;
    private final Tika tika = new Tika();


    @Override
    public String extractText(Path filePath) {
        try {
            if (!Files.exists(filePath)) {
                throw new ResumeNotFoundException("File not found: " + filePath);
            }
            long fileSize = Files.size(filePath);
            if (fileSize == 0) {
                log.warn("Empty file: {}", filePath);
                return "";
            }
            if (Files.size(filePath) > MAX_PARSE_SIZE_BYTES) {
                throw new ResumeTooLargeException("Resume file too large to parse");
            }

            String content = tika.parseToString(filePath);

            if (content == null || content.isBlank()) {
                log.warn("Parsed content is empty for file: {}", filePath);
                return "";
            }

            return content;

        } catch (IOException | TikaException ex) {
            log.error("Failed to parse resume: {}", filePath, ex);
            throw new ResumeParsingException("Failed to parse resume");
        }
    }
}