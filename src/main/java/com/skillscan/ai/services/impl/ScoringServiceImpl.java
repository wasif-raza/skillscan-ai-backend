package com.skillscan.ai.services.impl;

import com.skillscan.ai.dto.response.AIResponse;
import com.skillscan.ai.services.ScoringService;
import com.skillscan.ai.services.SkillNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoringServiceImpl implements ScoringService {

    private final SkillNormalizer normalizer;

    @Override
    public AIResponse calculateWithJD(String resumeText, String jd) {

        Set<String> resumeSkills = normalize(extract(resumeText));
        Set<String> jdSkills = normalize(extract(jd));

        Set<String> matched = new HashSet<>(resumeSkills);
        matched.retainAll(jdSkills);

        Set<String> missing = new HashSet<>(jdSkills);
        missing.removeAll(resumeSkills);

        double score = jdSkills.isEmpty()
                ? 0
                : ((double) matched.size() / jdSkills.size()) * 100;

        return AIResponse.builder()
                .ruleScore(score)
                .matchedKeywords(new ArrayList<>(matched))
                .missingKeywords(new ArrayList<>(missing))
                .suggestions(buildSuggestions(missing))
                .build();
    }

    @Override
    public AIResponse calculateWithoutJD(String resumeText) {

        Set<String> skills = normalize(extract(resumeText));

        double score = Math.min(skills.size() * 5, 100);

        return AIResponse.builder()
                .ruleScore(score)
                .skills(new ArrayList<>(skills))
                .suggestions(List.of("Add measurable achievements"))
                .build();
    }

    private Set<String> extract(String text) {
        if (text == null) return new HashSet<>();

        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toSet());
    }

    private Set<String> normalize(Set<String> skills) {
        return new HashSet<>(normalizer.normalize(new ArrayList<>(skills)));
    }

    private List<String> buildSuggestions(Set<String> missing) {
        return missing.stream().map(s -> "Add skill: " + s).toList();
    }
}