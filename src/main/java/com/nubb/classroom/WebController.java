package com.nubb.classroom;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private AccountRepository accountRepository;

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
        String[] provinces = {
            "ខេត្តបាត់ដំបង", "រាជធានីភ្នំពេញ", "ខេត្តសៀមរាប", "ខេត្តបន្ទាយមានជ័យ", "ខេត្តពោធិ៍សាត់",
            "ខេត្តកំពង់ចាម", "ខេត្តកំពង់ធំ", "ខេត្តកំពង់ឆ្នាំង", "ខេត្តកំពង់ស្ពឺ", "ខេត្តកណ្តាល",
            "ខេត្តតាកែវ", "ខេត្តកំពត", "ខេត្តកែប", "ខេត្តព្រះសីហនុ", "ខេត្តកោះកុង",
            "ខេត្តព្រៃវែង", "ខេត្តស្វាយរៀង", "ខេត្តក្រចេះ", "ខេត្តស្ទឹងត្រែង", "ខេត្តរតនគិរី",
            "ខេត្តមណ្ឌលគិរី", "ខេត្តព្រះវិហារ", "ខេត្តឧត្តរមានជ័យ", "ខេត្តប៉ៃលិន", "ខេត្តត្បូងឃ្មុំ"
        };
        model.addAttribute("provinces", provinces);

        String[] standardSubjects = {
            "គ្រប់មុខវិជ្ជា", "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", 
            "ភាសាខ្មែរ", "ភាសាអង់គ្លេស", "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា", 
            "សីលធម៌-ពលរដ្ឋវិជ្ជា", "ផែនដី និងបរិស្ថានវិទ្យា", "ព័ត៌មានវិទ្យា (ICT)", "កីឡា"
        };
        model.addAttribute("standardSubjects", standardSubjects);

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        Account teacherAccount = null;
        if (accountId != null) {
            try {
                teacherAccount = accountRepository.findById(accountId).orElse(null);
            } catch (Exception ignored) {}
        }
        model.addAttribute("teacherAccount", teacherAccount);

        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            long totalStudents = 0;
            long totalFemale = 0;
            long presentToday = 0;
            long presentFemale = 0;
            long absentToday = 0;
            long absentFemale = 0;
            List<com.nubb.classroom.model.Attendance> recentAbsents = new java.util.ArrayList<>();


            if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
                List<Student> students = (accountId != null) ? studentRepository.findByClassNameAndAccountId(className, accountId) : studentRepository.findAll();
                if (students != null) {
                    totalStudents = students.size();
                    totalFemale = students.stream().filter(s -> s != null && "ស្រី".equals(s.getGender())).count();
                }
                
                List<com.nubb.classroom.model.Attendance> pres = attendanceRepository.findByDateAndPresentTrueAndStudentClassName(today, className);
                if (pres != null) {
                    presentToday = pres.size();
                    presentFemale = pres.stream().filter(a -> a != null && a.getStudent() != null && "ស្រី".equals(a.getStudent().getGender())).count();
                }
                
                List<com.nubb.classroom.model.Attendance> abs = attendanceRepository.findByDateAndPresentFalseAndStudentClassName(today, className);
                if (abs != null) {
                    absentToday = abs.size();
                    absentFemale = abs.stream().filter(a -> a != null && a.getStudent() != null && "ស្រី".equals(a.getStudent().getGender())).count();
                    recentAbsents = abs;
                }
            } else {
                List<Student> students = (accountId != null) ? studentRepository.findByAccountId(accountId) : studentRepository.findAll();
                if (students != null) {
                    totalStudents = students.size();
                    totalFemale = students.stream().filter(s -> s != null && "ស្រី".equals(s.getGender())).count();
                }
                
                List<com.nubb.classroom.model.Attendance> pres = attendanceRepository.findByDateAndPresentTrue(today);
                if (pres != null) {
                    presentToday = pres.size();
                    presentFemale = pres.stream().filter(a -> a != null && a.getStudent() != null && "ស្រី".equals(a.getStudent().getGender())).count();
                }
                
                List<com.nubb.classroom.model.Attendance> abs = attendanceRepository.findByDateAndPresentFalse(today);
                if (abs != null) {
                    absentToday = abs.size();
                    absentFemale = abs.stream().filter(a -> a != null && a.getStudent() != null && "ស្រី".equals(a.getStudent().getGender())).count();
                    recentAbsents = abs;
                }
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
        } catch (Exception e) {
            System.err.println("Error in index dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("totalStudents", 0);
            model.addAttribute("totalFemale", 0);
            model.addAttribute("presentToday", 0);
            model.addAttribute("presentFemale", 0);
            model.addAttribute("absentToday", 0);
            model.addAttribute("absentFemale", 0);
            model.addAttribute("recentAbsents", new java.util.ArrayList<>());
            model.addAttribute("selectedClass", className);
            return "index";
        }
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }
        Account teacherAccount = accountRepository.findById(accountId).orElse(null);
        if (teacherAccount != null && (teacherAccount.getSchoolName() == null || teacherAccount.getSchoolName().isEmpty())) {
            for (Account other : accountRepository.findAll()) {
                if (other.getSchoolName() != null && !other.getSchoolName().isEmpty()) {
                    teacherAccount.setSchoolName(other.getSchoolName());
                    if (other.getSubject() != null) teacherAccount.setSubject(other.getSubject());
                    if (other.getProvince() != null) teacherAccount.setProvince(other.getProvince());
                    if (other.getGender() != null) teacherAccount.setGender(other.getGender());
                    if (other.getPosition() != null) teacherAccount.setPosition(other.getPosition());
                    if (other.getTeachingClasses() != null) teacherAccount.setTeachingClasses(other.getTeachingClasses());
                    if (other.getTelegram() != null) teacherAccount.setTelegram(other.getTelegram());
                    if (other.getPhone() != null) teacherAccount.setPhone(other.getPhone());
                    accountRepository.save(teacherAccount);
                    break;
                }
            }
        }
        model.addAttribute("teacherAccount", teacherAccount);

        String[] provinces = {
            "ខេត្តបាត់ដំបង", "រាជធានីភ្នំពេញ", "ខេត្តសៀមរាប", "ខេត្តបន្ទាយមានជ័យ", "ខេត្តពោធិ៍សាត់",
            "ខេត្តកំពង់ចាម", "ខេត្តកំពង់ធំ", "ខេត្តកំពង់ឆ្នាំង", "ខេត្តកំពង់ស្ពឺ", "ខេត្តកណ្តាល",
            "ខេត្តតាកែវ", "ខេត្តកំពត", "ខេត្តកែប", "ខេត្តព្រះសីហនុ", "ខេត្តកោះកុង",
            "ខេត្តព្រៃវែង", "ខេត្តស្វាយរៀង", "ខេត្តក្រចេះ", "ខេត្តស្ទឹងត្រែង", "ខេត្តរតនគិរី",
            "ខេត្តមណ្ឌលគិរី", "ខេត្តព្រះវិហារ", "ខេត្តឧត្តរមានជ័យ", "ខេត្តប៉ៃលិន", "ខេត្តត្បូងឃ្មុំ"
        };
        model.addAttribute("provinces", provinces);

        String[] standardSubjects = {
            "គ្រប់មុខវិជ្ជា", "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", 
            "ភាសាខ្មែរ", "ភាសាអង់គ្លេស", "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា", 
            "សីលធម៌-ពលរដ្ឋវិជ្ជា", "ផែនដី និងបរិស្ថានវិទ្យា", "ព័ត៌មានវិទ្យា (ICT)", "កីឡា"
        };
        model.addAttribute("standardSubjects", standardSubjects);

        return "profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "schoolName", required = false) String schoolName,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "customSubject", required = false) String customSubject,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "telegram", required = false) String telegram,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "teachingClasses", required = false) String teachingClasses,
            @RequestParam(value = "bio", required = false) String bio,
            RedirectAttributes redirectAttributes) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            if (fullName != null && !fullName.trim().isEmpty()) {
                acc.setFullName(fullName.trim());
                session.setAttribute("ACCOUNT_NAME", acc.getFullName());
            }
            if (gender != null && !gender.trim().isEmpty()) {
                acc.setGender(gender.trim());
            }
            if (schoolName != null) {
                acc.setSchoolName(schoolName.trim());
            }

            String finalSubject = subject;
            if ("custom".equals(subject) && customSubject != null && !customSubject.trim().isEmpty()) {
                finalSubject = customSubject.trim();
            }
            if (finalSubject != null) {
                acc.setSubject(finalSubject.trim());
            }

            if (province != null) {
                acc.setProvince(province.trim());
            }
            if (phone != null) {
                acc.setPhone(phone.trim());
            }
            if (telegram != null) {
                acc.setTelegram(telegram.trim());
            }
            if (email != null) {
                acc.setEmail(email.trim());
            }
            if (position != null) {
                acc.setPosition(position.trim());
            }
            if (teachingClasses != null) {
                acc.setTeachingClasses(teachingClasses.trim());
            }
            if (bio != null) {
                acc.setBio(bio.trim());
            }

            accountRepository.save(acc);
            syncTeacherAccounts(acc);
            redirectAttributes.addFlashAttribute("profileSuccess", "✅ បានរក្សាទុកព័ត៌មានផ្ទាល់ខ្លួនរបស់លោកគ្រូ-អ្នកគ្រូដោយជោគជ័យ!");
        }

        return "redirect:/profile";
    }

    private void syncTeacherAccounts(Account src) {
        try {
            for (Account other : accountRepository.findAll()) {
                if (!other.getId().equals(src.getId()) && ("admin".equals(other.getUsername()) || "samkimchheng".equals(other.getUsername()) || (other.getFullName() != null && other.getFullName().contains("សំ គឹមឆេង")))) {
                    if (src.getFullName() != null) other.setFullName(src.getFullName());
                    if (src.getGender() != null) other.setGender(src.getGender());
                    if (src.getSchoolName() != null) other.setSchoolName(src.getSchoolName());
                    if (src.getSubject() != null) other.setSubject(src.getSubject());
                    if (src.getProvince() != null) other.setProvince(src.getProvince());
                    if (src.getPhone() != null) other.setPhone(src.getPhone());
                    if (src.getTelegram() != null) other.setTelegram(src.getTelegram());
                    if (src.getEmail() != null) other.setEmail(src.getEmail());
                    if (src.getPosition() != null) other.setPosition(src.getPosition());
                    if (src.getTeachingClasses() != null) other.setTeachingClasses(src.getTeachingClasses());
                    if (src.getBio() != null) other.setBio(src.getBio());
                    accountRepository.save(other);
                }
            }
        } catch (Exception ignored) {}
    }

    @PostMapping("/api/profile/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSaveProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "schoolName", required = false) String schoolName,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "customSubject", required = false) String customSubject,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "telegram", required = false) String telegram,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "teachingClasses", required = false) String teachingClasses,
            @RequestParam(value = "bio", required = false) String bio) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់គណនីជាមុនសិន!"));
        }

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            if (fullName != null && !fullName.trim().isEmpty()) {
                acc.setFullName(fullName.trim());
                session.setAttribute("ACCOUNT_NAME", acc.getFullName());
            }
            if (gender != null && !gender.trim().isEmpty()) {
                acc.setGender(gender.trim());
            }
            if (schoolName != null) {
                acc.setSchoolName(schoolName.trim());
            }

            String finalSubject = subject;
            if ("custom".equals(subject) && customSubject != null && !customSubject.trim().isEmpty()) {
                finalSubject = customSubject.trim();
            }
            if (finalSubject != null) {
                acc.setSubject(finalSubject.trim());
            }

            if (province != null) {
                acc.setProvince(province.trim());
            }
            if (phone != null) {
                acc.setPhone(phone.trim());
            }
            if (telegram != null) {
                acc.setTelegram(telegram.trim());
            }
            if (email != null) {
                acc.setEmail(email.trim());
            }
            if (position != null) {
                acc.setPosition(position.trim());
            }
            if (teachingClasses != null) {
                acc.setTeachingClasses(teachingClasses.trim());
            }
            if (bio != null) {
                acc.setBio(bio.trim());
            }

            accountRepository.save(acc);
            syncTeacherAccounts(acc);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ បានរក្សាទុកព័ត៌មានផ្ទាល់ខ្លួនដោយជោគជ័យ!",
                "fullName", acc.getFullName() != null ? acc.getFullName() : ""
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "រកមិនឃើញគណនីគ្រូបង្រៀន!"));
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
        Long accId = (Long) session.getAttribute("ACCOUNT_ID");
        student.setAccountId(accId != null ? accId : 97L);
        studentRepository.save(student);
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
        Student existing = studentRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(student.getName());
            existing.setGender(student.getGender());
            existing.setClassName(student.getClassName());
            existing.setStudentCode(student.getStudentCode());
            existing.setVillage(student.getVillage());
            existing.setCommune(student.getCommune());
            existing.setDistrict(student.getDistrict());
            existing.setProvince(student.getProvince());
            existing.setFatherName(student.getFatherName());
            existing.setFatherOccupation(student.getFatherOccupation());
            existing.setMotherName(student.getMotherName());
            existing.setMotherOccupation(student.getMotherOccupation());
            existing.setStatus(student.getStatus());
            Long accId = (Long) session.getAttribute("ACCOUNT_ID");
            if (accId != null) {
                existing.setAccountId(accId);
            } else if (existing.getAccountId() == null) {
                existing.setAccountId(97L);
            }
            studentRepository.save(existing);
        }
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
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (month == null || month.isEmpty()) {
            month = "តេស្តដើមឆ្នាំ";
        }
        
        List<com.nubb.classroom.model.Score> allScores;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            allScores = scoreRepository.findByMonthAndClassNameAndAccountId(month, className, accountId);
        } else {
            allScores = scoreRepository.findByMonthAndAccountId(month, accountId);
            className = "ថ្នាក់រៀនទាំងអស់";
        }

        // Get enrolled students for this class or all classes
        List<com.nubb.classroom.model.Student> enrolledStudents;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            enrolledStudents = studentRepository.findByClassNameAndAccountId(className, accountId);
        } else {
            enrolledStudents = studentRepository.findByAccountId(accountId);
        }
        int enrolledTotal = enrolledStudents.size();
        int enrolledFemale = (int) enrolledStudents.stream().filter(s -> "ស្រី".equals(s.getGender())).count();

        // Student fast lookup map
        java.util.Map<Long, com.nubb.classroom.model.Student> studentMap = new java.util.HashMap<>();
        for (com.nubb.classroom.model.Student st : enrolledStudents) {
            if (st.getId() != null) {
                studentMap.put(st.getId(), st);
            }
        }
        for (com.nubb.classroom.model.Score sc : allScores) {
            if (sc.getStudentId() != null && !studentMap.containsKey(sc.getStudentId())) {
                studentRepository.findById(sc.getStudentId()).ifPresent(s -> studentMap.put(s.getId(), s));
            }
        }

        // Helper to get max total score for subject
        java.util.function.BiFunction<String, String, Double> getMaxTotal = (cls, subj) -> {
            com.nubb.classroom.model.ScoreRule rule = null;
            List<com.nubb.classroom.model.ScoreRule> rules = scoreRuleRepository.findRules(cls, subj, accountId);
            if (rules != null && !rules.isEmpty()) {
                rule = rules.get(0);
            } else {
                List<com.nubb.classroom.model.ScoreRule> globalRules = scoreRuleRepository.findRules("ថ្នាក់រៀនទាំងអស់", subj, accountId);
                if (globalRules != null && !globalRules.isEmpty()) {
                    rule = globalRules.get(0);
                }
            }
            if (rule != null) {
                double max = rule.getMaxMonthlyScore() + rule.getMaxAttendanceScore() + rule.getMaxAssignmentScore();
                if (max > 0) return max;
            }
            return 100.0;
        };

        // Group scores by subject for the summary table
        java.util.Map<String, SubjectStat> subjectStatMap = new java.util.LinkedHashMap<>();
        for (com.nubb.classroom.model.Score score : allScores) {
            String subj = score.getSubjectName();
            if (subj == null || subj.isEmpty()) subj = "មិនស្គាល់";
            
            SubjectStat stat = subjectStatMap.getOrDefault(subj, new SubjectStat());
            stat.subjectName = subj;
            stat.total++;
            
            double maxTotal = getMaxTotal.apply(score.getClassName(), subj);
            double percentage = (score.getTotal() / maxTotal) * 100.0;
            
            boolean isFemale = false;
            com.nubb.classroom.model.Student st = studentMap.get(score.getStudentId());
            if (st != null && "ស្រី".equals(st.getGender())) {
                isFemale = true;
                stat.totalFemale++;
            }
            
            if (percentage >= 50.0) {
                stat.pass++;
                if (isFemale) stat.passFemale++;
            } else {
                stat.fail++;
                if (isFemale) stat.failFemale++;
            }
            
            if (percentage >= 90.0) { stat.gradeA++; if (isFemale) stat.gradeAFemale++; }
            else if (percentage >= 80.0) { stat.gradeB++; if (isFemale) stat.gradeBFemale++; }
            else if (percentage >= 70.0) { stat.gradeC++; if (isFemale) stat.gradeCFemale++; }
            else if (percentage >= 60.0) { stat.gradeD++; if (isFemale) stat.gradeDFemale++; }
            else if (percentage >= 50.0) { stat.gradeE++; if (isFemale) stat.gradeEFemale++; }
            else { stat.gradeF++; if (isFemale) stat.gradeFFemale++; }
            
            subjectStatMap.put(subj, stat);
        }

        int totalStudents = 0;
        int totalFemale = 0;
        int passCount = 0, failCount = 0, passFemaleCount = 0, failFemaleCount = 0;
        long gradeA = 0, gradeB = 0, gradeC = 0, gradeD = 0, gradeE = 0, gradeF = 0;
        long gradeAFemale = 0, gradeBFemale = 0, gradeCFemale = 0, gradeDFemale = 0, gradeEFemale = 0, gradeFFemale = 0;

        if (subjectName == null || subjectName.isEmpty() || subjectName.equals("គ្រប់មុខវិជ្ជា")) {
            subjectName = "គ្រប់មុខវិជ្ជា";

            // Group allScores by studentId to evaluate each student's average performance across subjects
            java.util.Map<Long, List<com.nubb.classroom.model.Score>> scoresByStudent = new java.util.LinkedHashMap<>();
            for (com.nubb.classroom.model.Score sc : allScores) {
                if (sc.getStudentId() != null) {
                    scoresByStudent.computeIfAbsent(sc.getStudentId(), k -> new java.util.ArrayList<>()).add(sc);
                }
            }

            for (java.util.Map.Entry<Long, List<com.nubb.classroom.model.Score>> entry : scoresByStudent.entrySet()) {
                Long stId = entry.getKey();
                List<com.nubb.classroom.model.Score> stScores = entry.getValue();
                if (stScores.isEmpty()) continue;

                double sumPct = 0.0;
                for (com.nubb.classroom.model.Score sc : stScores) {
                    double maxTotal = getMaxTotal.apply(sc.getClassName(), sc.getSubjectName());
                    sumPct += (sc.getTotal() / maxTotal) * 100.0;
                }
                double avgPct = sumPct / stScores.size();

                boolean isFemale = false;
                com.nubb.classroom.model.Student st = studentMap.get(stId);
                if (st != null && "ស្រី".equals(st.getGender())) {
                    isFemale = true;
                }

                boolean isPass = avgPct >= 50.0;
                if (isPass) {
                    passCount++;
                    if (isFemale) passFemaleCount++;
                } else {
                    failCount++;
                    if (isFemale) failFemaleCount++;
                }

                if (avgPct >= 90.0) { gradeA++; if (isFemale) gradeAFemale++; }
                else if (avgPct >= 80.0) { gradeB++; if (isFemale) gradeBFemale++; }
                else if (avgPct >= 70.0) { gradeC++; if (isFemale) gradeCFemale++; }
                else if (avgPct >= 60.0) { gradeD++; if (isFemale) gradeDFemale++; }
                else if (avgPct >= 50.0) { gradeE++; if (isFemale) gradeEFemale++; }
                else { gradeF++; if (isFemale) gradeFFemale++; }
            }

            int evaluatedStudents = passCount + failCount;
            totalStudents = evaluatedStudents > 0 ? evaluatedStudents : enrolledTotal;
            totalFemale = evaluatedStudents > 0 ? (passFemaleCount + failFemaleCount) : enrolledFemale;

        } else {
            // Specific subject selected
            List<com.nubb.classroom.model.Score> filteredScores = new java.util.ArrayList<>();
            for (com.nubb.classroom.model.Score s : allScores) {
                if (subjectName.equals(s.getSubjectName())) {
                    filteredScores.add(s);
                }
            }

            for (com.nubb.classroom.model.Score score : filteredScores) {
                double maxTotal = getMaxTotal.apply(score.getClassName(), subjectName);
                double percentage = (score.getTotal() / maxTotal) * 100.0;

                boolean isPass = percentage >= 50.0;
                if (isPass) passCount++;
                else failCount++;

                boolean isFemale = false;
                com.nubb.classroom.model.Student st = studentMap.get(score.getStudentId());
                if (st != null && "ស្រី".equals(st.getGender())) {
                    isFemale = true;
                    if (isPass) passFemaleCount++;
                    else failFemaleCount++;
                }

                if (percentage >= 90.0) { gradeA++; if (isFemale) gradeAFemale++; }
                else if (percentage >= 80.0) { gradeB++; if (isFemale) gradeBFemale++; }
                else if (percentage >= 70.0) { gradeC++; if (isFemale) gradeCFemale++; }
                else if (percentage >= 60.0) { gradeD++; if (isFemale) gradeDFemale++; }
                else if (percentage >= 50.0) { gradeE++; if (isFemale) gradeEFemale++; }
                else { gradeF++; if (isFemale) gradeFFemale++; }
            }

            int evaluatedStudents = passCount + failCount;
            totalStudents = evaluatedStudents > 0 ? evaluatedStudents : enrolledTotal;
            totalFemale = evaluatedStudents > 0 ? (passFemaleCount + failFemaleCount) : enrolledFemale;
        }

        int evaluatedStudents = passCount + failCount;
        long passPercent = evaluatedStudents == 0 ? 0 : Math.round((passCount * 100.0) / evaluatedStudents);
        long failPercent = evaluatedStudents == 0 ? 0 : Math.round((failCount * 100.0) / evaluatedStudents);
        
        long aPercent = evaluatedStudents == 0 ? 0 : Math.round((gradeA * 100.0) / evaluatedStudents);
        long bPercent = evaluatedStudents == 0 ? 0 : Math.round((gradeB * 100.0) / evaluatedStudents);
        long cPercent = evaluatedStudents == 0 ? 0 : Math.round((gradeC * 100.0) / evaluatedStudents);
        long dPercent = evaluatedStudents == 0 ? 0 : Math.round((gradeD * 100.0) / evaluatedStudents);
        long ePercent = evaluatedStudents == 0 ? 0 : Math.round((gradeE * 100.0) / evaluatedStudents);
        long fPercent = evaluatedStudents == 0 ? 0 : Math.round((gradeF * 100.0) / evaluatedStudents);

        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedSubject", subjectName);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalFemale", totalFemale);
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
            double max = getMaxTotal.apply(className, subjectName);
            if (max > 0 && max != 100.0) {
                rangeA = String.format("%.0f - %.0f", Math.ceil(max * 0.90), max);
                rangeB = String.format("%.0f - %.0f", Math.ceil(max * 0.80), Math.ceil(max * 0.90) - 1);
                rangeC = String.format("%.0f - %.0f", Math.ceil(max * 0.70), Math.ceil(max * 0.80) - 1);
                rangeD = String.format("%.0f - %.0f", Math.ceil(max * 0.60), Math.ceil(max * 0.70) - 1);
                rangeE = String.format("%.0f - %.0f", Math.ceil(max * 0.50), Math.ceil(max * 0.60) - 1);
                rangeF = String.format("ក្រោម %.0f", Math.ceil(max * 0.50));
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
        
        model.addAttribute("myClassrooms", classroomRepository.findByAccountId(accountId));
        
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