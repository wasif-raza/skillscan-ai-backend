package com.skillscan.ai.services;

import java.nio.file.Path;

public interface ResumeParserService {
    String extractText(Path filePath);
}
