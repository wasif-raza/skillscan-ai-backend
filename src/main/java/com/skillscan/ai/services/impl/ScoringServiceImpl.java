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

    //  Stopwords
    private static final Set<String> STOPWORDS = Set.of(
            "the","a","an","and","or","to","of","in","on","with","for",
            "is","are","was","were","be","been","being","we","you",
            "they","he","she","it","as","at","by","from"
    );

    //  Valid Skills (expand this as needed)
    private static final Set<String> VALID_SKILLS = Set.of(
            "java","spring","springboot","react","reactjs","nodejs",
            "docker","kubernetes","postgresql","mysql","redis",
            "aws","git","rest","api","microservices","hibernate","jpa",
            "c","c++","c#","go","python","javascript"
    );

    //  Special short skills
    private static final Set<String> SPECIAL_SKILLS = Set.of(
            "c","c++","c#","go","r"
    );

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
                .skills(new ArrayList<>(resumeSkills))
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
                .matchedKeywords(Collections.emptyList())
                .missingKeywords(Collections.emptyList())
                .suggestions(List.of("Add measurable achievements"))
                .build();
    }

    //  CLEAN SKILL EXTRACTION
    private Set<String> extract(String text) {
        if (text == null) return new HashSet<>();

        String lower = text.toLowerCase();
        Set<String> result = new HashSet<>();

        //  Phrase detection
        if (lower.contains("spring boot")) result.add("springboot");
        if (lower.contains("react js") || lower.contains("react.js")) result.add("reactjs");
        if (lower.contains("node js") || lower.contains("node.js")) result.add("nodejs");

        //  Special normalization
        if (lower.contains("c++") || lower.contains("cpp") || lower.contains("c plus plus")) {
            result.add("c++");
        }
        if (lower.contains("c#")) result.add("c#");
        if (lower.contains("golang")) result.add("go");

        //  Token extraction
        Set<String> tokens = Arrays.stream(lower.split("[^a-zA-Z0-9+#.]+"))
                .filter(s -> s.length() > 2 || SPECIAL_SKILLS.contains(s))
                .filter(s -> !STOPWORDS.contains(s))
                .collect(Collectors.toSet());

        //  Keep only valid skills
        tokens.stream()
                .filter(VALID_SKILLS::contains)
                .forEach(result::add);

        return result;
    }

    private Set<String> normalize(Set<String> skills) {
        return new HashSet<>(normalizer.normalize(new ArrayList<>(skills)));
    }

    private List<String> buildSuggestions(Set<String> missing) {
        return missing.stream()
                .filter(VALID_SKILLS::contains)
                .map(s -> "Add skill : " + s)
                .collect(Collectors.toList());
    }
}