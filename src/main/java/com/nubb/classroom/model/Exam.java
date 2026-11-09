package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        private Long accountId;
    @Column(name = "exam_month")
    private String month;
    @Column(length = 1000)
    private String className;
    private String subjectName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    @Column(columnDefinition = "TEXT")
    private String readingPassage;

    @Column(columnDefinition = "TEXT")
    private String wordBank;

    private String section1Title;
    private String section1Instruction;
    private String section2Title;
    private String section2Instruction;
    private String section3Title;
    private String section3Instruction;

    public String getReadingPassage() { return readingPassage; }
    public void setReadingPassage(String readingPassage) { this.readingPassage = readingPassage; }

    public String getWordBank() { return wordBank; }
    public void setWordBank(String wordBank) { this.wordBank = wordBank; }

    public String getSection1Title() { return section1Title; }
    public void setSection1Title(String section1Title) { this.section1Title = section1Title; }

    public String getSection1Instruction() { return section1Instruction; }
    public void setSection1Instruction(String section1Instruction) { this.section1Instruction = section1Instruction; }

    public String getSection2Title() { return section2Title; }
    public void setSection2Title(String section2Title) { this.section2Title = section2Title; }

    public String getSection2Instruction() { return section2Instruction; }
    public void setSection2Instruction(String section2Instruction) { this.section2Instruction = section2Instruction; }

    public String getSection3Title() { return section3Title; }
    public void setSection3Title(String section3Title) { this.section3Title = section3Title; }

    public String getSection3Instruction() { return section3Instruction; }
    public void setSection3Instruction(String section3Instruction) { this.section3Instruction = section3Instruction; }

    private String section4Title;
    private String section4Instruction;
    @Column(columnDefinition = "TEXT")
    private String writingPrompt;
    private Integer writingMaxPoints;

    public String getSection4Title() { return section4Title; }
    public void setSection4Title(String section4Title) { this.section4Title = section4Title; }

    public String getSection4Instruction() { return section4Instruction; }
    public void setSection4Instruction(String section4Instruction) { this.section4Instruction = section4Instruction; }

    public String getWritingPrompt() { return writingPrompt; }
    public void setWritingPrompt(String writingPrompt) { this.writingPrompt = writingPrompt; }

    public Integer getWritingMaxPoints() { return writingMaxPoints; }
    public void setWritingMaxPoints(Integer writingMaxPoints) { this.writingMaxPoints = writingMaxPoints; }
}
