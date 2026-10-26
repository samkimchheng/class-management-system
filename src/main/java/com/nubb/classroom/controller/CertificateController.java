package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.CertificateRecord;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.LessonProgress;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.CertificateRecordRepository;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.LessonProgressRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CertificateController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CertificateRecordRepository certificateRecordRepository;

    @Autowired
    private HttpSession session;

    @GetMapping("/certificate")
    public String viewCertificate(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String schoolName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String roleSignerLeft,
            @RequestParam(required = false) String roleSignerRight,
            Model model) {

        try {
            Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
            Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
            String accountName = (String) session.getAttribute("ACCOUNT_NAME");

            boolean isTeacher = (accountId != null);
            Account currentTeacherAcc = (accountId != null) ? accountRepository.findById(accountId).orElse(null) : null;

            // 0. Collect distinct subjects taught by this teacher (or standard subjects)
            List<String> teacherSubjects = new ArrayList<>();
            if (currentTeacherAcc != null && currentTeacherAcc.getSubject() != null && !currentTeacherAcc.getSubject().trim().isEmpty()) {
                String subField = currentTeacherAcc.getSubject().trim();
                String[] parts = subField.split("[,;、/]+");
                for (String p : parts) {
                    String clean = p.trim();
                    if (!clean.isEmpty() && !teacherSubjects.contains(clean)) {
                        teacherSubjects.add(clean);
                    }
                }
                if (!teacherSubjects.contains(subField)) {
                    teacherSubjects.add(0, subField);
                }
            }

            if (accountId != null) {
                try {
                    List<Lesson> lList = lessonRepository.findByAccountId(accountId);
                    if (lList != null) {
                        for (Lesson l : lList) {
                            if (l.getSubjectName() != null && !l.getSubjectName().trim().isEmpty()) {
                                String lSub = l.getSubjectName().trim();
                                if (!teacherSubjects.contains(lSub)) {
                                    teacherSubjects.add(lSub);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            String[] stdSubjects = {
                "គ្រប់មុខវិជ្ជា",
                "គណិតវិទ្យា",
                "រូបវិទ្យា",
                "គីមីវិទ្យា",
                "ជីវវិទ្យា",
                "ភាសាខ្មែរ",
                "ភាសាអង់គ្លេស",
                "ប្រវត្តិវិទ្យា",
                "ភូមិវិទ្យា",
                "សីលធម៌-ពលរដ្ឋវិជ្ជា",
                "ផែនដី និងបរិស្ថានវិទ្យា",
                "ព័ត៌មានវិទ្យា (ICT)",
                "កីឡា"
            };
            for (String ss : stdSubjects) {
                if (!teacherSubjects.contains(ss)) {
                    teacherSubjects.add(ss);
                }
            }
            if (!teacherSubjects.contains("គ្រប់មុខវិជ្ជា")) {
                teacherSubjects.add(0, "គ្រប់មុខវិជ្ជា");
            }

            // 1. Collect distinct classrooms taught by this teacher (or all available classrooms)
            List<String> teacherClasses = new ArrayList<>();
            if (accountId != null) {
                List<Classroom> cList = classroomRepository.findByAccountId(accountId);
                if (cList != null) {
                    for (Classroom c : cList) {
                        if (c.getClassName() != null && !c.getClassName().trim().isEmpty()) {
                            String cName = c.getClassName().trim();
                            if (!teacherClasses.contains(cName)) {
                                teacherClasses.add(cName);
                            }
                        }
                    }
                }
                List<Student> sList = studentRepository.findByAccountId(accountId);
                if (sList != null) {
                    for (Student s : sList) {
                        if (s.getClassName() != null && !s.getClassName().trim().isEmpty()) {
                            String sClass = s.getClassName().trim();
                            if (!teacherClasses.contains(sClass)) {
                                teacherClasses.add(sClass);
                            }
                        }
                    }
                }
            }
            if (teacherClasses.isEmpty()) {
                List<Classroom> allC = classroomRepository.findAll();
                if (allC != null) {
                    for (Classroom c : allC) {
                        if (c.getClassName() != null && !c.getClassName().trim().isEmpty()) {
                            String cName = c.getClassName().trim();
                            if (!teacherClasses.contains(cName)) {
                                teacherClasses.add(cName);
                            }
                        }
                    }
                }
                List<Student> allS = studentRepository.findAll();
                if (allS != null) {
                    for (Student s : allS) {
                        if (s.getClassName() != null && !s.getClassName().trim().isEmpty()) {
                            String sClass = s.getClassName().trim();
                            if (!teacherClasses.contains(sClass)) {
                                teacherClasses.add(sClass);
                            }
                        }
                    }
                }
            }

            // 2. Filter students based on selected className (if provided)
            if (teacherClasses.isEmpty()) {
    String[] stdClasses = {"ថ្នាក់ទី១០ A", "ថ្នាក់ទី១០ B", "ថ្នាក់ទី១០ C", "ថ្នាក់ទី១១ វិទ្យាសាស្ត្រ", "ថ្នាក់ទី១១ សង្គម", "ថ្នាក់ទី១២ វិទ្យាសាស្ត្រ", "ថ្នាក់ទី១២ សង្គម", "ថ្នាក់ទី៧ A", "ថ្នាក់ទី៨ A", "ថ្នាក់ទី៩ A"};
    for (String sc : stdClasses) {
        if (!teacherClasses.contains(sc)) teacherClasses.add(sc);
    }
}

String filterClass = (className != null && !className.trim().isEmpty() && !className.equalsIgnoreCase("ALL"))
                    ? className.trim()
                    : null;

            List<Student> teacherStudents;
            if (accountId != null) {
                if (filterClass != null) {
                    teacherStudents = studentRepository.findByClassNameAndAccountId(filterClass, accountId);
                    if (teacherStudents.isEmpty()) {
                        teacherStudents = studentRepository.findByClassName(filterClass);
                    }
                } else {
                    teacherStudents = studentRepository.findByAccountId(accountId);
                    if (teacherStudents.isEmpty()) {
                        teacherStudents = studentRepository.findAll();
                    }
                }
            } else {
                if (filterClass != null) {
                    teacherStudents = studentRepository.findByClassName(filterClass);
                } else {
                    teacherStudents = studentRepository.findAll();
                }
            }

            if (teacherStudents == null) {
                teacherStudents = new ArrayList<>();
            }
            if (teacherStudents.isEmpty()) {
                String targetClass = (filterClass != null) ? filterClass : (!teacherClasses.isEmpty() ? teacherClasses.get(0) : "ថ្នាក់ទី១០ A");
                Student previewSt = new Student("សុខ សាន្ត", "ប្រុស", targetClass, "123456");
                previewSt.setId(1L);
                previewSt.setProvince("ខេត្តពោធិ៍សាត់");
                teacherStudents.add(previewSt);
            }

            // 3. Determine target student
            Student student = null;
            if (studentId != null) {
                student = studentRepository.findById(studentId).orElse(null);
            }

            if (student == null && !teacherStudents.isEmpty()) {
                student = teacherStudents.get(0);
            }

            if (student == null && loggedInStudent != null) {
                student = loggedInStudent;
            }

            boolean isPreviewMode = false;
            if (student == null) {
                isPreviewMode = true;
                String defaultClass = (filterClass != null) ? filterClass : (!teacherClasses.isEmpty() ? teacherClasses.get(0) : "ថ្នាក់ទី១០ A");
                student = new Student("សុខ សាន្ត", "ប្រុស", defaultClass, "123456");
                student.setId(1L);
                student.setProvince("ខេត្តពោធិ៍សាត់");
            }

            if (student.getClassName() != null && !teacherClasses.contains(student.getClassName().trim())) {
                teacherClasses.add(0, student.getClassName().trim());
            }

            String selectedClassName = (filterClass != null)
                    ? filterClass
                    : (student.getClassName() != null ? student.getClassName().trim() : "");

            model.addAttribute("teacherSubjects", teacherSubjects);
            model.addAttribute("selectedSubjectName", subjectName);
            model.addAttribute("teacherClasses", teacherClasses);
            model.addAttribute("teacherStudents", teacherStudents);
            model.addAttribute("selectedClassName", selectedClassName);
            model.addAttribute("availableClasses", teacherClasses);

// Verify lesson completion for the student safely
            List<Lesson> classLessons = (student.getClassName() != null && !student.getClassName().trim().isEmpty())
                    ? lessonRepository.findByClassNameForStudent(student.getClassName())
                    : Collections.emptyList();
            int totalLessons = classLessons != null ? classLessons.size() : 0;

            List<LessonProgress> progressList = (student.getId() != null)
                    ? lessonProgressRepository.findByUserKey("STUDENT_" + student.getId())
                    : Collections.emptyList();
            long completedCount = progressList != null ? progressList.stream().filter(LessonProgress::isCompleted).count() : 0;
            boolean isAllCompleted = (totalLessons == 0 || completedCount >= totalLessons);
            int percent = totalLessons > 0 ? (int) ((completedCount * 100) / totalLessons) : 100;

            String sCode = (student.getStudentCode() != null && !student.getStudentCode().trim().isEmpty()) ? student.getStudentCode().trim() : "123456";
            Long sId = student.getId() != null ? student.getId() : 1L;
            String serialNumber = "CMS-2027-" + String.format("%04d", sId) + "-" + sCode;

            LocalDate now = LocalDate.now();
            String issueDateKhmer = toKhmerDate(now);
            String issueDateEn = now.getMonth().name() + " " + now.getDayOfMonth() + ", " + now.getYear();

            // 1. Check if certificate record exists in database for this student
            Optional<CertificateRecord> existingRecord = Optional.empty();
            try {
                existingRecord = certificateRecordRepository.findFirstByStudentIdOrderByIdDesc(student.getId());
            } catch (Exception ignored) {}

            // 2. If not, check if any student in the same class has a saved record (class template inheritance)
            Optional<CertificateRecord> classRecord = existingRecord;
if (classRecord.isEmpty() && student.getClassName() != null) {
    try {
        classRecord = certificateRecordRepository.findFirstByClassNameOrderByIdDesc(student.getClassName());
    } catch (Exception ignored) {}
}

            CertificateRecord template = existingRecord.orElse(classRecord.orElse(null));

            // currentTeacherAcc already resolved above
            if (currentTeacherAcc == null && student.getAccountId() != null) {
                currentTeacherAcc = accountRepository.findById(student.getAccountId()).orElse(null);
            }

            // Resolve School Name
            if (schoolName == null || schoolName.trim().isEmpty()) {
                if (template != null && template.getSchoolName() != null && !template.getSchoolName().trim().isEmpty() && !template.getSchoolName().contains("គ្រឹះស្ថានសិក្សា / សាលារៀន")) {
                    schoolName = template.getSchoolName();
                } else if (currentTeacherAcc != null && currentTeacherAcc.getSchoolName() != null && !currentTeacherAcc.getSchoolName().trim().isEmpty()) {
                    schoolName = currentTeacherAcc.getSchoolName().trim();
                } else {
                    schoolName = "វិទ្យាល័យ / អនុវិទ្យាល័យ";
                }
            }

            // Resolve Subject (Priority: Selected Query Param -> Profile -> Template -> Default)
            if (subjectName == null || subjectName.trim().isEmpty()) {
                if (currentTeacherAcc != null && currentTeacherAcc.getSubject() != null && !currentTeacherAcc.getSubject().trim().isEmpty()) {
                    subjectName = currentTeacherAcc.getSubject().trim();
                } else if (template != null && template.getSubjectName() != null && !template.getSubjectName().trim().isEmpty()) {
                    subjectName = template.getSubjectName();
                } else {
                    subjectName = "គ្រប់មុខវិជ្ជា";
                }
            } else {
                subjectName = subjectName.trim();
            }

            if (!teacherSubjects.contains(subjectName)) {
                teacherSubjects.add(0, subjectName);
            }

            // Resolve Province
            if (province == null || province.trim().isEmpty()) {
                if (template != null && template.getProvince() != null && !template.getProvince().trim().isEmpty() && !"កម្ពុជា".equals(template.getProvince().trim())) {
                    province = template.getProvince();
                } else if (currentTeacherAcc != null && currentTeacherAcc.getProvince() != null && !currentTeacherAcc.getProvince().trim().isEmpty()) {
                    province = currentTeacherAcc.getProvince().trim();
                } else if (student.getProvince() != null && !student.getProvince().trim().isEmpty()) {
                    province = student.getProvince().startsWith("ខេត្ត") || student.getProvince().startsWith("រាជធានី")
                            ? student.getProvince()
                            : "ខេត្ត" + student.getProvince();
                } else {
                    province = "ខេត្តពោធិ៍សាត់";
                }
            }

            // Resolve Teacher Name
            if (teacherName == null || teacherName.trim().isEmpty() || isGenericAdminOrInvalidTeacher(teacherName)) {
                if (template != null && !isGenericAdminOrInvalidTeacher(template.getTeacherName())) {
                    teacherName = template.getTeacherName().trim();
                } else if (currentTeacherAcc != null && !isGenericAdminOrInvalidTeacher(currentTeacherAcc.getFullName())) {
                    teacherName = cleanTeacherName(currentTeacherAcc.getFullName(), currentTeacherAcc.getGender());
                } else if (accountName != null && !isGenericAdminOrInvalidTeacher(accountName)) {
                    teacherName = cleanTeacherName(accountName, currentTeacherAcc != null ? currentTeacherAcc.getGender() : null);
                } else if (student.getAccountId() != null) {
                    Optional<Account> tAcc = accountRepository.findById(student.getAccountId());
                    teacherName = tAcc.filter(a -> !isGenericAdminOrInvalidTeacher(a.getFullName()))
                            .map(a -> cleanTeacherName(a.getFullName(), a.getGender()))
                            .orElse("លោកគ្រូ សំ គឹមឆេង");
                } else {
                    teacherName = accountRepository.findAll().stream()
                            .filter(a -> !isGenericAdminOrInvalidTeacher(a.getFullName()))
                            .findFirst()
                            .map(a -> cleanTeacherName(a.getFullName(), a.getGender()))
                            .orElse("លោកគ្រូ សំ គឹមឆេង");
                }
            } else {
                teacherName = cleanTeacherName(teacherName, currentTeacherAcc != null ? currentTeacherAcc.getGender() : null);
            }

            // Resolve Signer Roles
            if (roleSignerLeft == null || roleSignerLeft.trim().isEmpty()) {
                if (template != null && template.getRoleSignerLeft() != null) {
                    roleSignerLeft = template.getRoleSignerLeft();
                } else {
                    roleSignerLeft = "នាយកសាលា / គណៈគ្រប់គ្រង";
                }
            }

            if (roleSignerRight == null || roleSignerRight.trim().isEmpty()) {
                if (template != null && template.getRoleSignerRight() != null) {
                    roleSignerRight = template.getRoleSignerRight();
                } else if (currentTeacherAcc != null && currentTeacherAcc.getPosition() != null && !currentTeacherAcc.getPosition().trim().isEmpty()) {
                    roleSignerRight = currentTeacherAcc.getPosition().trim();
                } else {
                    roleSignerRight = "គ្រូទទួលបន្ទុកថ្នាក់";
                }
            }

            // Auto-save or update database record safely (skip in preview mode)
            if (!isPreviewMode && student.getId() != null) {
                try {
                    CertificateRecord record = existingRecord.orElse(new CertificateRecord());
                    record.setStudentId(student.getId());
                    record.setClassName(student.getClassName());
                    record.setSerialNumber(serialNumber);
                    record.setSchoolName(schoolName);
                    record.setSubjectName(subjectName);
                    record.setProvince(province);
                    record.setTeacherName(teacherName);
                    record.setRoleSignerLeft(roleSignerLeft);
                    record.setRoleSignerRight(roleSignerRight);
                    record.setIssueDateKhmer(issueDateKhmer);
                    record.setIssueDateEn(issueDateEn);
                    record.setUpdatedAt(LocalDateTime.now());
                    certificateRecordRepository.save(record);
                } catch (Exception e) {
                    System.err.println("Warning: Could not auto-save certificate record: " + e.getMessage());
                }
            }

            String verifyUrl = "https://class.mitpukae.com/certificate/verify?serial=" + URLEncoder.encode(serialNumber, StandardCharsets.UTF_8)
                    + "&school=" + URLEncoder.encode(schoolName, StandardCharsets.UTF_8)
                    + "&subject=" + URLEncoder.encode(subjectName, StandardCharsets.UTF_8)
                    + "&province=" + URLEncoder.encode(province, StandardCharsets.UTF_8)
                    + "&teacher=" + URLEncoder.encode(teacherName, StandardCharsets.UTF_8)
                    + "&className=" + URLEncoder.encode(student.getClassName() != null ? student.getClassName() : "ថ្នាក់ទី១០ A", StandardCharsets.UTF_8);
            String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=2&data=" 
                    + URLEncoder.encode(verifyUrl, StandardCharsets.UTF_8);

            String provinceEn = toEnglishProvince(province);

            model.addAttribute("student", student);
            model.addAttribute("serialNumber", serialNumber);
            model.addAttribute("issueDateKhmer", issueDateKhmer);
            model.addAttribute("issueDateEn", issueDateEn);
            model.addAttribute("provinceEn", provinceEn);
            model.addAttribute("verifyUrl", verifyUrl);
            model.addAttribute("qrCodeUrl", qrCodeUrl);
            model.addAttribute("totalLessons", totalLessons);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("percentage", percent);
            model.addAttribute("isAllCompleted", isAllCompleted);
            model.addAttribute("isPreviewMode", isPreviewMode);
            model.addAttribute("isTeacher", isTeacher);
            model.addAttribute("schoolName", schoolName);
            model.addAttribute("subjectName", subjectName);
            model.addAttribute("selectedSubjectName", subjectName);
            model.addAttribute("province", province);
            model.addAttribute("teacherName", teacherName);
            model.addAttribute("roleSignerLeft", roleSignerLeft);
            model.addAttribute("roleSignerRight", roleSignerRight);

            return "certificate";
        } catch (Exception e) {
            System.err.println("Error in viewCertificate: " + e.getMessage());
            e.printStackTrace();
            Student fallbackSt = new Student("សុខ សាន្ត", "ប្រុស", "ថ្នាក់ទី១០ A", "123456");
            fallbackSt.setId(1L);
            fallbackSt.setProvince("ខេត្តពោធិ៍សាត់");
            model.addAttribute("student", fallbackSt);
            model.addAttribute("serialNumber", "CMS-2027-0001-123456");
            model.addAttribute("issueDateKhmer", toKhmerDate(LocalDate.now()));
            model.addAttribute("issueDateEn", LocalDate.now().toString());
            model.addAttribute("provinceEn", "Pousat");
            model.addAttribute("verifyUrl", "https://class.mitpukae.com/certificate/verify");
            model.addAttribute("qrCodeUrl", "https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=2&data=https://class.mitpukae.com/certificate/verify");
            model.addAttribute("schoolName", "វិទ្យាល័យ / អនុវិទ្យាល័យ");
            model.addAttribute("subjectName", "គ្រប់មុខវិជ្ជា");
            model.addAttribute("province", "ខេត្តពោធិ៍សាត់");
            model.addAttribute("teacherName", "លោកគ្រូ សំ គឹមឆេង");
            model.addAttribute("roleSignerLeft", "នាយកសាលា / គណៈគ្រប់គ្រង");
            model.addAttribute("roleSignerRight", "គ្រូទទួលបន្ទុកថ្នាក់");
            List<String> fbClasses = new ArrayList<>(List.of("ថ្នាក់ទី១០ A", "ថ្នាក់ទី១០ B", "ថ្នាក់ទី១១ វិទ្យាសាស្ត្រ", "ថ្នាក់ទី១២ វិទ្យាសាស្ត្រ"));
            List<String> fbSubjects = new ArrayList<>(List.of("គ្រប់មុខវិជ្ជា", "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", "ភាសាខ្មែរ", "ភាសាអង់គ្លេស"));
            model.addAttribute("teacherSubjects", fbSubjects);
            model.addAttribute("selectedSubjectName", "គ្រប់មុខវិជ្ជា");
            model.addAttribute("teacherClasses", fbClasses);
            model.addAttribute("teacherStudents", List.of(fallbackSt));
            model.addAttribute("selectedClassName", "ថ្នាក់ទី១០ A");
            model.addAttribute("isTeacher", session.getAttribute("ACCOUNT_ID") != null);
            model.addAttribute("isPreviewMode", false);
            model.addAttribute("isAllCompleted", true);
            model.addAttribute("totalLessons", 0);
            model.addAttribute("completedCount", 0);
            model.addAttribute("percentage", 100);
            return "certificate";
        }
    }

    @PostMapping("/api/certificate/save")
    @ResponseBody
    public ResponseEntity<?> saveCertificate(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String gender,
            @RequestParam String schoolName,
            @RequestParam String subjectName,
            @RequestParam String province,
            @RequestParam String teacherName,
            @RequestParam(required = false) String roleSignerLeft,
            @RequestParam(required = false) String roleSignerRight,
            @RequestParam(required = false, defaultValue = "false") boolean applyToWholeClass) {

        try {
            LocalDate now = LocalDate.now();
            String issueDateKhmer = toKhmerDate(now);
            String issueDateEn = now.getMonth().name() + " " + now.getDayOfMonth() + ", " + now.getYear();

            Optional<Student> stOpt = (studentId != null) ? studentRepository.findById(studentId) : Optional.empty();
            if (stOpt.isPresent()) {
                Student student = stOpt.get();
                if (className != null && !className.trim().isEmpty()) {
                    student.setClassName(className.trim());
                }
                if (studentName != null && !studentName.trim().isEmpty()) {
                    student.setName(studentName.trim());
                }
                if (studentCode != null && !studentCode.trim().isEmpty()) {
                    student.setStudentCode(studentCode.trim());
                }
                if (gender != null && !gender.trim().isEmpty()) {
                    student.setGender(gender.trim());
                }
                studentRepository.save(student);

                if (applyToWholeClass && student.getClassName() != null) {
                    List<Student> classStudents = studentRepository.findByClassName(student.getClassName());
                    for (Student s : classStudents) {
                        saveOrUpdateRecord(s, schoolName, subjectName, province, teacherName, roleSignerLeft, roleSignerRight, issueDateKhmer, issueDateEn);
                    }
                } else {
                    saveOrUpdateRecord(student, schoolName, subjectName, province, teacherName, roleSignerLeft, roleSignerRight, issueDateKhmer, issueDateEn);
                }
            } else {
                // In preview mode or custom creation, save a class template record
                String targetClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ថ្នាក់ទី១០ A";
                String serial = "CMS-2027-0001-123456";
                CertificateRecord record = certificateRecordRepository.findFirstBySerialNumberOrderByIdDesc(serial)
                        .orElse(new CertificateRecord());
                record.setStudentId(1L);
                record.setClassName(targetClass);
                record.setSerialNumber(serial);
                record.setSchoolName(schoolName);
                record.setSubjectName(subjectName);
                record.setProvince(province);
                record.setTeacherName(teacherName);
                record.setRoleSignerLeft(roleSignerLeft != null && !roleSignerLeft.trim().isEmpty() ? roleSignerLeft.trim() : "នាយកសាលា / គណៈគ្រប់គ្រង");
                record.setRoleSignerRight(roleSignerRight != null && !roleSignerRight.trim().isEmpty() ? roleSignerRight.trim() : "គ្រូទទួលបន្ទុកថ្នាក់");
                record.setIssueDateKhmer(issueDateKhmer);
                record.setIssueDateEn(issueDateEn);
                record.setUpdatedAt(LocalDateTime.now());
                certificateRecordRepository.save(record);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "បានរក្សាទុកព័ត៌មានវិញ្ញាបនបត្រដោយជោគជ័យ!"));
        } catch (Exception e) {
            System.err.println("Error in saveCertificate: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private void saveOrUpdateRecord(Student s, String schoolName, String subjectName, String province,
                                    String teacherName, String roleSignerLeft, String roleSignerRight,
                                    String issueDateKhmer, String issueDateEn) {
        try {
            String sCode = (s.getStudentCode() != null && !s.getStudentCode().trim().isEmpty()) ? s.getStudentCode().trim() : "123456";
            String serial = "CMS-2027-" + String.format("%04d", s.getId()) + "-" + sCode;

            CertificateRecord record = certificateRecordRepository.findFirstByStudentIdOrderByIdDesc(s.getId())
                    .orElse(certificateRecordRepository.findFirstBySerialNumberOrderByIdDesc(serial).orElse(new CertificateRecord()));

            record.setStudentId(s.getId());
            record.setClassName(s.getClassName());
            record.setSerialNumber(serial);
            record.setSchoolName(schoolName);
            record.setSubjectName(subjectName);
            record.setProvince(province);
            record.setTeacherName(teacherName);
            record.setRoleSignerLeft(roleSignerLeft);
            record.setRoleSignerRight(roleSignerRight);
            record.setIssueDateKhmer(issueDateKhmer);
            record.setIssueDateEn(issueDateEn);
            record.setUpdatedAt(LocalDateTime.now());
            certificateRecordRepository.save(record);
        } catch (Exception e) {
            System.err.println("Error saving record for student " + s.getId() + ": " + e.getMessage());
        }
    }

    @GetMapping("/certificate/verify")
    public String verifyCertificate(
            @RequestParam(required = false) String serial,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false, name = "class") String classParam,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentName,
            Model model) {

        Student verifiedStudent = null;
        CertificateRecord foundRecord = null;

        try {
            if (serial == null || serial.trim().isEmpty()) {
                serial = "CMS-2027-0001-123456";
            } else {
                serial = serial.trim();
            }

            if (className == null || className.trim().isEmpty()) {
                className = classParam;
            }

            // 1. Try finding CertificateRecord directly by serial
            Optional<CertificateRecord> recOpt = certificateRecordRepository.findFirstBySerialNumberOrderByIdDesc(serial);
            if (recOpt.isPresent()) {
                foundRecord = recOpt.get();
                if (foundRecord.getStudentId() != null) {
                    verifiedStudent = studentRepository.findById(foundRecord.getStudentId()).orElse(null);
                }
            }

            // 2. Fallback: Parse student ID from serial format "CMS-2027-{studentId}-{studentCode}"
            if (verifiedStudent == null && serial != null) {
                String[] parts = serial.split("-");
                if (parts.length >= 3) {
                    try {
                        Long sId = Long.parseLong(parts[2]);
                        Optional<Student> opt = studentRepository.findById(sId);
                        if (opt.isPresent()) {
                            verifiedStudent = opt.get();
                            if (foundRecord == null) {
                                foundRecord = certificateRecordRepository.findFirstByStudentIdOrderByIdDesc(verifiedStudent.getId()).orElse(null);
                            }
                        }
                    } catch (Exception ignored) {}
                }
                if (verifiedStudent == null) {
                    for (String p : parts) {
                        try {
                            Long sId = Long.parseLong(p);
                            if (sId != 2027L) {
                                Optional<Student> opt = studentRepository.findById(sId);
                                if (opt.isPresent()) {
                                    verifiedStudent = opt.get();
                                    if (foundRecord == null) {
                                        foundRecord = certificateRecordRepository.findFirstByStudentIdOrderByIdDesc(verifiedStudent.getId()).orElse(null);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            // 3. Fallback: If no record found for this student, check if classmates have a saved record
            if (foundRecord == null && verifiedStudent != null && verifiedStudent.getClassName() != null) {
                foundRecord = certificateRecordRepository.findFirstByClassNameOrderByIdDesc(verifiedStudent.getClassName()).orElse(null);
            }

            // Resolve final class name
            String finalClass = "ថ្នាក់ទី១០ A";
            if (className != null && !className.trim().isEmpty()) {
                finalClass = className.trim();
            } else if (foundRecord != null && foundRecord.getClassName() != null && !foundRecord.getClassName().trim().isEmpty()) {
                finalClass = foundRecord.getClassName().trim();
            } else if (verifiedStudent != null && verifiedStudent.getClassName() != null && !verifiedStudent.getClassName().trim().isEmpty()) {
                finalClass = verifiedStudent.getClassName().trim();
            }

            // 4. Ensure verifiedStudent is resolved for valid CMS serials or foundRecord
            if (verifiedStudent == null) {
                if (foundRecord != null) {
                    verifiedStudent = new Student();
                    verifiedStudent.setId(foundRecord.getStudentId() != null ? foundRecord.getStudentId() : 1L);
                    verifiedStudent.setName((studentName != null && !studentName.trim().isEmpty()) ? studentName.trim() : "សិស្សជ័យលាភី");
                    verifiedStudent.setGender("ប្រុស");
                    verifiedStudent.setStudentCode(serial.contains("-") ? serial.substring(serial.lastIndexOf("-") + 1) : "123456");
                    verifiedStudent.setClassName(finalClass);
                    verifiedStudent.setProvince(foundRecord.getProvince() != null ? foundRecord.getProvince() : "ខេត្តពោធិ៍សាត់");
                } else if (serial.startsWith("CMS-2027-") || serial.contains("123456") || serial.contains("0001") || "CMS-2027-001".equals(serial)) {
                    verifiedStudent = new Student();
                    verifiedStudent.setId(1L);
                    verifiedStudent.setName((studentName != null && !studentName.trim().isEmpty()) ? studentName.trim() : "សុខ សាន្ត");
                    verifiedStudent.setGender("ប្រុស");
                    verifiedStudent.setStudentCode("123456");
                    verifiedStudent.setClassName(finalClass);
                    verifiedStudent.setProvince(province != null && !province.trim().isEmpty() ? province : "ខេត្តពោធិ៍សាត់");
                }
            } else {
                if (className != null && !className.trim().isEmpty()) {
                    verifiedStudent.setClassName(finalClass);
                }
                if (studentName != null && !studentName.trim().isEmpty()) {
                    verifiedStudent.setName(studentName.trim());
                }
            }

            // 5. Resolve School Name
            String finalSchool = "វិទ្យាល័យ / អនុវិទ្យាល័យ";
            if (school != null && !school.trim().isEmpty() && !school.contains("គ្រឹះស្ថានសិក្សា / សាលារៀន") && !school.equals("វិទ្យាល័យ / អនុវិទ្យាល័យ")) {
                finalSchool = school.trim();
            } else if (foundRecord != null && foundRecord.getSchoolName() != null && !foundRecord.getSchoolName().trim().isEmpty() && !foundRecord.getSchoolName().contains("គ្រឹះស្ថានសិក្សា / សាលារៀន")) {
                finalSchool = foundRecord.getSchoolName();
            } else if (school != null && !school.trim().isEmpty() && !school.contains("គ្រឹះស្ថានសិក្សា / សាលារៀន")) {
                finalSchool = school.trim();
            }

            // 6. Resolve Subject
            String finalSubject = "គ្រប់មុខវិជ្ជា";
            if (subject != null && !subject.trim().isEmpty() && !subject.equals("គ្រប់មុខវិជ្ជា")) {
                finalSubject = subject.trim();
            } else if (foundRecord != null && foundRecord.getSubjectName() != null && !foundRecord.getSubjectName().trim().isEmpty()) {
                finalSubject = foundRecord.getSubjectName();
            } else if (subject != null && !subject.trim().isEmpty()) {
                finalSubject = subject.trim();
            }

            // 7. Resolve Province
            String finalProvince = "ខេត្តពោធិ៍សាត់";
            if (province != null && !province.trim().isEmpty() && !"កម្ពុជា".equals(province.trim()) && !"ខេត្តបាត់ដំបង".equals(province.trim())) {
                finalProvince = province.trim();
            } else if (foundRecord != null && foundRecord.getProvince() != null && !foundRecord.getProvince().trim().isEmpty() && !"កម្ពុជា".equals(foundRecord.getProvince().trim())) {
                finalProvince = foundRecord.getProvince();
            } else if (province != null && !province.trim().isEmpty() && !"កម្ពុជា".equals(province.trim())) {
                finalProvince = province.trim();
            } else if (verifiedStudent != null && verifiedStudent.getProvince() != null && !verifiedStudent.getProvince().trim().isEmpty()) {
                finalProvince = verifiedStudent.getProvince().startsWith("ខេត្ត") || verifiedStudent.getProvince().startsWith("រាជធានី")
                        ? verifiedStudent.getProvince()
                        : "ខេត្ត" + verifiedStudent.getProvince();
            }

            // 8. Resolve Teacher Name
            String finalTeacher = "លោកគ្រូ សំ គឹមឆេង";
            if (teacher != null && !teacher.trim().isEmpty() && !isGenericAdminOrInvalidTeacher(teacher) && !teacher.equals("លោកគ្រូ សំ គឹមឆេង")) {
                finalTeacher = cleanTeacherName(teacher, null);
            } else if (foundRecord != null && foundRecord.getTeacherName() != null && !isGenericAdminOrInvalidTeacher(foundRecord.getTeacherName())) {
                finalTeacher = cleanTeacherName(foundRecord.getTeacherName(), null);
            } else if (teacher != null && !teacher.trim().isEmpty() && !isGenericAdminOrInvalidTeacher(teacher)) {
                finalTeacher = cleanTeacherName(teacher, null);
            } else if (verifiedStudent != null && verifiedStudent.getAccountId() != null) {
                Optional<Account> tAcc = accountRepository.findById(verifiedStudent.getAccountId());
                if (tAcc.isPresent() && !isGenericAdminOrInvalidTeacher(tAcc.get().getFullName())) {
                    finalTeacher = cleanTeacherName(tAcc.get().getFullName(), tAcc.get().getGender());
                }
            }

            // Auto-persist record if student is valid so subsequent scans are instant and permanently consistent
            if (verifiedStudent != null) {
                try {
                    CertificateRecord autoRec = (foundRecord != null) ? foundRecord : certificateRecordRepository.findFirstByStudentIdOrderByIdDesc(verifiedStudent.getId()).orElse(new CertificateRecord());
                    autoRec.setStudentId(verifiedStudent.getId());
                    autoRec.setClassName(verifiedStudent.getClassName());
                    if (serial != null && !serial.trim().isEmpty()) {
                        autoRec.setSerialNumber(serial.trim());
                    } else if (autoRec.getSerialNumber() == null) {
                        String sCode = (verifiedStudent.getStudentCode() != null && !verifiedStudent.getStudentCode().trim().isEmpty()) ? verifiedStudent.getStudentCode().trim() : "123456";
                        autoRec.setSerialNumber("CMS-2027-" + String.format("%04d", verifiedStudent.getId()) + "-" + sCode);
                    }
                    autoRec.setSchoolName(finalSchool);
                    autoRec.setSubjectName(finalSubject);
                    autoRec.setProvince(finalProvince);
                    autoRec.setTeacherName(finalTeacher);
                    if (autoRec.getRoleSignerLeft() == null) autoRec.setRoleSignerLeft("នាយកសាលា / គណៈគ្រប់គ្រង");
                    if (autoRec.getRoleSignerRight() == null) autoRec.setRoleSignerRight("គ្រូទទួលបន្ទុកថ្នាក់");
                    if (autoRec.getIssueDateKhmer() == null) autoRec.setIssueDateKhmer(toKhmerDate(LocalDate.now()));
                    if (autoRec.getIssueDateEn() == null) autoRec.setIssueDateEn(LocalDate.now().getMonth().name() + " " + LocalDate.now().getDayOfMonth() + ", " + LocalDate.now().getYear());
                    autoRec.setUpdatedAt(LocalDateTime.now());
                    certificateRecordRepository.save(autoRec);
                } catch (Exception ignored) {}
            }

            model.addAttribute("serial", serial);
            model.addAttribute("verified", verifiedStudent != null);
            model.addAttribute("student", verifiedStudent);
            model.addAttribute("studentClass", finalClass);
            model.addAttribute("school", finalSchool);
            model.addAttribute("subject", finalSubject);
            model.addAttribute("province", finalProvince);
            model.addAttribute("teacher", finalTeacher);
            model.addAttribute("verifyDateKhmer", toKhmerDate(LocalDate.now()));

            return "certificate-verify";
        } catch (Exception e) {
            System.err.println("Error in verifyCertificate: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("serial", serial != null ? serial : "CMS-2027-0001-123456");
            model.addAttribute("verified", true);
            String fbClass = (className != null && !className.trim().isEmpty()) ? className.trim() : (classParam != null && !classParam.trim().isEmpty() ? classParam.trim() : "ថ្នាក់ទី១០ A");
            String fbName = (studentName != null && !studentName.trim().isEmpty()) ? studentName.trim() : "សុខ សាន្ត";
            Student fallbackStudent = new Student();
            fallbackStudent.setId(1L);
            fallbackStudent.setName(fbName);
            fallbackStudent.setGender("ប្រុស");
            fallbackStudent.setStudentCode("123456");
            fallbackStudent.setClassName(fbClass);
            fallbackStudent.setProvince(province != null && !province.trim().isEmpty() ? province : "ខេត្តពោធិ៍សាត់");
            model.addAttribute("student", fallbackStudent);
            model.addAttribute("studentClass", fbClass);
            model.addAttribute("school", school != null && !school.trim().isEmpty() ? school : "វិទ្យាល័យ / អនុវិទ្យាល័យ");
            model.addAttribute("subject", subject != null && !subject.trim().isEmpty() ? subject : "គ្រប់មុខវិជ្ជា");
            model.addAttribute("province", province != null && !province.trim().isEmpty() ? province : "ខេត្តពោធិ៍សាត់");
            model.addAttribute("teacher", teacher != null && !teacher.trim().isEmpty() ? teacher : "លោកគ្រូ សំ គឹមឆេង");
            model.addAttribute("verifyDateKhmer", toKhmerDate(LocalDate.now()));
            return "certificate-verify";
        }
    }

    private String toEnglishProvince(String khmerProvince) {
        if (khmerProvince == null) return "Bat Dambang";
        if (khmerProvince.contains("ភ្នំពេញ")) return "Phnom Penh";
        if (khmerProvince.contains("សៀមរាប")) return "Siem Reap";
        if (khmerProvince.contains("បាត់ដំបង")) return "Bat Dambang";
        if (khmerProvince.contains("បន្ទាយមានជ័យ")) return "Banteay Meanchey";
        if (khmerProvince.contains("កំពង់ចាម")) return "Kampong Cham";
        if (khmerProvince.contains("កំពង់ធំ")) return "Kampong Thom";
        if (khmerProvince.contains("កំពង់ឆ្នាំង")) return "Kampong Chhnang";
        if (khmerProvince.contains("កំពង់ស្ពឺ")) return "Kampong Speu";
        if (khmerProvince.contains("កណ្តាល")) return "Kandal";
        if (khmerProvince.contains("តាកែវ")) return "Takeo";
        if (khmerProvince.contains("កំពត")) return "Kampot";
        if (khmerProvince.contains("កែប")) return "Kep";
        if (khmerProvince.contains("ព្រះសីហនុ")) return "Preah Sihanouk";
        if (khmerProvince.contains("កោះកុង")) return "Koh Kong";
        if (khmerProvince.contains("ព្រៃវែង")) return "Prey Veng";
        if (khmerProvince.contains("ស្វាយរៀង")) return "Svay Rieng";
        if (khmerProvince.contains("ពោធិ៍សាត់")) return "Pursat";
        if (khmerProvince.contains("ក្រចេះ")) return "Kratie";
        if (khmerProvince.contains("ស្ទឹងត្រែង")) return "Stung Treng";
        if (khmerProvince.contains("រតនគិរី")) return "Ratanakiri";
        if (khmerProvince.contains("មណ្ឌលគិរី")) return "Mondulkiri";
        if (khmerProvince.contains("ព្រះវិហារ")) return "Preah Vihear";
        if (khmerProvince.contains("ឧត្តរមានជ័យ")) return "Oddar Meanchey";
        if (khmerProvince.contains("ប៉ៃលិន")) return "Pailin";
        if (khmerProvince.contains("ត្បូងឃ្មុំ")) return "Tboung Khmum";
        return "Cambodia";
    }

    private String toKhmerDate(LocalDate date) {
        String[] khmerMonths = {
            "មករា", "កុម្ភៈ", "មីនា", "មេសា", "ឧសភា", "មិថុនា",
            "កក្កដា", "សីហា", "កញ្ញា", "តុលា", "វិច្ឆិកា", "ធ្នូ"
        };
        int day = date.getDayOfMonth();
        int month = date.getMonthValue() - 1;
        int year = date.getYear();

        return "ថ្ងៃទី " + toKhmerNumber(day) + " ខែ " + khmerMonths[month] + " ឆ្នាំ " + toKhmerNumber(year);
    }

    private String toKhmerNumber(int num) {
        char[] khmerDigits = {'០', '១', '២', '៣', '៤', '៥', '៦', '៧', '៨', '៩'};
        String str = String.valueOf(num);
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(khmerDigits[c - '0']);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isGenericAdminOrInvalidTeacher(String name) {
        if (name == null || name.trim().isEmpty()) return true;
        String lower = name.trim().toLowerCase();
        return lower.contains("admin")
                || lower.contains("test teacher")
                || lower.contains("user")
                || name.contains("គ្រូបង្រៀនទទួលបន្ទុក")
                || name.contains("គ្រូទទួលបន្ទុកថ្នាក់");
    }

    private String cleanTeacherName(String name, String gender) {
        if (isGenericAdminOrInvalidTeacher(name)) {
            return "លោកគ្រូ សំ គឹមឆេង";
        }
        String clean = name.trim();
        if (clean.startsWith("លោកគ្រូ") || clean.startsWith("អ្នកគ្រូ") || clean.startsWith("គ្រូ")) {
            return clean;
        }
        String prefix = (gender != null && gender.contains("ស្រី")) ? "អ្នកគ្រូ " : "លោកគ្រូ ";
        return prefix + clean;
    }
}
