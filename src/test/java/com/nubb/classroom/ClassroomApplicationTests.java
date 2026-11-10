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
        List<Student> students8D = studentRepository.findByClassNameAndAccountIdAndAcademicYear("ថ្នាក់ទី៨D", somuny.getId(), "២០២៦-២០២៧");
        if (students8D != null) studentRepository.deleteAll(students8D);
    }

    @Test
    void testDailyPlanWorkflowAndScheduleIntegration() throws Exception {
        Account kimchheng = accountRepository.findByUsername("samkimchheng").orElse(null);
        assertNotNull(kimchheng);

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

        // 4. Verify homepage dashboard has tomorrow daily plan model attributes
        mockMvc.perform(get("/")
                .sessionAttr("ACCOUNT_ID", kimchheng.getId())
                .sessionAttr("ACCOUNT_ROLE", kimchheng.getRole())
                .sessionAttr("ACCOUNT_NAME", kimchheng.getFullName())
                .sessionAttr("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tomorrowDateStr"))
                .andExpect(model().attributeExists("tomorrowClasses"));

        // Clean up test logs
        dailyTeachingLogRepository.deleteAll(logs);
    }
}
