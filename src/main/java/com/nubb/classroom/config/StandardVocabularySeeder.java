package com.nubb.classroom.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nubb.classroom.model.VocabularyWord;
import com.nubb.classroom.repository.VocabularyWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
public class StandardVocabularySeeder {

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void seedGrade10Vocabulary() {
        try {
            long existingCount = vocabularyWordRepository.countByGradeLevel(10);
            if (existingCount >= 460) {
                // Already fully seeded
                return;
            }

            ClassPathResource resource = new ClassPathResource("data/grade10_vocabulary.json");
            if (!resource.exists()) {
                System.err.println(">>> [StandardVocabularySeeder] grade10_vocabulary.json resource not found!");
                return;
            }

            try (InputStream is = resource.getInputStream()) {
                List<Map<String, Object>> list = objectMapper.readValue(is, new TypeReference<>() {});
                if (list == null || list.isEmpty()) return;

                // Load existing words to avoid duplicating by wordNumber
                List<VocabularyWord> existingList = vocabularyWordRepository.findByGradeLevelOrderByWordNumberAsc(10);
                Map<Integer, VocabularyWord> existingByNum = new HashMap<>();
                for (VocabularyWord w : existingList) {
                    if (w.getWordNumber() != null) {
                        existingByNum.put(w.getWordNumber(), w);
                    }
                }

                List<VocabularyWord> toSave = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    Number numVal = (Number) map.get("num");
                    if (numVal == null) continue;
                    int num = numVal.intValue();

                    String english = (String) map.get("english");
                    String cleanWord = (String) map.get("cleanWord");
                    String pos = (String) map.get("pos");
                    String khmer = (String) map.get("khmer");
                    String unit = (String) map.get("unit");

                    VocabularyWord word = existingByNum.get(num);
                    if (word == null) {
                        word = new VocabularyWord();
                        word.setWordNumber(num);
                        word.setGradeLevel(10);
                        word.setActive(true);
                    }
                    word.setEnglishWord(english);
                    word.setCleanWord(cleanWord != null ? cleanWord : english);
                    word.setPartOfSpeech(pos);
                    word.setKhmerTranslation(khmer);
                    word.setUnitName(unit);
                    toSave.add(word);
                }

                if (!toSave.isEmpty()) {
                    vocabularyWordRepository.saveAll(toSave);
                    System.out.println(">>> [StandardVocabularySeeder] Successfully seeded " + toSave.size() + " Grade 10 vocabulary words!");
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [StandardVocabularySeeder] Error seeding vocabulary: " + e.getMessage());
        }
    }
}
