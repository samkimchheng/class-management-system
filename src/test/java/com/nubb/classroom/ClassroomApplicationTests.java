package com.nubb.classroom;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.ScoreRepository;
import com.nubb.classroom.repository.ScoreRuleRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.StudentRepository;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Score;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.controller.LessonController;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClassroomApplicationTests {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ScoreRuleRepository scoreRuleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WebController webController;

    @Test
    void testLessonClassMatching() {
        // Universal class match
        assertTrue(LessonController.isClassMatching("គ្រប់ថ្នាក់", "10E"));
        assertTrue(LessonController.isClassMatching("ថ្នាក់រៀនទាំងអស់", "10E"));
        assertTrue(LessonController.isClassMatching("ALL", "10E"));

        // Comma-separated classes
        assertTrue(LessonController.isClassMatching("10E, 10F", "10E"));
        assertTrue(LessonController.isClassMatching("10E, 10F", "10F"));
        assertFalse(LessonController.isClassMatching("10E, 10F", "10G"));

        // Khmer numerals and "ថ្នាក់ទី" prefix variations
        assertTrue(LessonController.isClassMatching("ថ្នាក់ទី១០E", "10E"));
        assertTrue(LessonController.isClassMatching("10E", "ថ្នាក់ទី១០E"));
        assertTrue(LessonController.isClassMatching("ថ្នាក់១០E", "10E"));
        assertTrue(LessonController.isClassMatching("10E", "ថ្នាក់១០E"));
        assertTrue(LessonController.isClassMatching("១០E", "10E"));

        // Different classes
        assertFalse(LessonController.isClassMatching("11A", "10E"));
        assertFalse(LessonController.isClassMatching("10F", "10E"));
    }

    @Test
    void testSubjectMatching() {
        assertTrue(WebController.isSameSubject("គណិតវិទ្យា", "គណិតវិទ្យា"));
        assertTrue(WebController.isSameSubject("ជីវវិទ្យា", "ជីវៈវិទ្យា"));
        assertTrue(WebController.isSameSubject("ភាសាខ្មែរ", "អក្សរសិល្ប៍"));
        assertTrue(WebController.isSameSubject("សីលធម៌", "សីលធម៌-ពលរដ្ឋវិជ្ជា"));
        assertTrue(WebController.isSameSubject("ផែនដីវិទ្យា", "ផែនដី និងបរិស្ថានវិទ្យា"));
    }

    @Test
    void testStudentLessonVisibilityOnCreateAndDelete() {
        Student s = studentRepository.findByStudentCode("10E-01").orElse(null);
        assertNotNull(s);

        // 1. Verify initial eligible lessons
        List<Lesson> eligible = lessonRepository.findLessonsForStudent(s.getAccountId());
        List<Lesson> studentLessonsInitial = new java.util.ArrayList<>();
        for (Lesson l : eligible) {
            if (LessonController.isClassMatching(l.getClassName(), s.getClassName())) {
                studentLessonsInitial.add(l);
            }
        }
        int initialCount = studentLessonsInitial.size();

        // 2. Teacher creates new lesson with Arabic numerals "10E"
        Lesson newL = new Lesson();
        newL.setTitle("Lesson Unit Test 10E");
        newL.setClassName("10E");
        newL.setSubjectName("ភាសាអង់គ្លេស");
        newL.setAccountId(97L);
        lessonRepository.save(newL);

        // 3. Student immediately sees the new lesson (even though student className is in Khmer numerals "១០E")
        List<Lesson> eligibleWithNew = lessonRepository.findLessonsForStudent(s.getAccountId());
        List<Lesson> studentLessonsWithNew = new java.util.ArrayList<>();
        for (Lesson l : eligibleWithNew) {
            if (LessonController.isClassMatching(l.getClassName(), s.getClassName())) {
                studentLessonsWithNew.add(l);
            }
        }
        assertEquals(initialCount + 1, studentLessonsWithNew.size());
        assertTrue(studentLessonsWithNew.stream().anyMatch(l -> l.getTitle().equals("Lesson Unit Test 10E")));

        // 4. Teacher deletes the lesson
        lessonRepository.delete(newL);

        // 5. Student immediately no longer sees the deleted lesson
        List<Lesson> eligibleAfterDelete = lessonRepository.findLessonsForStudent(s.getAccountId());
        List<Lesson> studentLessonsAfterDelete = new java.util.ArrayList<>();
        for (Lesson l : eligibleAfterDelete) {
            if (LessonController.isClassMatching(l.getClassName(), s.getClassName())) {
                studentLessonsAfterDelete.add(l);
            }
        }
        assertEquals(initialCount, studentLessonsAfterDelete.size());
        assertFalse(studentLessonsAfterDelete.stream().anyMatch(l -> l.getTitle().equals("Lesson Unit Test 10E")));
    }

    @Test
    void testClassroomTracks() {
        Classroom c1 = new Classroom();
        c1.setClassName("ថ្នាក់ទី១១A");
        assertEquals("វិទ្យាសាស្ត្រ", c1.getTrack());

        Classroom c2 = new Classroom();
        c2.setClassName("ថ្នាក់ទី១១ សង្គម");
        assertEquals("វិទ្យាសាស្ត្រសង្គម", c2.getTrack());

        Classroom c3 = new Classroom();
        c3.setClassName("12B");
        assertEquals("វិទ្យាសាស្ត្រ", c3.getTrack());

        Classroom c4 = new Classroom();
        c4.setClassName("10E");
        assertEquals("ទូទៅ", c4.getTrack());
    }
}
