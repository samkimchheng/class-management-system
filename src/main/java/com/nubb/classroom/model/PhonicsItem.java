package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "phonics_items", indexes = {
    @Index(name = "idx_phonics_level_unit", columnList = "phonicsLevel, unitName"),
    @Index(name = "idx_phonics_item_num", columnList = "itemNumber")
})
public class PhonicsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer itemNumber; // Sequential number within level or global

    @Column(nullable = false)
    private Integer phonicsLevel = 1; // 1 = Alphabet, 2 = Short Vowels, 3 = Long Vowels, 4 = Consonant Blends, 5 = Letter Combinations

    @Column(nullable = false)
    private String unitName; // e.g. "Unit 1: Aa - Cc"

    @Column(nullable = false)
    private String soundPattern; // e.g. "A a", "-at", "a_e", "sh-", "-ar"

    @Column(nullable = false)
    private String englishWord; // e.g. "cat"

    private String cleanWord; // e.g. "cat"

    private String ipaPhonetic; // e.g. "/kæt/"

    @Column(columnDefinition = "TEXT")
    private String khmerPhonetic; // e.g. "ខែថ៏ (ខ + អ៊ែ + ថ)"

    private String blendingParts; // e.g. "c|a|t" or "sh|i|p"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String khmerTranslation; // e.g. "សត្វឆ្មា"

    private String emojiIcon; // e.g. "🐱"

    @Column(columnDefinition = "TEXT")
    private String exampleSentence; // e.g. "The fat cat sat on a mat."

    @Column(columnDefinition = "TEXT")
    private String exampleSentenceKhmer; // e.g. "ឆ្មាធាត់អង្គុយលើកន្ទេល។"

    private Boolean active = true;

    private Long accountId;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PhonicsItem() {}

    public PhonicsItem(Integer itemNumber, Integer phonicsLevel, String unitName, String soundPattern,
                       String englishWord, String ipaPhonetic, String khmerPhonetic, String blendingParts,
                       String khmerTranslation, String emojiIcon, String exampleSentence, String exampleSentenceKhmer) {
        this.itemNumber = itemNumber;
        this.phonicsLevel = phonicsLevel != null ? phonicsLevel : 1;
        this.unitName = unitName;
        this.soundPattern = soundPattern;
        this.englishWord = englishWord;
        this.cleanWord = englishWord != null ? englishWord.replaceAll("\\s*\\(.*?\\)", "").trim() : "";
        this.ipaPhonetic = ipaPhonetic;
        this.khmerPhonetic = khmerPhonetic;
        this.blendingParts = blendingParts;
        this.khmerTranslation = khmerTranslation;
        this.emojiIcon = emojiIcon;
        this.exampleSentence = exampleSentence;
        this.exampleSentenceKhmer = exampleSentenceKhmer;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getItemNumber() { return itemNumber; }
    public void setItemNumber(Integer itemNumber) { this.itemNumber = itemNumber; }

    public Integer getPhonicsLevel() { return phonicsLevel != null ? phonicsLevel : 1; }
    public void setPhonicsLevel(Integer phonicsLevel) { this.phonicsLevel = phonicsLevel; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getSoundPattern() { return soundPattern; }
    public void setSoundPattern(String soundPattern) { this.soundPattern = soundPattern; }

    public String getEnglishWord() { return englishWord; }
    public void setEnglishWord(String englishWord) { this.englishWord = englishWord; }

    public String getCleanWord() { return cleanWord != null ? cleanWord : englishWord; }
    public void setCleanWord(String cleanWord) { this.cleanWord = cleanWord; }

    public String getIpaPhonetic() { return ipaPhonetic; }
    public void setIpaPhonetic(String ipaPhonetic) { this.ipaPhonetic = ipaPhonetic; }

    public String getKhmerPhonetic() { return khmerPhonetic; }
    public void setKhmerPhonetic(String khmerPhonetic) { this.khmerPhonetic = khmerPhonetic; }

    public String getBlendingParts() { return blendingParts; }
    public void setBlendingParts(String blendingParts) { this.blendingParts = blendingParts; }

    public String getKhmerTranslation() { return khmerTranslation; }
    public void setKhmerTranslation(String khmerTranslation) { this.khmerTranslation = khmerTranslation; }

    public String getEmojiIcon() { return emojiIcon != null && !emojiIcon.isEmpty() ? emojiIcon : "🔤"; }
    public void setEmojiIcon(String emojiIcon) { this.emojiIcon = emojiIcon; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getExampleSentenceKhmer() { return exampleSentenceKhmer; }
    public void setExampleSentenceKhmer(String exampleSentenceKhmer) { this.exampleSentenceKhmer = exampleSentenceKhmer; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
