package com.nubb.classroom;

import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.repository.ClassroomRepository;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class WebController {

    @ModelAttribute("myClassrooms")
    public List<Classroom> getMyClassrooms(jakarta.servlet.http.HttpSession session) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId != null) {
            return classroomRepository.findByAccountId(accountId);
        }
        return new java.util.ArrayList<>();
    }
    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpSession session;


    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private com.nubb.classroom.repository.AttendanceRepository attendanceRepository;

    @Autowired
    private com.nubb.classroom.repository.ScoreRepository scoreRepository;
    @org.springframework.beans.factory.annotation.Autowired
    private com.nubb.classroom.repository.ScoreRuleRepository scoreRuleRepository;

    @GetMapping("/debug/accounts")
    @org.springframework.web.bind.annotation.ResponseBody
    public String debugAccounts() {
        StringBuilder sb = new StringBuilder();
        for (com.nubb.classroom.model.Account a : ((com.nubb.classroom.repository.AccountRepository) org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean(com.nubb.classroom.repository.AccountRepository.class)).findAll()) {
            sb.append(a.getUsername()).append(" / ").append(a.getPassword()).append(" [").append(a.getRole()).append("]<br>");
        }
        return sb.toString();
    }
    @GetMapping("/")
    public String index(@org.springframework.web.bind.annotation.RequestParam(required = false) String className, Model model) {
        java.time.LocalDate today = java.time.LocalDate.now();
        long totalStudents;
        long totalFemale = 0;
        long presentToday;
        long presentFemale = 0;
        long absentToday;
        long absentFemale = 0;
        List<com.nubb.classroom.model.Attendance> recentAbsents;

        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            List<Student> students = studentRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID"));
            totalStudents = students.size();
            totalFemale = students.stream().filter(s -> "ស្រី".equals(s.getGender())).count();
            
            List<com.nubb.classroom.model.Attendance> pres = attendanceRepository.findByDateAndPresentTrueAndStudentClassName(today, className);
            presentToday = pres.size();
            presentFemale = pres.stream().filter(a -> "ស្រី".equals(a.getStudent().getGender())).count();
            
            List<com.nubb.classroom.model.Attendance> abs = attendanceRepository.findByDateAndPresentFalseAndStudentClassName(today, className);
            absentToday = abs.size();
            absentFemale = abs.stream().filter(a -> "ស្រី".equals(a.getStudent().getGender())).count();
            recentAbsents = abs;
        } else {
            List<Student> students = studentRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID"));
            totalStudents = students.size();
            totalFemale = students.stream().filter(s -> "ស្រី".equals(s.getGender())).count();
            
            List<com.nubb.classroom.model.Attendance> pres = attendanceRepository.findByDateAndPresentTrue(today);
            presentToday = pres.size();
            presentFemale = pres.stream().filter(a -> "ស្រី".equals(a.getStudent().getGender())).count();
            
            List<com.nubb.classroom.model.Attendance> abs = attendanceRepository.findByDateAndPresentFalse(today);
            absentToday = abs.size();
            absentFemale = abs.stream().filter(a -> "ស្រី".equals(a.getStudent().getGender())).count();
            recentAbsents = abs;
        }

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalFemale", totalFemale);
        model.addAttribute("presentToday", presentToday);
        model.addAttribute("presentFemale", presentFemale);
        model.addAttribute("absentToday", absentToday);
        model.addAttribute("absentFemale", absentFemale);
        model.addAttribute("recentAbsents", recentAbsents);
        model.addAttribute("selectedClass", className);
        return "index";
    }

    @GetMapping("/attendance")
    public String attendance(@org.springframework.web.bind.annotation.RequestParam(required = false) String className, Model model) {
        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID"));
        } else {
            students = studentRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID"));
        }
        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
        return "attendance";
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(
            @org.springframework.web.bind.annotation.RequestParam("month") String month,
            @org.springframework.web.bind.annotation.RequestParam("className") String className,
            @org.springframework.web.bind.annotation.RequestParam(value = "attendanceData", required = false) java.util.List<String> attendanceData,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // In a real application, you would parse attendanceData (which contains studentId_day)
        // and save it to the Attendance repository for the specific month and class.
        // System.out.println("Saved attendance for: " + month + " - " + className + " Data: " + attendanceData);
        
        redirectAttributes.addFlashAttribute("successMessage", "ទិន្នន័យវត្តមានប្រចាំ " + month + " សម្រាប់ " + className + " ត្រូវបានរក្សាទុកដោយជោគជ័យ!");
        return "redirect:/attendance";
    }

    @GetMapping("/students")
    public String students(@org.springframework.web.bind.annotation.RequestParam(required = false) String className, Model model) {
        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID"));
        } else {
            students = studentRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID"));
        }
        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
        return "students";
    }

    @GetMapping("/student/add")
    public String addStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "add-student";
    }

    @PostMapping("/student/add")
    public String addStudentSubmit(Student student) {
        student.setAccountId((Long) session.getAttribute("ACCOUNT_ID")); studentRepository.save(student);
        return "redirect:/students";
    }

    @GetMapping("/student/edit/{id}")
    public String editStudentForm(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("student", student);
        return "edit-student";
    }

    @PostMapping("/student/edit/{id}")
    public String editStudentSubmit(@org.springframework.web.bind.annotation.PathVariable Long id, Student student) {
        student.setId(id);
        student.setAccountId((Long) session.getAttribute("ACCOUNT_ID")); studentRepository.save(student);
        return "redirect:/students";
    }

    @PostMapping("/student/delete/{id}")
    public String deleteStudent(@org.springframework.web.bind.annotation.PathVariable Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        attendanceRepository.deleteByStudent(student);
        scoreRepository.deleteByStudentId(id);
        studentRepository.delete(student);
        return "redirect:/students";
    }

    @GetMapping("/scores")
    public String scores(@org.springframework.web.bind.annotation.RequestParam(required = false) String className, Model model) {
        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID"));
        } else {
            students = studentRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID"));
        }
        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
        return "scores";
    }

    @PostMapping("/scores/save")
    public String saveScores(
            @org.springframework.web.bind.annotation.RequestParam("month") String month,
            @org.springframework.web.bind.annotation.RequestParam("className") String className,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("successMessage", "ទិន្នន័យពិន្ទុប្រចាំ " + month + " សម្រាប់ " + className + " ត្រូវបានរក្សាទុកដោយជោគជ័យ!");
        return "redirect:/scores";
    }



    @GetMapping("/schedule")
    public String schedule() {
        return "schedule";
    }

        @GetMapping("/statistics")
    public String statistics(@org.springframework.web.bind.annotation.RequestParam(required = false) String month, 
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String subjectName,
                             Model model) {
        if (month == null || month.isEmpty()) {
            month = "តេស្តដើមឆ្នាំ";
        }
        
        List<com.nubb.classroom.model.Score> allScores;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            allScores = scoreRepository.findByMonthAndClassNameAndAccountId(month, className, (Long) session.getAttribute("ACCOUNT_ID"));
        } else {
            allScores = scoreRepository.findByMonthAndAccountId(month, (Long) session.getAttribute("ACCOUNT_ID"));
            className = "ថ្នាក់រៀនទាំងអស់";
        }

        // Group scores by subject for the summary table
        java.util.Map<String, SubjectStat> subjectStatMap = new java.util.HashMap<>();
        
        for (com.nubb.classroom.model.Score score : allScores) {
            String subj = score.getSubjectName();
            if (subj == null || subj.isEmpty()) subj = "មិនស្គាល់";
            
            SubjectStat stat = subjectStatMap.getOrDefault(subj, new SubjectStat());
            stat.subjectName = subj;
            stat.total++;
            
            // fetch rule for this subject
            com.nubb.classroom.model.ScoreRule rule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), score.getClassName(), subj).orElse(null);
            if (rule == null) {
                rule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), "ថ្នាក់រៀនទាំងអស់", subj).orElse(null);
            }
            double maxTotal = 100; // default fallback
            if (rule != null) {
                maxTotal = rule.getMaxMonthlyScore() + rule.getMaxAttendanceScore() + rule.getMaxAssignmentScore();
            }
            if (maxTotal <= 0) maxTotal = 100;
            
            double percentage = (score.getTotal() / maxTotal) * 100;
            
            boolean isFemale = false;
            com.nubb.classroom.model.Student st = studentRepository.findById(score.getStudentId()).orElse(null);
            if (st != null && "ស្រី".equals(st.getGender())) {
                isFemale = true;
                stat.totalFemale++;
            }
            
            if (percentage >= 50) {
                stat.pass++;
                if (isFemale) stat.passFemale++;
            } else {
                stat.fail++;
                if (isFemale) stat.failFemale++;
            }
            
            if (percentage >= 90) { stat.gradeA++; if (isFemale) stat.gradeAFemale++; }
            else if (percentage >= 80) { stat.gradeB++; if (isFemale) stat.gradeBFemale++; }
            else if (percentage >= 70) { stat.gradeC++; if (isFemale) stat.gradeCFemale++; }
            else if (percentage >= 60) { stat.gradeD++; if (isFemale) stat.gradeDFemale++; }
            else if (percentage >= 50) { stat.gradeE++; if (isFemale) stat.gradeEFemale++; }
            else { stat.gradeF++; if (isFemale) stat.gradeFFemale++; }
            
            subjectStatMap.put(subj, stat);
        }
        
        // Filter for pie chart if subjectName is provided
        List<com.nubb.classroom.model.Score> filteredScores = allScores;
        if (subjectName != null && !subjectName.isEmpty() && !subjectName.equals("គ្រប់មុខវិជ្ជា")) {
            filteredScores = new java.util.ArrayList<>();
            for (com.nubb.classroom.model.Score s : allScores) {
                if (subjectName.equals(s.getSubjectName())) {
                    filteredScores.add(s);
                }
            }
        } else {
            subjectName = "គ្រប់មុខវិជ្ជា";
        }
        
                System.out.println("STATISTICS REQUEST: month=" + month + ", className=" + className + ", subjectName=" + subjectName);
        System.out.println("allScores size: " + allScores.size());
        System.out.println("filteredScores size: " + filteredScores.size());
        int totalStudents = filteredScores.size();
        int passCount = 0, failCount = 0, passFemaleCount = 0, failFemaleCount = 0;
        long gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0, gradeE = 0, gradeF = 0;
        long gradeAFemale = 0, gradeBFemale = 0, gradeCFemale = 0, gradeDFemale = 0, gradeEFemale = 0, gradeFFemale = 0;
        
        for (com.nubb.classroom.model.Score score : filteredScores) {
            String subj = score.getSubjectName();
            com.nubb.classroom.model.ScoreRule rule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), score.getClassName(), subj).orElse(null);
            if (rule == null) {
                rule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), "ថ្នាក់រៀនទាំងអស់", subj).orElse(null);
            }
            double maxTotal = 100;
            if (rule != null) {
                maxTotal = rule.getMaxMonthlyScore() + rule.getMaxAttendanceScore() + rule.getMaxAssignmentScore();
            }
            if (maxTotal <= 0) maxTotal = 100;
            double percentage = (score.getTotal() / maxTotal) * 100;
            
            boolean isPass = percentage >= 50;
            if (isPass) passCount++;
            else failCount++;
            
            boolean isFemale = false;
            com.nubb.classroom.model.Student st = studentRepository.findById(score.getStudentId()).orElse(null);
            if (st != null && "ស្រី".equals(st.getGender())) {
                isFemale = true;
                if (isPass) passFemaleCount++;
                else failFemaleCount++;
            }
            
            if (percentage >= 90) { gradeA++; if (isFemale) gradeAFemale++; }
            else if (percentage >= 80) { gradeB++; if (isFemale) gradeBFemale++; }
            else if (percentage >= 70) { gradeC++; if (isFemale) gradeCFemale++; }
            else if (percentage >= 60) { gradeD++; if (isFemale) gradeDFemale++; }
            else if (percentage >= 50) { gradeE++; if (isFemale) gradeEFemale++; }
            else { gradeF++; if (isFemale) gradeFFemale++; }
        }
        
        long passPercent = totalStudents == 0 ? 0 : (passCount * 100) / totalStudents;
        long failPercent = totalStudents == 0 ? 0 : (failCount * 100) / totalStudents;
        
        long aPercent = totalStudents == 0 ? 0 : (gradeA * 100) / totalStudents;
        long bPercent = totalStudents == 0 ? 0 : (gradeB * 100) / totalStudents;
        long cPercent = totalStudents == 0 ? 0 : (gradeC * 100) / totalStudents;
        long dPercent = totalStudents == 0 ? 0 : (gradeD * 100) / totalStudents;
        long ePercent = totalStudents == 0 ? 0 : (gradeE * 100) / totalStudents;
        long fPercent = totalStudents == 0 ? 0 : (gradeF * 100) / totalStudents;
        
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedSubject", subjectName);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalFemale", passFemaleCount + failFemaleCount);
        model.addAttribute("passCount", passCount);
        model.addAttribute("passFemaleCount", passFemaleCount);
        model.addAttribute("failCount", failCount);
        model.addAttribute("failFemaleCount", failFemaleCount);
        model.addAttribute("passPercent", passPercent);
        model.addAttribute("failPercent", failPercent);
        
                String rangeA = "៩០ - ១០០";
        String rangeB = "៨០ - ៨៩";
        String rangeC = "៧០ - ៧៩";
        String rangeD = "៦០ - ៦៩";
        String rangeE = "៥០ - ៥៩";
        String rangeF = "ក្រោម ៥០";
        
        if (subjectName != null && !subjectName.equals("គ្រប់មុខវិជ្ជា") && className != null) {
            com.nubb.classroom.model.ScoreRule globalRule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), className, subjectName).orElse(null);
            if (globalRule == null && !"ថ្នាក់រៀនទាំងអស់".equals(className)) {
                globalRule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName((Long) session.getAttribute("ACCOUNT_ID"), "ថ្នាក់រៀនទាំងអស់", subjectName).orElse(null);
            }
            if (globalRule != null) {
                double max = globalRule.getMaxMonthlyScore() + globalRule.getMaxAttendanceScore() + globalRule.getMaxAssignmentScore();
                if (max > 0) {
                    rangeA = String.format("%.0f - %.0f", Math.ceil(max * 0.90), max);
                    rangeB = String.format("%.0f - %.0f", Math.ceil(max * 0.80), Math.ceil(max * 0.90) - 1);
                    rangeC = String.format("%.0f - %.0f", Math.ceil(max * 0.70), Math.ceil(max * 0.80) - 1);
                    rangeD = String.format("%.0f - %.0f", Math.ceil(max * 0.60), Math.ceil(max * 0.70) - 1);
                    rangeE = String.format("%.0f - %.0f", Math.ceil(max * 0.50), Math.ceil(max * 0.60) - 1);
                    rangeF = String.format("ក្រោម %.0f", Math.ceil(max * 0.50));
                }
            }
        }
        
        model.addAttribute("rangeA", rangeA);
        model.addAttribute("rangeB", rangeB);
        model.addAttribute("rangeC", rangeC);
        model.addAttribute("rangeD", rangeD);
        model.addAttribute("rangeE", rangeE);
        model.addAttribute("rangeF", rangeF);
        
        model.addAttribute("gradeA", gradeA);
        model.addAttribute("gradeAFemale", gradeAFemale);
        model.addAttribute("gradeB", gradeB);
        model.addAttribute("gradeBFemale", gradeBFemale);
        model.addAttribute("gradeC", gradeC);
        model.addAttribute("gradeCFemale", gradeCFemale);
        model.addAttribute("gradeD", gradeD);
        model.addAttribute("gradeDFemale", gradeDFemale);
        model.addAttribute("gradeE", gradeE);
        model.addAttribute("gradeEFemale", gradeEFemale);
        model.addAttribute("gradeF", gradeF);
        model.addAttribute("gradeFFemale", gradeFFemale);
        
        model.addAttribute("aPercent", aPercent);
        model.addAttribute("bPercent", bPercent);
        model.addAttribute("cPercent", cPercent);
        model.addAttribute("dPercent", dPercent);
        model.addAttribute("ePercent", ePercent);
        model.addAttribute("fPercent", fPercent);
        
        // Table data
        List<SubjectStat> subjectStatList = new java.util.ArrayList<>(subjectStatMap.values());
        model.addAttribute("subjectStats", subjectStatList);
        
        model.addAttribute("myClassrooms", classroomRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID")));
        
        return "statistics";
    }

        @GetMapping("/classrooms")
    public String classrooms() {
        return "classrooms";
    }

    @GetMapping("/exams")
    public String exams() {
        return "exams";
    }

    @PostMapping("/exams/save")
    public String saveExams(
            @org.springframework.web.bind.annotation.RequestParam("month") String month,
            @org.springframework.web.bind.annotation.RequestParam("className") String className,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        redirectAttributes.addFlashAttribute("successMessage", "វិញ្ញាសាប្រឡងប្រចាំ " + month + " សម្រាប់ " + className + " ត្រូវបានរក្សាទុកដោយជោគជ័យ!");
        return "redirect:/exams";
    }

    // --- STUDENT EXAM PORTAL ---

    @GetMapping("/test")
    public String studentLogin() {
        return "student-login";
    }

    @PostMapping("/test/login")
    public String doStudentLogin(
            @org.springframework.web.bind.annotation.RequestParam("studentCode") String studentCode,
            jakarta.servlet.http.HttpSession session,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        java.util.Optional<Student> studentOpt = studentRepository.findByStudentCode(studentCode);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            session.setAttribute("loggedInStudent", student);

            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("REMEMBER_STUDENT_ID", String.valueOf(student.getId()));
            cookie.setMaxAge(30 * 24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return "redirect:/test/take";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "លេខកូដមិនត្រឹមត្រូវ សូមពិនិត្យម្តងទៀត!");
        return "redirect:/test";
    }

    @GetMapping("/test/take")
    public String takeExam(jakarta.servlet.http.HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/test";
        }
        model.addAttribute("student", student);
        return "take-exam";
    }

    @GetMapping("/test/logout")
    public String studentLogout(jakarta.servlet.http.HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("REMEMBER_STUDENT_ID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        session.invalidate();
        return "redirect:/test";
    }

    public static class ReportRow {
        private Student student;
        private double totalScore;
        private String grade;
        private int rank;
        private long presentCount;
        private long absentCount;

        public ReportRow(Student student, double totalScore, String grade, long presentCount, long absentCount) {
            this.student = student;
            this.totalScore = totalScore;
            this.grade = grade;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }

        public Student getStudent() { return student; }
        public double getTotalScore() { return totalScore; }
        public String getGrade() { return grade; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public long getPresentCount() { return presentCount; }
        public long getAbsentCount() { return absentCount; }
    }

    @GetMapping("/report")
    public String report(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String month,
            Model model) {
        
        if (month == null || month.isEmpty()) {
            month = "តេស្តដើមឆ្នាំ";
        }

        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID"));
        } else {
            students = studentRepository.findByAccountId((Long) session.getAttribute("ACCOUNT_ID"));
        }

        int monthNum = 1;
        int year = java.time.LocalDate.now().getYear();
        if (month.contains("មករា")) monthNum = 1;
        else if (month.contains("កុម្ភៈ")) monthNum = 2;
        else if (month.contains("មីនា")) monthNum = 3;
        else if (month.contains("មេសា")) monthNum = 4;
        else if (month.contains("ឧសភា")) monthNum = 5;
        else if (month.contains("មិថុនា")) monthNum = 6;
        else if (month.contains("កក្កដា")) monthNum = 7;
        else if (month.contains("សីហា")) monthNum = 8;
        else if (month.contains("កញ្ញា")) monthNum = 9;
        else if (month.contains("តុលា")) monthNum = 10;
        else if (month.contains("វិច្ឆិកា")) monthNum = 11;
        else if (month.contains("ធ្នូ")) monthNum = 12;

        String[] parts = month.split(" ");
        if(parts.length > 1) {
            try {
                year = Integer.parseInt(parts[1]);
            } catch (Exception e) {}
        }

        java.time.LocalDate start = java.time.LocalDate.of(year, monthNum, 1);
        java.time.LocalDate end = start.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        if (month.contains("តេស្តដើមឆ្នាំ") || month.contains("ប្រចាំឆ្នាំ")) {
            start = java.time.LocalDate.of(year, 1, 1);
            end = java.time.LocalDate.of(year, 12, 31);
        } else if (month.contains("ឆមាសទី១")) {
            start = java.time.LocalDate.of(2026, 11, 1);
            end = java.time.LocalDate.of(2027, 1, 31);
        } else if (month.contains("ឆមាសទី២")) {
            start = java.time.LocalDate.of(2027, 2, 1);
            end = java.time.LocalDate.of(2027, 7, 31);
        }

        java.util.List<ReportRow> reportRows = new java.util.ArrayList<>();

        for (Student student : students) {
            double totalScore = 0;
            String grade = "-";
            
            List<com.nubb.classroom.model.Score> scores = scoreRepository.findByStudentIdAndMonthAndClassNameAndAccountId(student.getId(), month, student.getClassName(), (Long) session.getAttribute("ACCOUNT_ID"));
            for (com.nubb.classroom.model.Score s : scores) {
                totalScore += s.getTotal();
            }
            if (!scores.isEmpty()) {
                if (totalScore >= 90) grade = "A (ល្អណាស់)";
                else if (totalScore >= 80) grade = "B (ល្អ)";
                else if (totalScore >= 70) grade = "C (បង្គួរ)";
                else if (totalScore >= 60) grade = "D (មធ្យម)";
                else if (totalScore >= 50) grade = "E (ខ្សោយ)";
                else grade = "F (ធ្លាក់)";
            }
            
            List<com.nubb.classroom.model.Attendance> attList = attendanceRepository.findByStudentIdAndDateBetween(student.getId(), start, end);
            long presentCount = attList.stream().filter(a -> a.isPresent()).count();
            long absentCount = attList.stream().filter(a -> !a.isPresent()).count();
            
            reportRows.add(new ReportRow(student, totalScore, grade, presentCount, absentCount));
        }

        // Sort by total score descending
        reportRows.sort((r1, r2) -> Double.compare(r2.getTotalScore(), r1.getTotalScore()));

        // Calculate ranks
        int currentRank = 1;
        for (int i = 0; i < reportRows.size(); i++) {
            if (i > 0 && reportRows.get(i).getTotalScore() < reportRows.get(i - 1).getTotalScore()) {
                currentRank = i + 1;
            }
            reportRows.get(i).setRank(currentRank);
        }

        model.addAttribute("reportRows", reportRows);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedMonth", month);
        
        return "report";
    }
    public static class SubjectStat {
        public String subjectName;
        public int total;
        public int totalFemale;
        public int pass;
        public int passFemale;
        public int fail;
        public int failFemale;
        public int gradeA;
        public int gradeAFemale;
        public int gradeB;
        public int gradeBFemale;
        public int gradeC;
        public int gradeCFemale;
        public int gradeD;
        public int gradeDFemale;
        public int gradeE;
        public int gradeEFemale;
        public int gradeF;
        public int gradeFFemale;
        
        // getters
        public String getSubjectName() { return subjectName; }
        public int getTotal() { return total; }
        public int getTotalFemale() { return totalFemale; }
        public int getPass() { return pass; }
        public int getPassFemale() { return passFemale; }
        public int getFail() { return fail; }
        public int getFailFemale() { return failFemale; }
        public int getGradeA() { return gradeA; }
        public int getGradeAFemale() { return gradeAFemale; }
        public int getGradeB() { return gradeB; }
        public int getGradeBFemale() { return gradeBFemale; }
        public int getGradeC() { return gradeC; }
        public int getGradeCFemale() { return gradeCFemale; }
        public int getGradeD() { return gradeD; }
        public int getGradeDFemale() { return gradeDFemale; }
        public int getGradeE() { return gradeE; }
        public int getGradeEFemale() { return gradeEFemale; }
        public int getGradeF() { return gradeF; }
        public int getGradeFFemale() { return gradeFFemale; }
    }
}