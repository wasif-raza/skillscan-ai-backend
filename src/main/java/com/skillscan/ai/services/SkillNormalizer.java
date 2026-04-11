package com.skillscan.ai.services;

import java.util.List;

public interface SkillNormalizer {
    List<String> normalize(List<String> skills);
}