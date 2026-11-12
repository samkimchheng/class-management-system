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
import com.nubb.classroom.model.ScoreRule;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.LeaveRequest;
import com.nubb.classroom.model.Attendance;
import com.nubb.classroom.controller.LessonController;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
class ClassroomApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ScoreRuleRepository scoreRuleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private com.nubb.classroom.repository.LessonProgressRepository lessonProgressRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private com.nubb.classroom.repository.ExamRepository examRepository;

    @Autowired
    private com.nubb.classroom.repository.QuestionRepository questionRepository;

    @Autowired
    private com.nubb.classroom.repository.DailyTeachingLogRepository dailyTeachingLogRepository;

    @Autowired
    private com.nubb.classroom.repository.VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private com.nubb.classroom.repository.StudentVocabularyProgressRepository studentVocabularyProgressRepository;

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

        // 2. Teacher creates new lesson with Arabic numerals "10E"
        Lesson newL = new Lesson();
        newL.setTitle("Lesson Unit Test 10E");
        newL.setClassName("10E");
        newL.setSubjectName("ភាសាអង់គ្លេស");
        newL.setAccountId(s.getAccountId() != null ? s.getAccountId() : 97L);
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

    @Test
    void testEndpointsRendering() throws Exception {
        // 1. Unauthenticated root redirects to /login
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());

        // 2. /login page renders successfully (HTTP 200)
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        // 3. /test student login page renders successfully (HTTP 200)
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk());

        // 4. Authenticated Teacher dashboard / renders successfully (HTTP 200)
        Account teacher = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(teacher);
        mockMvc.perform(get("/")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole())
                .sessionAttr("ACCOUNT_NAME", teacher.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        // 5. Authenticated Student portal /test/take renders successfully (HTTP 200)
        Student student = studentRepository.findByStudentCode("10E-01").orElse(null);
        assertNotNull(student);
        mockMvc.perform(get("/test/take")
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk());

        // 6. Authenticated Student leave page /test/leave renders successfully (HTTP 200)
        mockMvc.perform(get("/test/leave")
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk());

        // 7. Authenticated Teacher lessons page /lessons renders successfully (HTTP 200)
        mockMvc.perform(get("/lessons")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole()))
                .andExpect(status().isOk());

        // 8. Authenticated Teacher exams page /exams renders successfully (HTTP 200)
        mockMvc.perform(get("/exams")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole()))
                .andExpect(status().isOk());
    }

    @Test
    void testAllStudentsAndTeachersLoginAndRender() throws Exception {
        // Test all teachers
        List<Account> accounts = accountRepository.findAll();
        System.out.println(">>> Testing all " + accounts.size() + " accounts...");
        for (Account acc : accounts) {
            if ("TEACHER".equals(acc.getRole())) {
                mockMvc.perform(get("/")
                        .sessionAttr("ACCOUNT_ID", acc.getId())
                        .sessionAttr("ACCOUNT_ROLE", acc.getRole())
                        .sessionAttr("ACCOUNT_NAME", acc.getFullName() != null ? acc.getFullName() : acc.getUsername())
                        .sessionAttr("CURRENT_ACADEMIC_YEAR", acc.getCurrentAcademicYear() != null ? acc.getCurrentAcademicYear() : "២០២៦-២០២៧"))
                        .andExpect(status().isOk());
            }
        }

        // Test all students
        List<Student> students = studentRepository.findAll();
        System.out.println(">>> Testing all " + students.size() + " students...");
        for (Student s : students) {
            mockMvc.perform(get("/test/take")
                    .sessionAttr("loggedInStudent", s)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/test/leave")
                    .sessionAttr("loggedInStudent", s)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void testExamSecurityAndRetakeRules() throws Exception {
        Account teacher = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(teacher);
        Student student = studentRepository.findByStudentCode("10E-01").orElse(null);
        assertNotNull(student);

        // 1. Create a test exam for student's class (10E)
        com.nubb.classroom.model.Exam exam = new com.nubb.classroom.model.Exam();
        exam.setAccountId(teacher.getId());
        exam.setMonth("ខែវិច្ឆិកា ២០២៦");
        exam.setSubjectName("ភាសាខ្មែរ-តេស្តសុវត្ថិភាព");
        exam.setClassName("10E");
        exam.setOpenClasses(""); // Initially CLOSED
        exam = examRepository.save(exam);

        com.nubb.classroom.model.Question q = new com.nubb.classroom.model.Question();
        q.setExamId(exam.getId());
        q.setAccountId(teacher.getId());
        q.setSkillType("grammar");
        q.setQuestionText("សំណួរតេស្ត?");
        q.setOptionA("A");
        q.setOptionB("B");
        q.setOptionC("C");
        q.setOptionD("D");
        q.setCorrectOption("A");
        questionRepository.save(q);

        // Rule 1 Test: When student is absent today, entrance & submit are BLOCKED
        LocalDate today = LocalDate.now();
        attendanceRepository.findByStudentId(student.getId()).forEach(a -> {
            if (today.equals(a.getDate())) attendanceRepository.delete(a);
        });
        Attendance absentAtt = new Attendance(student, today, false, "UNEXCUSED");
        absentAtt.setAccountId(teacher.getId());
        attendanceRepository.save(absentAtt);

        // Student tries to load exam while absent -> HTTP 403 Forbidden with ABSENT
        mockMvc.perform(get("/api/student-portal/exam")
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.reason").value("ABSENT"));

        // Student tries to submit while absent -> HTTP 403 Forbidden
        mockMvc.perform(post("/api/student-portal/submit")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"month\":\"" + exam.getMonth() + "\",\"subjectName\":\"" + exam.getSubjectName() + "\",\"totalScore\":80}")
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.reason").value("ABSENT"));

        // Rule 2 Test: When student is present but class is CLOSED -> BLOCKED
        absentAtt.setPresent(true);
        absentAtt.setAbsenceType(null);
        attendanceRepository.save(absentAtt);

        // Exam is still closed (openClasses = "")
        mockMvc.perform(get("/api/student-portal/exam")
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.reason").value("CLASS_CLOSED"));

        // Teacher opens class access via /api/exams/toggle-class-access
        mockMvc.perform(post("/api/exams/toggle-class-access")
                .param("examId", String.valueOf(exam.getId()))
                .param("className", "10E")
                .param("open", "true")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.isOpen").value(true));

        // Now student CAN access the exam
        mockMvc.perform(get("/api/student-portal/exam")
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.exam.id").value(exam.getId()));

        // Rule 3 Test: One Attempt & Teacher Retake Permission
        // Clean any old score for this test subject
        scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), exam.getMonth(), student.getClassName(), exam.getSubjectName(), teacher.getId())
                .ifPresent(s -> scoreRepository.delete(s));

        // Submit the exam
        mockMvc.perform(post("/api/student-portal/submit")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"month\":\"" + exam.getMonth() + "\",\"subjectName\":\"" + exam.getSubjectName() + "\",\"totalScore\":85}")
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk());

        // Student tries to enter exam again -> BLOCKED with ALREADY_SUBMITTED
        mockMvc.perform(get("/api/student-portal/exam")
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.reason").value("ALREADY_SUBMITTED"));

        // Teacher grants retake permission via /api/exams/allow-retake
        mockMvc.perform(post("/api/exams/allow-retake")
                .param("studentId", String.valueOf(student.getId()))
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        // Student can now take exam again
        mockMvc.perform(get("/api/student-portal/exam")
                .param("month", exam.getMonth())
                .param("subjectName", exam.getSubjectName())
                .sessionAttr("loggedInStudent", student)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk());

        // Cleanup test data
        attendanceRepository.delete(absentAtt);
        scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), exam.getMonth(), student.getClassName(), exam.getSubjectName(), teacher.getId())
                .ifPresent(s -> scoreRepository.delete(s));
        questionRepository.delete(q);
        examRepository.delete(exam);
    }
    @Test
    void testTimetableAttendanceVerificationOnDashboard() throws Exception {
        Account teacher = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(teacher);

        // 1. Sunday (2026-11-08) is weekend -> hasTeachingToday must be false, attendance counts are 0
        mockMvc.perform(get("/")
                .param("date", "2026-11-08")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole())
                .sessionAttr("ACCOUNT_NAME", teacher.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasTeachingToday", false))
                .andExpect(model().attribute("presentToday", 0L))
                .andExpect(model().attribute("absentToday", 0L));

        // 2. Wednesday (2026-11-11) all classes -> teacher teaches 10E/10F -> hasTeachingToday must be true
        mockMvc.perform(get("/")
                .param("date", "2026-11-11")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole())
                .sessionAttr("ACCOUNT_NAME", teacher.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasTeachingToday", true));

        // 3. Wednesday (2026-11-11) for class 10E -> teacher teaches 10E on Wednesday -> true
        mockMvc.perform(get("/")
                .param("date", "2026-11-11")
                .param("className", "10E")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole())
                .sessionAttr("ACCOUNT_NAME", teacher.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasTeachingToday", true));

        // 4. Wednesday (2026-11-11) for class 11G -> teacher does NOT teach 11G on Wednesday (taught Tue/Thu) -> false
        mockMvc.perform(get("/")
                .param("date", "2026-11-11")
                .param("className", "11G")
                .sessionAttr("ACCOUNT_ID", teacher.getId())
                .sessionAttr("ACCOUNT_ROLE", teacher.getRole())
                .sessionAttr("ACCOUNT_NAME", teacher.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasTeachingToday", false))
                .andExpect(model().attribute("presentToday", 0L))
                .andExpect(model().attribute("absentToday", 0L));
    }

    @Test
    void testSeungSomunyHasNoGrade7Students() {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        // Verify Seung Somuny has 0 students in 7A or Grade 7
        List<Student> somunyStudents = studentRepository.findByAccountId(somuny.getId());
        assertTrue(somunyStudents.stream().noneMatch(s -> s.getClassName() != null && (s.getClassName().contains("7A") || s.getClassName().contains("7"))));

        // Verify no 7A score rules exist for Seung Somuny
        List<ScoreRule> somunyRules = scoreRuleRepository.findByAccountId(somuny.getId());
        assertTrue(somunyRules.stream().noneMatch(r -> r.getClassName() != null && (r.getClassName().contains("7A") || r.getClassName().contains("7"))));

        // Verify no 7A classrooms exist for Seung Somuny
        List<Classroom> somunyClasses = classroomRepository.findByAccountId(somuny.getId());
        assertTrue(somunyClasses.stream().noneMatch(c -> c.getClassName() != null && (c.getClassName().contains("7A") || c.getClassName().contains("7"))));
    }

    @Test
    void testBatchImportMultipleClassesWithoutSequenceCollision() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        // 1. Import Class 8D
        String payload8D = "{\"className\":\"ថ្នាក់ទី៨D\",\"overwrite\":false,\"students\":[" +
                "{\"name\":\"សិស្ស តេស្ត ៨D-១\",\"gender\":\"ប្រុស\",\"studentCode\":\"\"}," +
                "{\"name\":\"សិស្ស តេស្ត ៨D-២\",\"gender\":\"ស្រី\",\"studentCode\":\"\"}" +
                "]}";

        mockMvc.perform(post("/api/students/import-batch")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(payload8D)
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        // 2. Import Class 8E (Previously failed with HTTP 500 Primary Key violation)
        String payload8E = "{\"className\":\"ថ្នាក់ទី៨E\",\"overwrite\":false,\"students\":[" +
                "{\"name\":\"សិស្ស តេស្ត ៨E-១\",\"gender\":\"ប្រុស\",\"studentCode\":\"\"}," +
                "{\"name\":\"សិស្ស តេស្ត ៨E-២\",\"gender\":\"ស្រី\",\"studentCode\":\"\"}" +
                "]}";

        mockMvc.perform(post("/api/students/import-batch")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(payload8E)
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        // 3. Verify students exist and have clean auto-formatted codes
        List<Student> students8E = studentRepository.findByClassNameAndAccountIdAndAcademicYear("ថ្នាក់ទី៨E", somuny.getId(), "២០២៦-២០២៧");
        assertNotNull(students8E);
        assertEquals(2, students8E.size());
        assertEquals("៨E-01", students8E.get(0).getStudentCode());
        assertEquals("៨E-02", students8E.get(1).getStudentCode());

        // Clean up test students so DB remains clean
        studentRepository.deleteAll(students8E);
    }

    @Test
    void testDailyPlanWorkflowAndScheduleIntegration() throws Exception {
        Account kimchheng = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(kimchheng);

        // Clear test date 2026-11-11 so workflow tests clean saving
        var oldLogs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(kimchheng.getId(), LocalDate.parse("2026-11-11"));
        dailyTeachingLogRepository.deleteAll(oldLogs);

        // 1. GET /daily-plan for Wednesday (2026-11-11)
        // Sam Kimchheng has 4 hours on Wednesday: 10G, 11G, 10F, 11F
        mockMvc.perform(get("/daily-plan")
                .param("date", "2026-11-11")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasTeachingSlots", true))
                .andExpect(model().attribute("totalSlots", 4));

        // 2. POST /api/daily-plan/save for Wednesday (2026-11-11)
        String saveJson = "{\"date\":\"2026-11-11\",\"items\":[" +
                "{\"slotIndex\":0,\"periodNumber\":1,\"className\":\"10G\",\"subjectName\":\"ភាសាអង់គ្លេស\"," +
                "\"lessonTitle\":\"Unit 4: Environment - Reading Passage 1\"," +
                "\"contentSummary\":\"ពន្យល់ពាក្យគន្លឹះ និងអានអត្ថបទអំពីការការពារបរិស្ថាន\"," +
                "\"objectives\":\"សិស្សយល់ដឹងពីវិធីកាត់បន្ថយប្លាស្ទិក\"," +
                "\"homework\":\"ធ្វើលំហាត់ទំព័រ ៥០\",\"notes\":\"សិស្សចូលរួមយ៉ាងសកម្ម\"}," +
                "{\"slotIndex\":1,\"periodNumber\":2,\"className\":\"11G\",\"subjectName\":\"ភាសាអង់គ្លេស\"," +
                "\"lessonTitle\":\"Unit 4: Grammar Practice\"," +
                "\"contentSummary\":\"វេយ្យាករណ៍ Present Perfect Continuous\"," +
                "\"objectives\":\"សិស្សចេះប្រើប្រាស់កាលបានត្រឹមត្រូវ\"," +
                "\"homework\":\"តែងប្រយោគ ៥ ប្រយោគ\",\"notes\":\"ល្អ\"}" +
                "]}";

        mockMvc.perform(post("/api/daily-plan/save")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(saveJson)
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        // 3. Verify in repository
        var logs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(kimchheng.getId(), LocalDate.parse("2026-11-11"));
        assertEquals(2, logs.size());
        assertEquals("Unit 4: Environment - Reading Passage 1", logs.get(0).getLessonTitle());
        assertEquals("10G", logs.get(0).getClassName());
        assertEquals("Unit 4: Grammar Practice", logs.get(1).getLessonTitle());

        // 4. Test delete single slot
        mockMvc.perform(post("/api/daily-plan/delete-slot")
                .param("date", "2026-11-11")
                .param("slotIndex", "1")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId()))
                .andExpect(status().isOk());

        var logsAfterSlotDelete = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(kimchheng.getId(), LocalDate.parse("2026-11-11"));
        assertEquals(1, logsAfterSlotDelete.size());

        // 5. Test delete entire day
        mockMvc.perform(post("/api/daily-plan/delete-day")
                .param("date", "2026-11-11")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId()))
                .andExpect(status().isOk());

        var logsAfterDayDelete = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(kimchheng.getId(), LocalDate.parse("2026-11-11"));
        assertEquals(0, logsAfterDayDelete.size());

        // 6. Verify homepage dashboard has tomorrow daily plan model attributes
        mockMvc.perform(get("/")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tomorrowDateStr"))
                .andExpect(model().attributeExists("tomorrowClasses"));
    }

    @Test
    void testGrade10VocabularySystem() throws Exception {
        // 1. Verify 460 Grade 10 words and 303 Grade 11 words were seeded
        long count10 = vocabularyWordRepository.countByGradeLevel(10);
        assertTrue(count10 >= 460, "Expected at least 460 Grade 10 vocabulary words but found " + count10);

        long count11 = vocabularyWordRepository.countByGradeLevel(11);
        assertTrue(count11 >= 303, "Expected at least 303 Grade 11 vocabulary words but found " + count11);

        // Verify specific Grade 10 words
        var firstWordOpt = vocabularyWordRepository.findByGradeLevelAndWordNumber(10, 1);
        assertTrue(firstWordOpt.isPresent());
        assertEquals("birthday (n)", firstWordOpt.get().getEnglishWord());
        assertEquals("birthday", firstWordOpt.get().getCleanWord());
        assertEquals("n", firstWordOpt.get().getPartOfSpeech());
        assertEquals("ថ្ងៃកំណើត", firstWordOpt.get().getKhmerTranslation());

        var lastWordOpt = vocabularyWordRepository.findByGradeLevelAndWordNumber(10, 460);
        assertTrue(lastWordOpt.isPresent());
        assertEquals("watersports (n)", lastWordOpt.get().getEnglishWord());

        // Verify specific Grade 11 words
        var firstG11Opt = vocabularyWordRepository.findByGradeLevelAndWordNumber(11, 1);
        assertTrue(firstG11Opt.isPresent());
        assertEquals("cinema (n)", firstG11Opt.get().getEnglishWord());
        assertEquals("cinema", firstG11Opt.get().getCleanWord());
        assertEquals("n", firstG11Opt.get().getPartOfSpeech());
        assertEquals("រោងភាពយន្ត", firstG11Opt.get().getKhmerTranslation());
        assertEquals("unit 1 & 3", firstG11Opt.get().getUnitName());

        var lastG11Opt = vocabularyWordRepository.findByGradeLevelAndWordNumber(11, 303);
        assertTrue(lastG11Opt.isPresent());
        assertEquals("unnoticed (adj)", lastG11Opt.get().getEnglishWord());
        assertEquals("unit 31", lastG11Opt.get().getUnitName());

        // 2. Verify Teacher Vocabulary Management View (/vocabulary)
        Account kimchheng = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(kimchheng);

        mockMvc.perform(get("/vocabulary")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("words"))
                .andExpect(model().attributeExists("units"))
                .andExpect(model().attributeExists("totalWords"))
                .andExpect(model().attributeExists("studentProgressList"));

        // 3. Verify Student Interactive Vocabulary Portal (/test/vocabulary)
        List<Student> students = studentRepository.findAll();
        assertFalse(students.isEmpty());
        Student testStudent = students.stream()
                .filter(s -> s.getClassName() != null && s.getClassName().contains("10"))
                .findFirst()
                .orElse(students.get(0));

        mockMvc.perform(get("/test/vocabulary")
                .sessionAttr("loggedInStudent", testStudent)
                .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("words"))
                .andExpect(model().attributeExists("units"))
                .andExpect(model().attributeExists("masteredWordIds"))
                .andExpect(model().attributeExists("totalMastered"));

        // 4. Verify Student toggle mastered API
        Long testWordId = firstWordOpt.get().getId();
        mockMvc.perform(post("/api/student-portal/vocabulary/toggle-mastered")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"wordId\":" + testWordId + ",\"mastered\":true}")
                .sessionAttr("loggedInStudent", testStudent))
                .andExpect(status().isOk());

        var progOpt = studentVocabularyProgressRepository.findByStudentIdAndWordId(testStudent.getId(), testWordId);
        assertTrue(progOpt.isPresent());
        assertTrue(progOpt.get().getMastered());

        // 5. Verify Student Quiz record API
        String quizJson = "{\"unitName\":\"unit 1\",\"totalQuestions\":1,\"correctAnswers\":1," +
                "\"results\":[{\"wordId\":" + testWordId + ",\"correct\":true}]}";

        mockMvc.perform(post("/api/student-portal/vocabulary/record-quiz")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(quizJson)
                .sessionAttr("loggedInStudent", testStudent))
                .andExpect(status().isOk());

        // 6. Verify Dynamic Quiz generator
        mockMvc.perform(get("/api/student-portal/vocabulary/quiz-data")
                .param("unit", "unit 1")
                .sessionAttr("loggedInStudent", testStudent))
                .andExpect(status().isOk());

        // 7. Verify Teacher Word status toggle API
        mockMvc.perform(post("/api/vocabulary/toggle-status")
                .param("id", testWordId.toString())
                .sessionAttr("ACCOUNT_ID", kimchheng.getId()))
                .andExpect(status().isOk());

        // Toggle back to true
        mockMvc.perform(post("/api/vocabulary/toggle-status")
                .param("id", testWordId.toString())
                .sessionAttr("ACCOUNT_ID", kimchheng.getId()))
                .andExpect(status().isOk());

        // 7b. Verify Teacher Grade 11 Vocabulary Management View & Grade 11 Student Portal
        mockMvc.perform(get("/vocabulary")
                .param("grade", "11")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedGrade", 11))
                .andExpect(model().attribute("totalWords", count11));

        Student g11Student = students.stream()
                .filter(s -> s.getClassName() != null && (s.getClassName().contains("11") || s.getClassName().contains("១១")))
                .findFirst()
                .orElse(null);
        if (g11Student != null) {
            mockMvc.perform(get("/test/vocabulary")
                    .sessionAttr("loggedInStudent", g11Student)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedGrade", 11))
                    .andExpect(model().attributeExists("words"));

            mockMvc.perform(get("/api/student-portal/vocabulary/quiz-data")
                    .param("unit", "unit 2")
                    .param("grade", "11")
                    .sessionAttr("loggedInStudent", g11Student))
                    .andExpect(status().isOk());
        }

        // Clean up progress created for testStudent
        studentVocabularyProgressRepository.delete(progOpt.get());

        // 8. Verify Unauthorized teacher (Seung Somuny) cannot access /vocabulary or APIs
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);
        mockMvc.perform(get("/vocabulary")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?accessDenied=vocab"));

        mockMvc.perform(post("/api/vocabulary/toggle-status")
                .param("id", testWordId.toString())
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isForbidden());

        // 9. Verify Seung Somuny's Class 8D and 48 students are intact
        List<Student> somuny8D = studentRepository.findByClassNameAndAccountId("៨D", somuny.getId());
        assertEquals(48, somuny8D.size());
    }

    @Test
    void testGrade12VocabularySystemAndPermissions() throws Exception {
        // 1. Verify all 294 Grade 12 words were seeded cleanly
        long count12 = vocabularyWordRepository.countByGradeLevel(12);
        assertTrue(count12 >= 294, "Expected at least 294 Grade 12 vocabulary words but found " + count12);

        var firstG12Opt = vocabularyWordRepository.findByGradeLevelAndWordNumber(12, 1);
        assertTrue(firstG12Opt.isPresent());
        assertEquals("come between (phr v)", firstG12Opt.get().getEnglishWord());
        assertEquals("come between", firstG12Opt.get().getCleanWord());
        assertEquals("phr v", firstG12Opt.get().getPartOfSpeech());
        assertEquals("នៅចន្លោះ", firstG12Opt.get().getKhmerTranslation());
        assertEquals("unit 1", firstG12Opt.get().getUnitName());

        var row98Opt = vocabularyWordRepository.findByGradeLevelAndWordNumber(12, 98);
        assertTrue(row98Opt.isPresent());
        assertEquals("password (n)", row98Opt.get().getEnglishWord());
        assertEquals("password", row98Opt.get().getCleanWord());
        assertEquals("n", row98Opt.get().getPartOfSpeech());
        assertEquals("លេខសម្ងាត់", row98Opt.get().getKhmerTranslation());

        var lastG12Opt = vocabularyWordRepository.findByGradeLevelAndWordNumber(12, 294);
        assertTrue(lastG12Opt.isPresent());
        assertEquals("van (n)", lastG12Opt.get().getEnglishWord());
        assertEquals("ឡាន", lastG12Opt.get().getKhmerTranslation());
        assertEquals("unit 31", lastG12Opt.get().getUnitName());

        // 2. Verify Sam Kimchheng can manage Grade 12 vocabulary
        Account kimchheng = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(kimchheng);

        mockMvc.perform(get("/vocabulary")
                .param("grade", "12")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedGrade", 12))
                .andExpect(model().attribute("gradeKhmer", "១២"))
                .andExpect(model().attribute("totalWords", count12));

        // 3. Verify Unauthorized teacher (Seung Somuny) is blocked unless Admin grants permission
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);
        boolean origPerm = Boolean.TRUE.equals(somuny.getVocabularyPermission());

        Student tempG12Student = null;
        Long g12WordId = firstG12Opt.get().getId();
        try {
            mockMvc.perform(get("/vocabulary")
                    .param("grade", "12")
                    .sessionAttr("ACCOUNT_ID", somuny.getId())
                    .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                    .sessionAttr("ACCOUNT_NAME", somuny.getFullName()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/?accessDenied=vocab"));

            mockMvc.perform(post("/api/vocabulary/reset-default")
                    .param("grade", "12")
                    .sessionAttr("ACCOUNT_ID", somuny.getId()))
                    .andExpect(status().isForbidden());

            // Admin explicitly grants vocabulary permission -> allowed
            somuny.setVocabularyPermission(true);
            accountRepository.save(somuny);

            mockMvc.perform(get("/vocabulary")
                    .param("grade", "12")
                    .sessionAttr("ACCOUNT_ID", somuny.getId())
                    .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                    .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                    .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedGrade", 12));

            // 4. Verify Grade 12 student auto-detects Grade 12 vocabulary, quiz, mastery, & portal links
            tempG12Student = new Student();
            tempG12Student.setName("សិស្សសាកល្បងទី១២");
            tempG12Student.setGender("ប្រុស");
            tempG12Student.setClassName("12A");
            tempG12Student.setStudentCode("TEST-G12-VOCAB-001");
            tempG12Student.setAccountId(somuny.getId());
            tempG12Student = studentRepository.save(tempG12Student);

            mockMvc.perform(get("/test/vocabulary")
                    .sessionAttr("loggedInStudent", tempG12Student)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("selectedGrade", 12))
                    .andExpect(model().attribute("gradeKhmer", "១២"))
                    .andExpect(model().attribute("totalWords", count12))
                    .andExpect(model().attributeExists("words"));

            // Student portal pages (/test/take and /test/leave) show Vocab link for Grade 12 student
            mockMvc.perform(get("/test/take")
                    .sessionAttr("loggedInStudent", tempG12Student)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/test/vocabulary")));

            mockMvc.perform(get("/test/leave")
                    .sessionAttr("loggedInStudent", tempG12Student)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/test/vocabulary")));

            // Quiz & mastery APIs work for Grade 12
            mockMvc.perform(get("/api/student-portal/vocabulary/quiz-data")
                    .param("unit", "unit 1")
                    .param("grade", "12")
                    .sessionAttr("loggedInStudent", tempG12Student))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/student-portal/vocabulary/toggle-mastered")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content("{\"wordId\":" + g12WordId + ",\"mastered\":true}")
                    .sessionAttr("loggedInStudent", tempG12Student))
                    .andExpect(status().isOk());
        } finally {
            somuny.setVocabularyPermission(origPerm);
            accountRepository.save(somuny);
            if (tempG12Student != null && tempG12Student.getId() != null) {
                var progList = studentVocabularyProgressRepository.findByStudentId(tempG12Student.getId());
                if (!progList.isEmpty()) {
                    studentVocabularyProgressRepository.deleteAll(progList);
                }
                studentRepository.deleteById(tempG12Student.getId());
            }
        }
    }

    @Test
    void testKhmerStandardExamWorkflow_ZeroDataLoss() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        // 1. Verify /exams page loads cleanly with 200 OK
        mockMvc.perform(get("/exams")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        // 2. Verify Demo account (98) received starter Standard MoEYS Khmer Exam safely
        Account demoAcc = accountRepository.findById(98L).orElse(null);
        assertNotNull(demoAcc);
        List<com.nubb.classroom.model.Exam> demoKhmerExams = examRepository.findByAccountId(demoAcc.getId()).stream()
                .filter(e -> "ភាសាខ្មែរ".equals(e.getSubjectName()))
                .toList();
        assertFalse(demoKhmerExams.isEmpty(), "Expected starter Standard Khmer Exam for Demo account");

        com.nubb.classroom.model.Exam sampleExam = demoKhmerExams.get(0);
        assertNotNull(sampleExam.getReadingPassage());
        assertNotNull(sampleExam.getWritingPrompt());
        List<com.nubb.classroom.model.Question> qs = questionRepository.findByExamIdAndAccountId(sampleExam.getId(), demoAcc.getId());
        assertTrue(qs.stream().anyMatch(q -> "kh_reading".equals(q.getSkillType())));
        assertTrue(qs.stream().anyMatch(q -> "kh_grammar".equals(q.getSkillType())));
        assertTrue(qs.stream().anyMatch(q -> "kh_literature".equals(q.getSkillType())));

        // 3. Verify saving a new Khmer Standard Exam via /api/exams/save works and does not disturb Sam Kimchheng's exams
        long kimchhengExamCountBefore = examRepository.findByAccountId(97L).size();
        String khmerPayload = """
            {
              "month": "ប្រចាំឆ្នាំ",
              "classes": ["៨D"],
              "subjectName": "ភាសាខ្មែរ",
              "readingPassage": "អត្ថបទស្តង់ដាសម្រាប់តេស្ត...",
              "section1Title": "I. អំណាន៖",
              "section1Instruction": "ជ្រើសរើសចម្លើយ",
              "section1MaxPoints": 15,
              "section2Title": "II. វេយ្យាករណ៍៖",
              "section2Instruction": "ជ្រើសរើសចម្លើយ",
              "section2MaxPoints": 15,
              "section3Title": "III. អក្សរសិល្ប៍៖",
              "section3Instruction": "ជ្រើសរើសចម្លើយ",
              "section3MaxPoints": 15,
              "section4Title": "IV. តែងសេចក្តី៖",
              "section4Instruction": "សរសេរតែងសេចក្តី",
              "writingPrompt": "ពន្យល់សុភាសិត ចេះពីរៀន មានពីរក។",
              "writingMaxPoints": 25,
              "questions": [
                {"skillType":"kh_reading","questionText":"សំណួរទី១","optionA":"ក","optionB":"ខ","optionC":"គ","optionD":"ឃ","correctOption":"B"},
                {"skillType":"kh_grammar","questionText":"សំណួរទី២","optionA":"ក","optionB":"ខ","optionC":"គ","optionD":"ឃ","correctOption":"A"},
                {"skillType":"kh_literature","questionText":"សំណួរទី៣","optionA":"ក","optionB":"ខ","optionC":"គ","optionD":"ឃ","correctOption":"C"}
              ]
            }
            """;

        mockMvc.perform(post("/api/exams/save")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(khmerPayload)
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        assertEquals(kimchhengExamCountBefore, examRepository.findByAccountId(97L).size());

        // 4. Verify saved Khmer exam can be loaded back with all 4 sections + questions
        var createdOpt = examRepository.findByMonthAndSubjectNameAndAccountId("ប្រចាំឆ្នាំ", "ភាសាខ្មែរ", somuny.getId()).stream().findFirst();
        assertTrue(createdOpt.isPresent());
        assertEquals("ពន្យល់សុភាសិត ចេះពីរៀន មានពីរក។", createdOpt.get().getWritingPrompt());
        assertEquals(3, questionRepository.findByExamIdAndAccountId(createdOpt.get().getId(), somuny.getId()).size());

        // Clean up the test-created "ប្រចាំឆ្នាំ" exam while keeping starter seeded data intact
        var createdQs = questionRepository.findByExamIdAndAccountId(createdOpt.get().getId(), somuny.getId());
        questionRepository.deleteAll(createdQs);
        examRepository.delete(createdOpt.get());
    }

    @Test
    void testMathStandardExamWorkflow_ZeroDataLoss() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        // 1. Verify /exams renders cleanly with Math template & 48 Grade 8D students intact
        mockMvc.perform(get("/exams")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());
        assertEquals(48, studentRepository.findByClassNameAndAccountId("៨D", somuny.getId()).size());

        // 2. Verify Math teacher (33) and Demo (98) both have a Math exam safely (seeded only if previously missing)
        List<com.nubb.classroom.model.Exam> somunyMathExams = examRepository.findByAccountId(somuny.getId()).stream()
                .filter(e -> "គណិតវិទ្យា".equals(e.getSubjectName()))
                .toList();
        assertFalse(somunyMathExams.isEmpty(), "Expected Math teacher account 33 to have a Math exam");

        Account demoAcc = accountRepository.findById(98L).orElse(null);
        assertNotNull(demoAcc);
        List<com.nubb.classroom.model.Exam> demoMathExams = examRepository.findByAccountId(demoAcc.getId()).stream()
                .filter(e -> "គណិតវិទ្យា".equals(e.getSubjectName()))
                .toList();
        assertFalse(demoMathExams.isEmpty(), "Expected Demo account 98 to have starter Standard Math Exam");

        com.nubb.classroom.model.Exam demoSample = demoMathExams.get(0);
        List<com.nubb.classroom.model.Question> demoQs = questionRepository.findByExamIdAndAccountId(demoSample.getId(), demoAcc.getId());
        assertTrue(demoQs.stream().anyMatch(q -> "math_algebra".equals(q.getSkillType())));
        assertTrue(demoQs.stream().anyMatch(q -> "math_geometry".equals(q.getSkillType())));
        assertTrue(demoQs.stream().anyMatch(q -> "math_applied".equals(q.getSkillType())));

        // 3. Verify saving a Standard 4-section Math Exam works without touching Sam Kimchheng's exams
        long kimchhengExamCountBefore = examRepository.findByAccountId(97L).size();
        String mathPayload = """
            {
              "month": "__TEST_MATH__",
              "classes": ["៨D"],
              "subjectName": "គណិតវិទ្យា",
              "readingPassage": "រូបមន្តជំនួយ៖ (a+b)² = a² + 2ab + b²",
              "section1Title": "I. ពីជគណិត៖",
              "section1Instruction": "គណនា",
              "section1MaxPoints": 15,
              "section2Title": "II. ធរណីមាត្រ៖",
              "section2Instruction": "ដោះស្រាយ",
              "section2MaxPoints": 15,
              "section3Title": "III. អនុគមន៍/ស្ថិតិ៖",
              "section3Instruction": "ជ្រើសរើសចម្លើយ",
              "section3MaxPoints": 15,
              "section4Title": "IV. ចំណោទដោះស្រាយលម្អិត៖",
              "section4Instruction": "សរសេរជាជំហានៗ",
              "writingPrompt": "ដោះស្រាយសមីការ 2x + 6 = 14",
              "writingMaxPoints": 25,
              "questions": [
                {"skillType":"math_algebra","questionText":"2x+6=14","optionA":"2","optionB":"4","optionC":"6","optionD":"8","correctOption":"B"},
                {"skillType":"math_geometry","questionText":"ផលបូកមុំក្នុងត្រីកោណ","optionA":"90°","optionB":"180°","optionC":"270°","optionD":"360°","correctOption":"B"},
                {"skillType":"math_applied","questionText":"f(x)=3x-2, f(4)=","optionA":"10","optionB":"12","optionC":"14","optionD":"5","correctOption":"A"}
              ]
            }
            """;

        mockMvc.perform(post("/api/exams/save")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(mathPayload)
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        assertEquals(kimchhengExamCountBefore, examRepository.findByAccountId(97L).size());

        // 4. Verify saved Math exam reloads cleanly with all 4 sections + questions, then clean up test row
        var createdOpt = examRepository.findByMonthAndSubjectNameAndAccountId("__TEST_MATH__", "គណិតវិទ្យា", somuny.getId()).stream().findFirst();
        assertTrue(createdOpt.isPresent());
        assertEquals("ដោះស្រាយសមីការ 2x + 6 = 14", createdOpt.get().getWritingPrompt());
        assertEquals(3, questionRepository.findByExamIdAndAccountId(createdOpt.get().getId(), somuny.getId()).size());

        var createdQs = questionRepository.findByExamIdAndAccountId(createdOpt.get().getId(), somuny.getId());
        questionRepository.deleteAll(createdQs);
        examRepository.delete(createdOpt.get());
    }

    @Test
    void testPhysicsStandardExamAndImageUpload_ZeroDataLoss() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        // 1. Verify /exams renders cleanly and Demo account (98) received starter Standard MoEYS Physics Exam
        mockMvc.perform(get("/exams")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk());

        Account demoAcc = accountRepository.findById(98L).orElse(null);
        assertNotNull(demoAcc);
        List<com.nubb.classroom.model.Exam> demoPhysExams = examRepository.findByAccountId(demoAcc.getId()).stream()
                .filter(e -> "រូបវិទ្យា".equals(e.getSubjectName()))
                .toList();
        assertFalse(demoPhysExams.isEmpty(), "Expected starter Standard Physics Exam for Demo account");
        List<com.nubb.classroom.model.Question> demoQs = questionRepository.findByExamIdAndAccountId(demoPhysExams.get(0).getId(), demoAcc.getId());
        assertTrue(demoQs.stream().anyMatch(q -> "phys_mechanics".equals(q.getSkillType())));
        assertTrue(demoQs.stream().anyMatch(q -> "phys_optics".equals(q.getSkillType())));
        assertTrue(demoQs.stream().anyMatch(q -> "phys_electricity".equals(q.getSkillType())));

        // 2. Verify saving a Standard Physics Exam WITH uploaded exam images & question diagram persists cleanly without affecting other teachers' data
        long kimchhengExamCountBefore = examRepository.findByAccountId(97L).size();
        String tinyPngDataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";
        String physPayload = """
            {
              "month": "__TEST_PHYSICS__",
              "classes": ["៨D"],
              "subjectName": "រូបវិទ្យា",
              "readingPassage": "រូបមន្ត៖ U = R·I, g = 10 m/s²",
              "examImagesJson": "[{\\"caption\\":\\"សៀគ្វីអគ្គិសនី\\",\\"dataUrl\\":\\"%s\\"}]",
              "section1Title": "I. មេកានិច៖",
              "section1Instruction": "គណនា",
              "section1MaxPoints": 15,
              "section2Title": "II. អុបទិក៖",
              "section2Instruction": "ជ្រើសរើស",
              "section2MaxPoints": 15,
              "section3Title": "III. អគ្គិសនី៖",
              "section3Instruction": "ជ្រើសរើស",
              "section3MaxPoints": 15,
              "section4Title": "IV. លំហាត់ដោះស្រាយលម្អិត៖",
              "section4Instruction": "សរសេរជាជំហានៗ",
              "writingPrompt": "គណនាអាំងតង់ស៊ីតេចរន្ត I ពីសៀគ្វីក្នុងរូបភាព។",
              "writingMaxPoints": 25,
              "questions": [
                {"skillType":"phys_mechanics","questionText":"ខ្នាតកម្លាំង","optionA":"J","optionB":"N","optionC":"W","optionD":"Pa","correctOption":"B","imageData":"%s"},
                {"skillType":"phys_optics","questionText":"27°C ស្មើ","optionA":"246K","optionB":"273K","optionC":"300K","optionD":"327K","correctOption":"C"},
                {"skillType":"phys_electricity","questionText":"ខ្នាតរេស៊ីស្តង់","optionA":"A","optionB":"V","optionC":"Ω","optionD":"C","correctOption":"C"}
              ]
            }
            """.formatted(tinyPngDataUrl, tinyPngDataUrl);

        mockMvc.perform(post("/api/exams/save")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(physPayload)
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

        assertEquals(kimchhengExamCountBefore, examRepository.findByAccountId(97L).size());
        assertEquals(48, studentRepository.findByClassNameAndAccountId("៨D", somuny.getId()).size());

        // 3. Verify GET /api/exams/get returns uploaded exam images + per-question image intact
        mockMvc.perform(get("/api/exams/get")
                .param("month", "__TEST_PHYSICS__")
                .param("subjectName", "រូបវិទ្យា")
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.exam.examImagesJson").value(org.hamcrest.Matchers.containsString("សៀគ្វីអគ្គិសនី")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.questions[0].imageData").value(tinyPngDataUrl));

        // Clean up temporary test row only
        var createdOpt = examRepository.findByMonthAndSubjectNameAndAccountId("__TEST_PHYSICS__", "រូបវិទ្យា", somuny.getId()).stream().findFirst();
        assertTrue(createdOpt.isPresent());
        var createdQs = questionRepository.findByExamIdAndAccountId(createdOpt.get().getId(), somuny.getId());
        questionRepository.deleteAll(createdQs);
        examRepository.delete(createdOpt.get());
    }

    @Test
    void testAllRemainingStandardSubjectsAndMathImageUpload_ZeroDataLoss() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);
        Account demoAcc = accountRepository.findById(98L).orElse(null);
        assertNotNull(demoAcc);

        long kimchhengExamCountBefore = examRepository.findByAccountId(97L).size();
        String tinyPngDataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";

        // 1. Verify Demo account (98) received starter Standard MoEYS Exams for all 7 newly added subjects
        String[][] subjectPrefixes = {
            {"គីមីវិទ្យា", "chem_sec1"},
            {"ជីវវិទ្យា", "bio_sec1"},
            {"ប្រវត្តិវិទ្យា", "hist_sec1"},
            {"ភូមិវិទ្យា", "geo_sec1"},
            {"សីលធម៌", "civic_sec1"},
            {"ផែនដីវិទ្យា", "earth_sec1"},
            {"កុំព្យូទ័រ", "it_sec1"}
        };
        List<com.nubb.classroom.model.Exam> demoAll = examRepository.findByAccountId(demoAcc.getId());
        for (String[] pair : subjectPrefixes) {
            String subj = pair[0];
            String expectedSkill = pair[1];
            var found = demoAll.stream().filter(e -> subj.equals(e.getSubjectName())).findFirst();
            assertTrue(found.isPresent(), "Expected starter Standard Exam for Demo account on subject: " + subj);
            List<com.nubb.classroom.model.Question> qs = questionRepository.findByExamIdAndAccountId(found.get().getId(), demoAcc.getId());
            assertTrue(qs.stream().anyMatch(q -> expectedSkill.equals(q.getSkillType())), "Expected skillType " + expectedSkill);
        }

        // 2. Verify Math + Image Upload save/reload works cleanly
        String mathImgPayload = """
            {
              "month": "__TEST_MATH_IMG__",
              "classes": ["៨D"],
              "subjectName": "គណិតវិទ្យា",
              "readingPassage": "c² = a² + b²",
              "examImagesJson": "[{\\"caption\\":\\"ក្រាបត្រីកោណកែង\\",\\"dataUrl\\":\\"%s\\"}]",
              "section1Title": "I. ពីជគណិត៖", "section1MaxPoints": 15,
              "section2Title": "II. ធរណីមាត្រ៖", "section2MaxPoints": 15,
              "section3Title": "III. ស្ថិតិ៖", "section3MaxPoints": 15,
              "section4Title": "IV. ចំណោទ៖", "writingPrompt": "ដោះស្រាយតាមក្រាប", "writingMaxPoints": 25,
              "questions": [
                {"skillType":"math_algebra","questionText":"2x=8","optionA":"2","optionB":"4","optionC":"6","optionD":"8","correctOption":"B","imageData":"%s"}
              ]
            }
            """.formatted(tinyPngDataUrl, tinyPngDataUrl);
        mockMvc.perform(post("/api/exams/save")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(mathImgPayload)
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/exams/get")
                .param("month", "__TEST_MATH_IMG__")
                .param("subjectName", "គណិតវិទ្យា")
                .sessionAttr("ACCOUNT_ID", somuny.getId()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.exam.examImagesJson").value(org.hamcrest.Matchers.containsString("ក្រាបត្រីកោណកែង")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.questions[0].imageData").value(tinyPngDataUrl));
        var mathTmp = examRepository.findByMonthAndSubjectNameAndAccountId("__TEST_MATH_IMG__", "គណិតវិទ្យា", somuny.getId()).get(0);
        questionRepository.deleteAll(questionRepository.findByExamIdAndAccountId(mathTmp.getId(), somuny.getId()));
        examRepository.delete(mathTmp);

        // 3. Verify saving and reloading any of the 7 standard subjects WITH uploaded exam image + zero data loss
        for (String[] pair : subjectPrefixes) {
            String subj = pair[0];
            String skill1 = pair[1];
            String payload = """
                {
                  "month": "__TEST_STD__",
                  "classes": ["៨D"],
                  "subjectName": "%s",
                  "examImagesJson": "[{\\"caption\\":\\"រូបភាព%s\\",\\"dataUrl\\":\\"%s\\"}]",
                  "section1Title": "I. ផ្នែកទី១", "section1MaxPoints": 15,
                  "writingPrompt": "សំណួរសរសេរលម្អិត", "writingMaxPoints": 25,
                  "questions": [
                    {"skillType":"%s","questionText":"សំណួរ%s","optionA":"A","optionB":"B","optionC":"C","optionD":"D","correctOption":"A","imageData":"%s"}
                  ]
                }
                """.formatted(subj, subj, tinyPngDataUrl, skill1, subj, tinyPngDataUrl);
            mockMvc.perform(post("/api/exams/save")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(payload)
                    .sessionAttr("ACCOUNT_ID", somuny.getId()))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success").value(true));

            var tmpExam = examRepository.findByMonthAndSubjectNameAndAccountId("__TEST_STD__", subj, somuny.getId()).stream().findFirst();
            assertTrue(tmpExam.isPresent(), "Expected saved exam for " + subj);
            assertTrue(tmpExam.get().getExamImagesJson().contains("រូបភាព" + subj));
            questionRepository.deleteAll(questionRepository.findByExamIdAndAccountId(tmpExam.get().getId(), somuny.getId()));
            examRepository.delete(tmpExam.get());
        }

        // 4. Confirm zero impact on Sam Kimchheng's exams and Seung Somuny's 48 Grade 8D students
        assertEquals(kimchhengExamCountBefore, examRepository.findByAccountId(97L).size());
        assertEquals(48, studentRepository.findByClassNameAndAccountId("៨D", somuny.getId()).size());
    }

    @Test
    void testAdminComputerTrainingLinksVisibleForTeacherAndStudent() throws Exception {
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);

        String fbUrl = "https://web.facebook.com/bv.2pc";
        String webUrl = "https://thebravocenter.pages.dev";
        String ttUrl = "https://www.tiktok.com/@bravocenter27";

        // 1. Verify Teacher Dashboard "/" and "/lessons" contain all 3 Admin computer center link buttons
        mockMvc.perform(get("/")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(fbUrl)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(webUrl)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(ttUrl)));

        mockMvc.perform(get("/lessons")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole())
                .sessionAttr("ACCOUNT_NAME", somuny.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(fbUrl)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(webUrl)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(ttUrl)));

        // 2. Verify Student Portal pages (/test/take, /test/leave) contain all 3 Admin computer center link buttons
        Student sampleStudent = studentRepository.findByClassNameAndAccountId("៨D", somuny.getId()).get(0);
        for (String stuPath : new String[]{"/test/take", "/test/leave"}) {
            mockMvc.perform(get(stuPath)
                    .sessionAttr("loggedInStudent", sampleStudent)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT")
                    .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(fbUrl)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(webUrl)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(ttUrl)));
        }
    }

    @Test
    void testLessonStudentProgressTracking() throws Exception {
        Account kimchheng = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(kimchheng);

        List<Student> kimStudents = studentRepository.findByAccountId(kimchheng.getId());
        assertFalse(kimStudents.isEmpty());
        Student stu = kimStudents.stream()
                .filter(s -> !LessonController.resolveLessonsForStudent(lessonRepository, s).isEmpty())
                .findFirst()
                .orElse(kimStudents.get(0));
        List<Lesson> stuLessons = LessonController.resolveLessonsForStudent(lessonRepository, stu);
        assertFalse(stuLessons.isEmpty());
        Lesson targetLesson = stuLessons.get(0);

        String userKey = "STUDENT_" + stu.getId();
        var existingOpt = lessonProgressRepository.findByLessonIdAndUserKey(targetLesson.getId(), userKey);
        int origPct = existingOpt.map(com.nubb.classroom.model.LessonProgress::getProgressPercentage).orElse(-1);
        boolean origDone = existingOpt.map(com.nubb.classroom.model.LessonProgress::isCompleted).orElse(false);

        try {
            // 1. Student updates lesson progress via /api/lessons/progress
            mockMvc.perform(post("/api/lessons/progress")
                    .param("lessonId", targetLesson.getId().toString())
                    .param("progressPercentage", "100")
                    .sessionAttr("loggedInStudent", stu)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk());

            // 2. Teacher opens /lessons?tab=progress&progressClass=... and sees student progress tracking
            mockMvc.perform(get("/lessons")
                    .param("tab", "progress")
                    .param("progressClass", stu.getClassName())
                    .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                    .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                    .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                    .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("activeTab", "progress"))
                    .andExpect(model().attribute("selectedProgressClass", stu.getClassName()))
                    .andExpect(model().attributeExists("studentProgressList"))
                    .andExpect(model().attributeExists("progressClasses"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("📊 តាមដានការរៀនរបស់សិស្ស")))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString(stu.getName())));

            // 3. Student view of /lessons does NOT render the teacher progress tracking tab
            mockMvc.perform(get("/lessons")
                    .sessionAttr("loggedInStudent", stu)
                    .sessionAttr("ACTIVE_USER_MODE", "STUDENT"))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"progressTab\""))));
        } finally {
            var afterOpt = lessonProgressRepository.findByLessonIdAndUserKey(targetLesson.getId(), userKey);
            if (afterOpt.isPresent()) {
                if (origPct == -1) {
                    lessonProgressRepository.delete(afterOpt.get());
                } else {
                    var lp = afterOpt.get();
                    lp.setProgressPercentage(origPct);
                    lp.setCompleted(origDone);
                    lessonProgressRepository.save(lp);
                }
            }
        }
    }

    @Test
    void testAdminBackupAndRestoreWorkflow() throws Exception {
        Account admin = accountRepository.findByUsername("admin").orElse(null);
        assertNotNull(admin);

        // 1. Verify /admin dashboard renders Backup & Restore card
        mockMvc.perform(get("/admin")
                .sessionAttr("ACCOUNT_ID", admin.getId())
                .sessionAttr("ACCOUNT_ROLE", "ADMIN")
                .sessionAttr("ACCOUNT_NAME", admin.getFullName()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("/admin/backup/download")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("មជ្ឈមណ្ឌល Backup & Restore មូលដ្ឋានទិន្នន័យ")));

        // 2. Verify /admin/backup/download generates valid SQL backup
        var mvcResult = mockMvc.perform(get("/admin/backup/download")
                .sessionAttr("ACCOUNT_ID", admin.getId())
                .sessionAttr("ACCOUNT_ROLE", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=")))
                .andReturn();

        String sqlDump = mvcResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertNotNull(sqlDump);
        assertTrue(sqlDump.contains("Class Management System 2027 - Full Database Backup"));
        assertTrue(sqlDump.contains("INSERT INTO"));

        // 3. Verify Non-Admin teacher (Teacher1001) is forbidden from downloading backup
        Account somuny = accountRepository.findByUsername("Teacher1001").orElse(null);
        assertNotNull(somuny);
        mockMvc.perform(get("/admin/backup/download")
                .sessionAttr("ACCOUNT_ID", somuny.getId())
                .sessionAttr("ACCOUNT_ROLE", somuny.getRole()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
