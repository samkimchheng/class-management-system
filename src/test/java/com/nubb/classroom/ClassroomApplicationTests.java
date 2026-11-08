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
import com.nubb.classroom.repository.LeaveRequestRepository;
import com.nubb.classroom.repository.AttendanceRepository;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Score;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.LeaveRequest;
import com.nubb.classroom.model.Attendance;
import com.nubb.classroom.controller.LessonController;
import java.time.LocalDate;
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
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

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

        // 1. Verify initial eligible lessons via resolveLessonsForStudent
        List<Lesson> studentLessonsInitial = LessonController.resolveLessonsForStudent(lessonRepository, s);
        int initialCount = studentLessonsInitial.size();

        // 2. Teacher (account 97 or 33) creates new lesson with Arabic numerals "10E"
        Lesson newL = new Lesson();
        newL.setTitle("Lesson Unit Test 10E");
        newL.setClassName("10E");
        newL.setSubjectName("ភាសាអង់គ្លេស");
        newL.setAccountId(33L);
        lessonRepository.save(newL);

        // 3. Student immediately sees the new lesson (even though student className is in Khmer numerals "១០E" and account is 97)
        List<Lesson> studentLessonsWithNew = LessonController.resolveLessonsForStudent(lessonRepository, s);
        assertEquals(initialCount + 1, studentLessonsWithNew.size());
        assertTrue(studentLessonsWithNew.stream().anyMatch(l -> l.getTitle().equals("Lesson Unit Test 10E")));

        // 4. Teacher deletes the lesson
        lessonRepository.delete(newL);

        // 5. Student immediately no longer sees the deleted lesson
        List<Lesson> studentLessonsAfterDelete = LessonController.resolveLessonsForStudent(lessonRepository, s);
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

    @Test
    void testStudentLeaveRequestFlowForAllTeachers() {
        Student s = studentRepository.findByStudentCode("10E-01").orElse(null);
        assertNotNull(s);

        LocalDate testDate = LocalDate.of(2027, 4, 15);

        LeaveRequest lr = new LeaveRequest();
        lr.setStudent(s);
        lr.setAccountId(s.getAccountId() != null ? s.getAccountId() : 97L);
        lr.setClassName(s.getClassName());
        lr.setAcademicYear("២០២៦-២០២៧");
        lr.setStartDate(testDate);
        lr.setEndDate(testDate);
        lr.setLeaveType("ឈឺ");
        lr.setLeaveSession("ពេញមួយថ្ងៃ");
        lr.setReason("ឈឺគ្រុនក្តៅ");
        lr.setStatus("PENDING");

        LeaveRequest saved = leaveRequestRepository.save(lr);
        webController.syncLeaveRequestToAttendance(saved, true);

        // Verify teacher sees the leave request
        List<LeaveRequest> teacherReqs = webController.getLeaveRequestsForTeacher(97L, "10E");
        assertTrue(teacherReqs.stream().anyMatch(r -> r.getId().equals(saved.getId())));

        // Verify attendance was synced as PERMISSION
        List<Attendance> atts = attendanceRepository.findByStudentIdAndDateBetween(s.getId(), testDate, testDate);
        assertFalse(atts.isEmpty());
        assertTrue(atts.get(0).isPermission());

        // Cleanup test data
        leaveRequestRepository.delete(saved);
        webController.syncLeaveRequestToAttendance(saved, false);
    }

    @Test
    void testSortStudentsByClassAndKhmerAlphabet() {
        Student s1 = new Student(); s1.setName("សុខ ចាន់ថន"); s1.setClassName("10B");
        Student s2 = new Student(); s2.setName("កែវ សុខា"); s2.setClassName("10B");
        Student s3 = new Student(); s3.setName("អ៊ុក សម្បត្តិ"); s3.setClassName("10A");
        Student s4 = new Student(); s4.setName("ខៀវ តារា"); s4.setClassName("10A");
        Student s5 = new Student(); s5.setName("ចាន់ ធីតា"); s5.setClassName("7A");

        List<Student> input = java.util.Arrays.asList(s1, s2, s3, s4, s5);
        List<Student> sorted = WebController.sortStudentsByClassAndKhmerAlphabet(input);

        // Class 7A comes first, then 10A (ខៀវ តារា before អ៊ុក សម្បត្តិ), then 10B (កែវ សុខា before សុខ ចាន់ថន)
        assertEquals("ចាន់ ធីតា", sorted.get(0).getName());
        assertEquals("ខៀវ តារា", sorted.get(1).getName());
        assertEquals("អ៊ុក សម្បត្តិ", sorted.get(2).getName());
        assertEquals("កែវ សុខា", sorted.get(3).getName());
        assertEquals("សុខ ចាន់ថន", sorted.get(4).getName());
    }
}
