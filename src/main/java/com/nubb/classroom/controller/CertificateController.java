package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.LessonProgress;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.LessonProgressRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private HttpSession session;

    @GetMapping("/certificate")
    public String viewCertificate(
            @RequestParam(required = false) Long studentId,
            Model model) {

        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");

        Student student = null;
        boolean isTeacher = (accountId != null);

        if (isTeacher) {
            // Teacher / Admin mode: can view specific student or choose from students list
            List<Student> teacherStudents = studentRepository.findByAccountId(accountId);
            if (teacherStudents.isEmpty()) {
                teacherStudents = studentRepository.findAll();
            }
            model.addAttribute("teacherStudents", teacherStudents);

            if (studentId != null) {
                student = studentRepository.findById(studentId).orElse(null);
            } else if (!teacherStudents.isEmpty()) {
                student = teacherStudents.get(0);
            }
        } else if (loggedInStudent != null) {
            student = loggedInStudent;
        }

        if (student == null) {
            return "redirect:/lessons";
        }

        // Verify lesson completion for the student
        List<Lesson> classLessons = lessonRepository.findByClassNameForStudent(student.getClassName());
        int totalLessons = classLessons.size();
        List<LessonProgress> progressList = lessonProgressRepository.findByUserKey("STUDENT_" + student.getId());
        long completedCount = progressList.stream().filter(LessonProgress::isCompleted).count();
        boolean isAllCompleted = totalLessons > 0 && completedCount >= totalLessons;

        // If a student tries to view certificate before completing, show pending page
        if (!isTeacher && !isAllCompleted) {
            model.addAttribute("student", student);
            model.addAttribute("totalLessons", totalLessons);
            model.addAttribute("completedCount", completedCount);
            int percent = totalLessons > 0 ? (int) ((completedCount * 100) / totalLessons) : 0;
            model.addAttribute("percentage", percent);
            return "certificate-pending";
        }

        // Generate Certificate Metadata
        LocalDate now = LocalDate.now();
        String serialNumber = "NUBB-CMS-2027-" + String.format("%04d", student.getId()) + "-" + student.getStudentCode();
        String issueDateKhmer = toKhmerDate(now);
        String issueDateEn = now.getMonth().name() + " " + now.getDayOfMonth() + ", " + now.getYear();

        String verifyUrl = "https://class.mitpukae.com/certificate/verify?serial=" + serialNumber;
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=" 
                + URLEncoder.encode(verifyUrl, StandardCharsets.UTF_8);

        model.addAttribute("student", student);
        model.addAttribute("serialNumber", serialNumber);
        model.addAttribute("issueDateKhmer", issueDateKhmer);
        model.addAttribute("issueDateEn", issueDateEn);
        model.addAttribute("verifyUrl", verifyUrl);
        model.addAttribute("qrCodeUrl", qrCodeUrl);
        model.addAttribute("totalLessons", totalLessons);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("schoolName", "សាកលវិទ្យាល័យជាតិបាត់ដំបង (NUBB)");
        model.addAttribute("teacherName", "លោកគ្រូ សំ គឹមឆេង");

        return "certificate";
    }

    @GetMapping("/certificate/verify")
    public String verifyCertificate(
            @RequestParam(required = false) String serial,
            Model model) {

        Student verifiedStudent = null;
        if (serial != null && serial.startsWith("NUBB-CMS-2027-")) {
            try {
                String[] parts = serial.split("-");
                if (parts.length >= 4) {
                    Long sId = Long.parseLong(parts[3]);
                    verifiedStudent = studentRepository.findById(sId).orElse(null);
                }
            } catch (Exception ignored) {}
        }

        model.addAttribute("serial", serial);
        model.addAttribute("verified", verifiedStudent != null);
        model.addAttribute("student", verifiedStudent);
        model.addAttribute("verifyDateKhmer", toKhmerDate(LocalDate.now()));

        return "certificate-verify";
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
}
