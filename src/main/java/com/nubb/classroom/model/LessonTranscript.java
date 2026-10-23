package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lesson_transcripts")
public class LessonTranscript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long lessonId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String khmerContent;

    private String audioUrl;

    private int orderIndex;

    public LessonTranscript() {}

    public LessonTranscript(Long lessonId, String title, String content, String khmerContent, String audioUrl, int orderIndex) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.khmerContent = khmerContent;
        this.audioUrl = audioUrl;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getKhmerContent() {
        return khmerContent;
    }

    public void setKhmerContent(String khmerContent) {
        this.khmerContent = khmerContent;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
