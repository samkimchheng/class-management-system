package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vocabulary_words", indexes = {
    @Index(name = "idx_vocab_grade_unit", columnList = "gradeLevel, unitName"),
    @Index(name = "idx_vocab_word_num", columnList = "wordNumber")
})
public class VocabularyWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer wordNumber; // 1 to 460

    @Column(nullable = false)
    private String englishWord; // e.g. "birthday (n)"

    private String cleanWord; // e.g. "birthday" (for native Web Speech TTS)

    private String partOfSpeech; // e.g. "n", "v", "adj", "adv", "prep", "phr v"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String khmerTranslation; // e.g. "ថ្ងៃកំណើត"

    @Column(nullable = false)
    private String unitName; // e.g. "unit 1", "unit 2", etc.

    private Integer gradeLevel = 10; // Default 10

    private Long accountId; // null = shared/system for Grade 10, or specific teacher ID

    private Boolean active = true; // Teacher can toggle visibility

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    private LocalDateTime createdAt = LocalDateTime.now();

    public VocabularyWord() {}

    public VocabularyWord(Integer wordNumber, String englishWord, String cleanWord, String partOfSpeech,
                          String khmerTranslation, String unitName, Integer gradeLevel, Long accountId) {
        this.wordNumber = wordNumber;
        this.englishWord = englishWord;
        this.cleanWord = cleanWord != null ? cleanWord : englishWord;
        this.partOfSpeech = partOfSpeech;
        this.khmerTranslation = khmerTranslation;
        this.unitName = unitName;
        this.gradeLevel = gradeLevel != null ? gradeLevel : 10;
        this.accountId = accountId;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWordNumber() {
        return wordNumber;
    }

    public void setWordNumber(Integer wordNumber) {
        this.wordNumber = wordNumber;
    }

    public String getEnglishWord() {
        return englishWord;
    }

    public void setEnglishWord(String englishWord) {
        this.englishWord = englishWord;
    }

    public String getCleanWord() {
        return cleanWord;
    }

    public void setCleanWord(String cleanWord) {
        this.cleanWord = cleanWord;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getKhmerTranslation() {
        return khmerTranslation;
    }

    public void setKhmerTranslation(String khmerTranslation) {
        this.khmerTranslation = khmerTranslation;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Boolean getActive() {
        return active != null ? active : true;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
