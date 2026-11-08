package com.nubb.classroom;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Attendance;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import com.nubb.classroom.config.TokenSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TokenSecurityService tokenSecurityService;

    public static String toKhmerNumber(int n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9') {
                sb.append((char) ('០' + (c - '0')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static int fromKhmerNumber(String s) {
        if (s == null) return 0;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '០' && c <= '៩') {
                sb.append((char) ('0' + (c - '០')));
            } else if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    public static int[] parseAcademicYearNumbers(String academicYear) {
        int startYear = 2026;
        int endYear = 2027;
        if (academicYear != null) {
            String[] parts = academicYear.split("[-–/]+");
            if (parts.length >= 2) {
                int y1 = fromKhmerNumber(parts[0]);
                int y2 = fromKhmerNumber(parts[1]);
                if (y1 > 1900 && y1 < 2100) startYear = y1;
                if (y2 > 1900 && y2 < 2100) endYear = y2;
            } else if (parts.length == 1) {
                int y = fromKhmerNumber(parts[0]);
                if (y > 1900 && y < 2100) {
                    startYear = y;
                    endYear = y + 1;
                }
            }
        }
        return new int[]{startYear, endYear};
    }

    public static List<String> getMonthsForAcademicYear(String academicYear) {
        int[] years = parseAcademicYearNumbers(academicYear);
        int y1 = years[0];
        int y2 = years[1];
        String sy1 = toKhmerNumber(y1);
        String sy2 = toKhmerNumber(y2);

        List<String> list = new ArrayList<>();
        list.add("តេស្តដើមឆ្នាំ");
        list.add("ខែវិច្ឆិកា " + sy1);
        list.add("ខែធ្នូ " + sy1);
        list.add("ខែមករា " + sy2);
        list.add("ខែកុម្ភៈ " + sy2);
        list.add("ឆមាសទី១");
        list.add("ខែមីនា " + sy2);
        list.add("ខែមេសា " + sy2);
        list.add("ខែឧសភា " + sy2);
        list.add("ខែមិថុនា " + sy2);
        list.add("ខែកក្កដា " + sy2);
        list.add("ឆមាសទី២");
        list.add("ខែសីហា " + sy2);
        list.add("ប្រចាំឆ្នាំ");
        return list;
    }

    public String getCurrentAcademicYear() {
        String year = (String) session.getAttribute("CURRENT_ACADEMIC_YEAR");
        if (year != null && !year.trim().isEmpty()) {
            return year.trim();
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId != null) {
            try {
                Account acc = accountRepository.findById(accountId).orElse(null);
                if (acc != null && acc.getCurrentAcademicYear() != null && !acc.getCurrentAcademicYear().trim().isEmpty()) {
                    year = acc.getCurrentAcademicYear().trim();
                    session.setAttribute("CURRENT_ACADEMIC_YEAR", year);
                    return year;
                }
            } catch (Exception ignored) {}
        }
        year = "២០២៦-២០២៧";
        session.setAttribute("CURRENT_ACADEMIC_YEAR", year);
        return year;
    }

    @ModelAttribute("currentAcademicYear")
    public String currentAcademicYear() {
        return getCurrentAcademicYear();
    }

    @ModelAttribute("availableAcademicYears")
    public List<String> availableAcademicYears() {
        return getAvailableAcademicYears();
    }

    @ModelAttribute("monthsList")
    public List<String> monthsList() {
        return getMonthsForAcademicYear(getCurrentAcademicYear());
    }

    public List<String> getAvailableAcademicYears() {
        Set<String> set = new java.util.LinkedHashSet<>();
        set.add("២០២៤-២០២៥");
        set.add("២០២៥-២០២៦");
        set.add("២០២៦-២០២៧");
        set.add("២០២៧-២០២៨");
        set.add("២០២៨-២០២៩");
        set.add("២០២៩-២០៣០");

        String curr = (String) session.getAttribute("CURRENT_ACADEMIC_YEAR");
        if (curr != null && !curr.trim().isEmpty()) {
            set.add(curr.trim());
        }

        try {
            Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
            if (accountId != null) {
                List<Student> students = studentRepository.findByAccountId(accountId);
                for (Student s : students) {
                    if (s.getAcademicYear() != null && !s.getAcademicYear().trim().isEmpty()) {
                        set.add(s.getAcademicYear().trim());
                    }
                }
                List<Classroom> classrooms = classroomRepository.findByAccountId(accountId);
                for (Classroom c : classrooms) {
                    if (c.getAcademicYear() != null && !c.getAcademicYear().trim().isEmpty()) {
                        set.add(c.getAcademicYear().trim());
                    }
                }
            }
        } catch (Exception ignored) {}

        return new ArrayList<>(set);
    }

    @GetMapping("/switch-academic-year")
    public String switchAcademicYear(@RequestParam("year") String year, 
                                     @RequestParam(value = "redirect", required = false) String redirect) {
        if (year != null && !year.trim().isEmpty()) {
            String trimmed = year.trim();
            session.setAttribute("CURRENT_ACADEMIC_YEAR", trimmed);
            Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
            if (accountId != null) {
                try {
                    Account acc = accountRepository.findById(accountId).orElse(null);
                    if (acc != null) {
                        acc.setCurrentAcademicYear(trimmed);
                        accountRepository.save(acc);
                    }
                } catch (Exception ignored) {}
            }
        }
        if (redirect != null && !redirect.trim().isEmpty() && !redirect.startsWith("http")) {
            return "redirect:" + redirect;
        }
        return "redirect:/";
    }

    @PostMapping("/classrooms/copy-from-previous-year")
    @ResponseBody
    public Map<String, Object> copyClassroomsFromPreviousYear(@RequestParam("fromYear") String fromYear) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return Map.of("success", false, "message", "សូមចូលប្រព័ន្ធជាមុនសិន");
        }
        String currentYear = getCurrentAcademicYear();
        List<Classroom> prevClasses = classroomRepository.findByAccountIdAndAcademicYear(accountId, fromYear);
        if (prevClasses == null || prevClasses.isEmpty()) {
            return Map.of("success", false, "message", "មិនមានថ្នាក់រៀនក្នុងឆ្នាំសិក្សា " + fromYear + " ឡើយ");
        }
        int count = 0;
        for (Classroom prev : prevClasses) {
            if (!classroomRepository.existsByClassNameAndAccountIdAndAcademicYear(prev.getClassName(), accountId, currentYear)) {
                Classroom c = new Classroom();
                c.setClassName(prev.getClassName());
                c.setAccountId(accountId);
                c.setAcademicYear(currentYear);
                classroomRepository.save(c);
                count++;
            }
        }
        return Map.of("success", true, "message", "បានចម្លងថ្នាក់រៀនចំនួន " + count + " ពីឆ្នាំសិក្សា " + fromYear + " ដោយជោគជ័យ!", "count", count);
    }

    @ModelAttribute("teacherAccount")
    public Account getTeacherAccount(jakarta.servlet.http.HttpSession session) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId != null) {
            if (accountId == 1L) accountId = 97L;
            return accountRepository.findById(accountId).orElse(null);
        }
        return null;
    }

    @ModelAttribute("myClassrooms")
    public List<Classroom> getMyClassrooms(jakarta.servlet.http.HttpSession session) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId != null) {
            if (accountId == 1L) accountId = 97L;
            String academicYear = getCurrentAcademicYear();
            return classroomRepository.findByAccountIdAndAcademicYear(accountId, academicYear);
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
    @Autowired
    private com.nubb.classroom.repository.CertificateRecordRepository certificateRecordRepository;

    @Autowired
    private com.nubb.classroom.repository.LeaveRequestRepository leaveRequestRepository;

    public List<com.nubb.classroom.model.LeaveRequest> getLeaveRequestsForTeacher(Long teacherAccountId, String classNameFilter) {
        try {
            Long effTeacherId = (teacherAccountId == null || teacherAccountId == 1L) ? 97L : teacherAccountId;
            Set<String> teacherClasses = new HashSet<>();
            for (Classroom c : classroomRepository.findByAccountId(effTeacherId)) {
                if (c.getClassName() != null && !c.getClassName().trim().isEmpty()) {
                    teacherClasses.add(c.getClassName().trim());
                }
            }
            for (Student s : studentRepository.findByAccountId(effTeacherId)) {
                if (s.getClassName() != null && !s.getClassName().trim().isEmpty()) {
                    teacherClasses.add(s.getClassName().trim());
                }
            }

            List<com.nubb.classroom.model.LeaveRequest> all = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
            List<com.nubb.classroom.model.LeaveRequest> result = new ArrayList<>();
            for (com.nubb.classroom.model.LeaveRequest lr : all) {
                if (lr == null || lr.getStudent() == null) continue;
                Long reqAccId = lr.getAccountId() != null ? lr.getAccountId() :
                        (lr.getStudent().getAccountId() != null ? lr.getStudent().getAccountId() : 97L);
                Long effReqAccId = (reqAccId == 1L) ? 97L : reqAccId;

                boolean visible = effTeacherId.equals(effReqAccId);
                if (!visible && effTeacherId != 98L && effReqAccId != 98L) {
                    String lrClass = lr.getClassName() != null ? lr.getClassName() : lr.getStudent().getClassName();
                    for (String tc : teacherClasses) {
                        if (com.nubb.classroom.controller.LessonController.isClassMatching(tc, lrClass)) {
                            visible = true;
                            break;
                        }
                    }
                }
                if (!visible) continue;

                if (classNameFilter != null && !classNameFilter.trim().isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(classNameFilter.trim())) {
                    String lrClass = lr.getClassName() != null ? lr.getClassName() : lr.getStudent().getClassName();
                    if (!com.nubb.classroom.controller.LessonController.isClassMatching(lrClass, classNameFilter.trim())) {
                        continue;
                    }
                }
                result.add(lr);
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void syncLeaveRequestToAttendance(com.nubb.classroom.model.LeaveRequest lr, boolean markPermission) {
        if (lr == null || lr.getStudent() == null || lr.getStudent().getId() == null
                || lr.getStartDate() == null || lr.getEndDate() == null) {
            return;
        }
        try {
            Student student = lr.getStudent();
            LocalDate start = lr.getStartDate();
            LocalDate end = lr.getEndDate();
            Long accId = lr.getAccountId() != null ? lr.getAccountId() :
                    (student.getAccountId() != null ? student.getAccountId() : 97L);
            if (accId == 1L) accId = 97L;

            List<Attendance> existing = attendanceRepository.findByStudentIdAndDateBetween(student.getId(), start, end);
            if (markPermission) {
                List<Attendance> toSave = new ArrayList<>();
                LocalDate d = start;
                int guard = 0;
                while (!d.isAfter(end) && guard < 35) {
                    final LocalDate curr = d;
                    Optional<Attendance> found = existing.stream().filter(a -> curr.equals(a.getDate())).findFirst();
                    if (found.isPresent()) {
                        Attendance att = found.get();
                        att.setPresent(false);
                        att.setAbsenceType("PERMISSION");
                        if (att.getAccountId() == null) att.setAccountId(accId);
                        toSave.add(att);
                    } else {
                        Attendance att = new Attendance(student, curr, false, "PERMISSION");
                        att.setAccountId(accId);
                        toSave.add(att);
                    }
                    d = d.plusDays(1);
                    guard++;
                }
                if (!toSave.isEmpty()) {
                    attendanceRepository.saveAll(toSave);
                }
            } else {
                List<com.nubb.classroom.model.LeaveRequest> otherActive = leaveRequestRepository
                        .findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                        .filter(other -> !other.getId().equals(lr.getId()) && !other.isRejected())
                        .collect(Collectors.toList());

                for (Attendance att : existing) {
                    if (att.isPermission()) {
                        LocalDate d = att.getDate();
                        boolean coveredByOther = otherActive.stream().anyMatch(o ->
                                o.getStartDate() != null && o.getEndDate() != null
                                        && !d.isBefore(o.getStartDate()) && !d.isAfter(o.getEndDate()));
                        if (!coveredByOther) {
                            attendanceRepository.delete(att);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }


    
    @GetMapping({"/docs/teacher-guide.pdf", "/docs/guide.pdf"})
    @ResponseBody
    public ResponseEntity<Resource> viewTeacherGuidePdf(
            @RequestParam(value = "download", defaultValue = "false") boolean download) {
        Resource resource = new ClassPathResource("static/docs/teacher-guide.pdf");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String khmerFilename = "សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.pdf";
        String encodedFilename = URLEncoder.encode(khmerFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = download ? "attachment" : "inline";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"teacher-guide.pdf\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(resource);
    }

    @GetMapping({"/download/teacher-guide", "/download/teacher-guide-pdf"})
    @ResponseBody
    public ResponseEntity<Resource> downloadTeacherGuidePdf() {
        Resource resource = new ClassPathResource("static/docs/teacher-guide.pdf");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String khmerFilename = "សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.pdf";
        String encodedFilename = URLEncoder.encode(khmerFilename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"teacher-guide.pdf\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(resource);
    }

    @GetMapping({"/download/teacher-guide.docx", "/download/teacher-guide.doc"})
    @ResponseBody
    public ResponseEntity<Resource> downloadTeacherGuideDocx() {
        Resource resource = new ClassPathResource("static/docs/teacher-guide.docx");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String khmerFilename = "សៀវភៅណែនាំ_ការប្រើប្រាស់ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន_២០២៧.docx";
        String encodedFilename = URLEncoder.encode(khmerFilename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"teacher-guide.docx\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(resource);
    }

    
    @GetMapping({"/guide", "/teacher-guide"})
    public String guidePage() {
        return "guide";
    }

    @GetMapping("/")
    public String index(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String date,
            Model model) {
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
        Long effAccountId = (accountId != null && accountId == 1L) ? 97L : accountId;
        Account teacherAccount = null;
        if (effAccountId != null) {
            try {
                teacherAccount = accountRepository.findById(effAccountId).orElse(null);
            } catch (Exception ignored) {}
        }
        model.addAttribute("teacherAccount", teacherAccount);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate targetDate = today;
        if (date != null && !date.trim().isEmpty()) {
            try {
                targetDate = java.time.LocalDate.parse(date.trim());
            } catch (Exception e1) {
                try {
                    targetDate = java.time.LocalDate.parse(date.trim(), java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                } catch (Exception ignored) {}
            }
        }
        boolean isToday = targetDate.equals(today);
        java.time.format.DateTimeFormatter isoFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.format.DateTimeFormatter displayFmt = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String selectedDateStr = targetDate.format(isoFmt);
        String selectedDateDisplay = targetDate.format(displayFmt);
        String prevDateStr = targetDate.minusDays(1).format(isoFmt);
        String nextDateStr = targetDate.plusDays(1).format(isoFmt);
        String todayDateStr = today.format(isoFmt);

        try {
            long totalStudents = 0;
            long totalFemale = 0;
            long presentToday = 0;
            long presentFemale = 0;
            long absentToday = 0;
            long absentFemale = 0;
            long absentPermissionToday = 0;
            long absentUnexcusedToday = 0;
            List<com.nubb.classroom.model.Attendance> recentAbsents = new java.util.ArrayList<>();

            String currentYear = getCurrentAcademicYear();
            List<Student> students;
            if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
                students = (effAccountId != null)
                        ? studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, effAccountId, currentYear)
                        : studentRepository.findAll();
            } else {
                students = (effAccountId != null)
                        ? studentRepository.findByAccountIdAndAcademicYear(effAccountId, currentYear)
                        : studentRepository.findAll();
            }

            List<com.nubb.classroom.model.LeaveRequest> allLeaveReqs = getLeaveRequestsForTeacher(effAccountId, className);

            if (students != null) {
                totalStudents = students.size();
                totalFemale = students.stream().filter(s -> s != null && "ស្រី".equals(s.getGender())).count();
                Set<Long> studentIds = students.stream()
                        .filter(s -> s != null && s.getId() != null)
                        .map(Student::getId)
                        .collect(Collectors.toSet());

                List<com.nubb.classroom.model.Attendance> abs;
                if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
                    abs = attendanceRepository.findByDateAndPresentFalseAndStudentClassName(targetDate, className);
                } else {
                    abs = attendanceRepository.findByDateAndPresentFalse(targetDate);
                }

                Map<Long, com.nubb.classroom.model.Attendance> absentByStudentId = new java.util.LinkedHashMap<>();
                if (abs != null) {
                    for (com.nubb.classroom.model.Attendance a : abs) {
                        if (a != null && a.getStudent() != null && a.getStudent().getId() != null
                                && studentIds.contains(a.getStudent().getId())) {
                            absentByStudentId.putIfAbsent(a.getStudent().getId(), a);
                        }
                    }
                }

                if (allLeaveReqs != null) {
                    final java.time.LocalDate checkDate = targetDate;
                    for (com.nubb.classroom.model.LeaveRequest lr : allLeaveReqs) {
                        if (lr != null && !lr.isRejected()
                                && lr.getStudent() != null && lr.getStudent().getId() != null
                                && studentIds.contains(lr.getStudent().getId())
                                && lr.getStartDate() != null && lr.getEndDate() != null
                                && !checkDate.isBefore(lr.getStartDate()) && !checkDate.isAfter(lr.getEndDate())) {
                            absentByStudentId.computeIfAbsent(lr.getStudent().getId(),
                                    sId -> new com.nubb.classroom.model.Attendance(lr.getStudent(), checkDate, false, "PERMISSION"));
                        }
                    }
                }

                recentAbsents = new java.util.ArrayList<>(absentByStudentId.values());
                absentToday = recentAbsents.size();
                absentFemale = recentAbsents.stream()
                        .filter(a -> a != null && a.getStudent() != null && "ស្រី".equals(a.getStudent().getGender()))
                        .count();
                absentPermissionToday = recentAbsents.stream()
                        .filter(com.nubb.classroom.model.Attendance::isPermission)
                        .count();
                absentUnexcusedToday = Math.max(0L, absentToday - absentPermissionToday);

                presentToday = Math.max(0L, totalStudents - absentToday);
                presentFemale = Math.max(0L, totalFemale - absentFemale);
            }

            String presentPercent = "0";
            String absentPercent = "0";
            if (totalStudents > 0) {
                double pPct = (presentToday * 100.0) / totalStudents;
                double aPct = (absentToday * 100.0) / totalStudents;
                presentPercent = (Math.abs(pPct - Math.round(pPct)) < 0.05)
                        ? String.valueOf(Math.round(pPct))
                        : String.format(java.util.Locale.US, "%.1f", pPct);
                absentPercent = (Math.abs(aPct - Math.round(aPct)) < 0.05)
                        ? String.valueOf(Math.round(aPct))
                        : String.format(java.util.Locale.US, "%.1f", aPct);
            }

            List<com.nubb.classroom.model.LeaveRequest> pendingLeaveRequests = allLeaveReqs.stream()
                    .filter(com.nubb.classroom.model.LeaveRequest::isPending)
                    .collect(Collectors.toList());

            model.addAttribute("totalStudents", totalStudents);
            model.addAttribute("totalFemale", totalFemale);
            model.addAttribute("presentToday", presentToday);
            model.addAttribute("presentFemale", presentFemale);
            model.addAttribute("presentPercent", presentPercent);
            model.addAttribute("absentToday", absentToday);
            model.addAttribute("absentFemale", absentFemale);
            model.addAttribute("absentPercent", absentPercent);
            model.addAttribute("absentPermissionToday", absentPermissionToday);
            model.addAttribute("absentUnexcusedToday", absentUnexcusedToday);
            model.addAttribute("recentAbsents", recentAbsents);
            model.addAttribute("pendingLeaveRequests", pendingLeaveRequests);
            model.addAttribute("pendingLeaveCount", pendingLeaveRequests.size());
            model.addAttribute("selectedClass", className);
            model.addAttribute("selectedDate", selectedDateStr);
            model.addAttribute("selectedDateDisplay", selectedDateDisplay);
            model.addAttribute("prevDate", prevDateStr);
            model.addAttribute("nextDate", nextDateStr);
            model.addAttribute("todayDate", todayDateStr);
            model.addAttribute("isToday", isToday);
            return "index";
        } catch (Exception e) {
            System.err.println("Error in index dashboard: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("totalStudents", 0);
            model.addAttribute("totalFemale", 0);
            model.addAttribute("presentToday", 0);
            model.addAttribute("presentFemale", 0);
            model.addAttribute("presentPercent", "0");
            model.addAttribute("absentToday", 0);
            model.addAttribute("absentFemale", 0);
            model.addAttribute("absentPercent", "0");
            model.addAttribute("absentPermissionToday", 0);
            model.addAttribute("absentUnexcusedToday", 0);
            model.addAttribute("recentAbsents", new java.util.ArrayList<>());
            model.addAttribute("selectedClass", className);
            model.addAttribute("selectedDate", selectedDateStr);
            model.addAttribute("selectedDateDisplay", selectedDateDisplay);
            model.addAttribute("prevDate", prevDateStr);
            model.addAttribute("nextDate", nextDateStr);
            model.addAttribute("todayDate", todayDateStr);
            model.addAttribute("isToday", isToday);
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
        if (teacherAccount != null) {
            boolean changed = false;
            if (teacherAccount.getSchoolName() == null || teacherAccount.getSchoolName().isEmpty() || teacherAccount.getSchoolName().contains("វិទ្យាល័យ / អនុវិទ្យាល័យ")) {
                teacherAccount.setSchoolName("វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត");
                changed = true;
            }
            if (teacherAccount.getProvince() == null || teacherAccount.getProvince().isEmpty()) {
                teacherAccount.setProvince("ខេត្តពោធិ៍សាត់");
                changed = true;
            }
            if (teacherAccount.getPrincipalName() == null || teacherAccount.getPrincipalName().isEmpty() || teacherAccount.getPrincipalName().contains("គឹម ហ៊ាង")) {
                teacherAccount.setPrincipalName("លី ប៊ុនថេង");
                changed = true;
            }
            // Clear telegram for other teacher accounts if it was erroneously copied from Sam Kimchheng
            String uname = teacherAccount.getUsername();
            if (uname != null && !uname.equals("samkimchheng") && !uname.equals("admin")) {
                if (teacherAccount.getTelegram() != null && teacherAccount.getTelegram().contains("Kimchheng12")) {
                    teacherAccount.setTelegram(null);
                    changed = true;
                }
            }
            if (changed) {
                try { accountRepository.save(teacherAccount); } catch (Exception ignored) {}
            }
        }
        if (teacherAccount != null && (teacherAccount.getProvince() == null || teacherAccount.getProvince().trim().isEmpty())) {
            teacherAccount.setProvince("ខេត្តពោធិ៍សាត់");
            try { accountRepository.save(teacherAccount); } catch (Exception ignored) {}
        }
        model.addAttribute("teacherAccount", teacherAccount);

        String[] provinces = {
            "ខេត្តពោធិ៍សាត់", "រាជធានីភ្នំពេញ", "ខេត្តបាត់ដំបង", "ខេត្តសៀមរាប", "ខេត្តបន្ទាយមានជ័យ",
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
            @RequestParam(value = "principalName", required = false) String principalName,
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
            if (principalName != null) {
                acc.setPrincipalName(principalName.trim());
            }
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
            redirectAttributes.addFlashAttribute("profileSuccess", "បានរក្សាទុកព័ត៌មានផ្ទាល់ខ្លួនរបស់លោកគ្រូ-អ្នកគ្រូដោយជោគជ័យ!");
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
                    if (src.getPrincipalName() != null) other.setPrincipalName(src.getPrincipalName());
                    accountRepository.save(other);
                }
            }
            if (src.getSchoolName() != null && !com.nubb.classroom.controller.CertificateController.isPlaceholderSchool(src.getSchoolName())) {
                String newSchool = src.getSchoolName().trim();
                List<com.nubb.classroom.model.CertificateRecord> certs = certificateRecordRepository.findAll();
                for (com.nubb.classroom.model.CertificateRecord cr : certs) {
                    cr.setSchoolName(newSchool);
                    certificateRecordRepository.save(cr);
                }
            }
            if (src.getPrincipalName() != null && !src.getPrincipalName().trim().isEmpty() && !src.getPrincipalName().contains("គឹម ហ៊ាង")) {
                String newPrincipal = src.getPrincipalName().trim();
                List<com.nubb.classroom.model.CertificateRecord> certs = certificateRecordRepository.findAll();
                for (com.nubb.classroom.model.CertificateRecord cr : certs) {
                    cr.setPrincipalName(newPrincipal);
                    certificateRecordRepository.save(cr);
                }
            }
        } catch (Exception ignored) {}
    }

    @PostMapping("/api/profile/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiSaveProfile(
            @RequestParam(value = "principalName", required = false) String principalName,
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
            if (principalName != null) {
                acc.setPrincipalName(principalName.trim());
            }
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
                "message", "បានរក្សាទុកព័ត៌មានផ្ទាល់ខ្លួនដោយជោគជ័យ!",
                "fullName", acc.getFullName() != null ? acc.getFullName() : "",
                "principalName", acc.getPrincipalName() != null ? acc.getPrincipalName() : ""
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "រកមិនឃើញគណនីគ្រូបង្រៀន!"));
    }

    public static class YearlyStudentAbsence {
        private Student student;
        // Month breakdowns (Permission / Unexcused)
        private int novPermission;
        private int novUnexcused;

        private int decPermission;
        private int decUnexcused;

        private int janPermission;
        private int janUnexcused;

        private int febPermission;
        private int febUnexcused;

        private int semester1Permission;
        private int semester1Unexcused;
        private int semester1Total;

        private int marPermission;
        private int marUnexcused;

        private int aprPermission;
        private int aprUnexcused;

        private int mayPermission;
        private int mayUnexcused;

        private int junPermission;
        private int junUnexcused;

        private int julPermission;
        private int julUnexcused;

        private int augPermission;
        private int augUnexcused;

        private int semester2Permission;
        private int semester2Unexcused;
        private int semester2Total;

        private int totalYearlyPermission;
        private int totalYearlyUnexcused;
        private int totalYearly;

        public YearlyStudentAbsence(Student student) {
            this.student = student;
        }

        public void addAbsence(int year, int month, boolean isPermission) {
            if (year == 2026 && month == 11) {
                if (isPermission) novPermission++; else novUnexcused++;
            } else if (year == 2026 && month == 12) {
                if (isPermission) decPermission++; else decUnexcused++;
            } else if (year == 2027 && month == 1) {
                if (isPermission) janPermission++; else janUnexcused++;
            } else if (year == 2027 && month == 2) {
                if (isPermission) febPermission++; else febUnexcused++;
            } else if (year == 2027 && month == 3) {
                if (isPermission) marPermission++; else marUnexcused++;
            } else if (year == 2027 && month == 4) {
                if (isPermission) aprPermission++; else aprUnexcused++;
            } else if (year == 2027 && month == 5) {
                if (isPermission) mayPermission++; else mayUnexcused++;
            } else if (year == 2027 && month == 6) {
                if (isPermission) junPermission++; else junUnexcused++;
            } else if (year == 2027 && month == 7) {
                if (isPermission) julPermission++; else julUnexcused++;
            } else if (year == 2027 && month == 8) {
                if (isPermission) augPermission++; else augUnexcused++;
            }
        }

        public void calculateTotals() {
            this.semester1Permission = novPermission + decPermission + janPermission + febPermission;
            this.semester1Unexcused = novUnexcused + decUnexcused + janUnexcused + febUnexcused;
            this.semester1Total = semester1Permission + semester1Unexcused;

            this.semester2Permission = marPermission + aprPermission + mayPermission + junPermission + julPermission + augPermission;
            this.semester2Unexcused = marUnexcused + aprUnexcused + mayUnexcused + junUnexcused + julUnexcused + augUnexcused;
            this.semester2Total = semester2Permission + semester2Unexcused;

            this.totalYearlyPermission = semester1Permission + semester2Permission;
            this.totalYearlyUnexcused = semester1Unexcused + semester2Unexcused;
            this.totalYearly = totalYearlyPermission + totalYearlyUnexcused;
        }

        public Student getStudent() { return student; }
        public void setStudent(Student student) { this.student = student; }

        public int getNovPermission() { return novPermission; }
        public void setNovPermission(int novPermission) { this.novPermission = novPermission; }
        public int getNovUnexcused() { return novUnexcused; }
        public void setNovUnexcused(int novUnexcused) { this.novUnexcused = novUnexcused; }
        public int getNov() { return novPermission + novUnexcused; }

        public int getDecPermission() { return decPermission; }
        public void setDecPermission(int decPermission) { this.decPermission = decPermission; }
        public int getDecUnexcused() { return decUnexcused; }
        public void setDecUnexcused(int decUnexcused) { this.decUnexcused = decUnexcused; }
        public int getDec() { return decPermission + decUnexcused; }

        public int getJanPermission() { return janPermission; }
        public void setJanPermission(int janPermission) { this.janPermission = janPermission; }
        public int getJanUnexcused() { return janUnexcused; }
        public void setJanUnexcused(int janUnexcused) { this.janUnexcused = janUnexcused; }
        public int getJan() { return janPermission + janUnexcused; }

        public int getFebPermission() { return febPermission; }
        public void setFebPermission(int febPermission) { this.febPermission = febPermission; }
        public int getFebUnexcused() { return febUnexcused; }
        public void setFebUnexcused(int febUnexcused) { this.febUnexcused = febUnexcused; }
        public int getFeb() { return febPermission + febUnexcused; }

        public int getSemester1Permission() { return semester1Permission; }
        public void setSemester1Permission(int semester1Permission) { this.semester1Permission = semester1Permission; }
        public int getSemester1Unexcused() { return semester1Unexcused; }
        public void setSemester1Unexcused(int semester1Unexcused) { this.semester1Unexcused = semester1Unexcused; }
        public int getSemester1Total() { return semester1Total; }
        public void setSemester1Total(int semester1Total) { this.semester1Total = semester1Total; }

        public int getMarPermission() { return marPermission; }
        public void setMarPermission(int marPermission) { this.marPermission = marPermission; }
        public int getMarUnexcused() { return marUnexcused; }
        public void setMarUnexcused(int marUnexcused) { this.marUnexcused = marUnexcused; }
        public int getMar() { return marPermission + marUnexcused; }

        public int getAprPermission() { return aprPermission; }
        public void setAprPermission(int aprPermission) { this.aprPermission = aprPermission; }
        public int getAprUnexcused() { return aprUnexcused; }
        public void setAprUnexcused(int aprUnexcused) { this.aprUnexcused = aprUnexcused; }
        public int getApr() { return aprPermission + aprUnexcused; }

        public int getMayPermission() { return mayPermission; }
        public void setMayPermission(int mayPermission) { this.mayPermission = mayPermission; }
        public int getMayUnexcused() { return mayUnexcused; }
        public void setMayUnexcused(int mayUnexcused) { this.mayUnexcused = mayUnexcused; }
        public int getMay() { return mayPermission + mayUnexcused; }

        public int getJunPermission() { return junPermission; }
        public void setJunPermission(int junPermission) { this.junPermission = junPermission; }
        public int getJunUnexcused() { return junUnexcused; }
        public void setJunUnexcused(int junUnexcused) { this.junUnexcused = junUnexcused; }
        public int getJun() { return junPermission + junUnexcused; }

        public int getJulPermission() { return julPermission; }
        public void setJulPermission(int julPermission) { this.julPermission = julPermission; }
        public int getJulUnexcused() { return julUnexcused; }
        public void setJulUnexcused(int julUnexcused) { this.julUnexcused = julUnexcused; }
        public int getJul() { return julPermission + julUnexcused; }

        public int getAugPermission() { return augPermission; }
        public void setAugPermission(int augPermission) { this.augPermission = augPermission; }
        public int getAugUnexcused() { return augUnexcused; }
        public void setAugUnexcused(int augUnexcused) { this.augUnexcused = augUnexcused; }
        public int getAug() { return augPermission + augUnexcused; }

        public int getSemester2Permission() { return semester2Permission; }
        public void setSemester2Permission(int semester2Permission) { this.semester2Permission = semester2Permission; }
        public int getSemester2Unexcused() { return semester2Unexcused; }
        public void setSemester2Unexcused(int semester2Unexcused) { this.semester2Unexcused = semester2Unexcused; }
        public int getSemester2Total() { return semester2Total; }
        public void setSemester2Total(int semester2Total) { this.semester2Total = semester2Total; }

        public int getTotalYearlyPermission() { return totalYearlyPermission; }
        public void setTotalYearlyPermission(int totalYearlyPermission) { this.totalYearlyPermission = totalYearlyPermission; }
        public int getTotalYearlyUnexcused() { return totalYearlyUnexcused; }
        public void setTotalYearlyUnexcused(int totalYearlyUnexcused) { this.totalYearlyUnexcused = totalYearlyUnexcused; }
        public int getTotalYearly() { return totalYearly; }
        public void setTotalYearly(int totalYearly) { this.totalYearly = totalYearly; }
    }

    public static int[] parseMonthAndYear(String monthStr) {
        int monthNum = 11;
        int year = 2026;
        if (monthStr == null || monthStr.isEmpty()) {
            return new int[]{11, 2026};
        }
        if (monthStr.contains("វិច្ឆិកា")) { monthNum = 11; year = 2026; }
        else if (monthStr.contains("ធ្នូ")) { monthNum = 12; year = 2026; }
        else if (monthStr.contains("មករា")) { monthNum = 1; year = 2027; }
        else if (monthStr.contains("កុម្ភៈ")) { monthNum = 2; year = 2027; }
        else if (monthStr.contains("មីនា")) { monthNum = 3; year = 2027; }
        else if (monthStr.contains("មេសា")) { monthNum = 4; year = 2027; }
        else if (monthStr.contains("ឧសភា")) { monthNum = 5; year = 2027; }
        else if (monthStr.contains("មិថុនា")) { monthNum = 6; year = 2027; }
        else if (monthStr.contains("កក្កដា")) { monthNum = 7; year = 2027; }
        else if (monthStr.contains("សីហា")) { monthNum = 8; year = 2027; }
        else if (monthStr.contains("កញ្ញា")) { monthNum = 9; year = 2027; }
        else if (monthStr.contains("តុលា")) { monthNum = 10; year = 2026; }

        int extractedYear = extractYear(monthStr);
        if (extractedYear > 2000 && extractedYear < 2100) {
            year = extractedYear;
        }

        return new int[]{monthNum, year};
    }

    public static int extractYear(String str) {
        if (str == null) return 0;
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c >= '០' && c <= '៩') {
                sb.append((char) ('0' + (c - '០')));
            } else if (c >= '0' && c <= '9') {
                sb.append(c);
            } else {
                if (sb.length() >= 4) break;
                sb.setLength(0);
            }
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    @GetMapping("/attendance")
    public String attendance(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String month,
            Model model) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null || accountId == 1L) {
            accountId = 97L;
        }

        String currentYear = getCurrentAcademicYear();
        int[] years = parseAcademicYearNumbers(currentYear);
        if (month == null || month.trim().isEmpty()) {
            month = "ខែវិច្ឆិកា " + toKhmerNumber(years[0]);
        } else {
            month = month.trim();
        }

        boolean isKhmerSorted = isKhmerSortActive(accountId, null);
        List<Student> students = getOrderedStudentsForTeacher(accountId, className, currentYear, isKhmerSorted);

        boolean isYearly = "ប្រចាំឆ្នាំ".equals(month);
        boolean isSemester1 = "ឆមាសទី១".equals(month);
        boolean isSemester2 = "ឆមាសទី២".equals(month);

        if (isYearly || isSemester1 || isSemester2) {
            List<YearlyStudentAbsence> yearlyList = new ArrayList<>();
            LocalDate yrStart = LocalDate.of(years[0], 10, 1);
            LocalDate yrEnd = LocalDate.of(years[1], 9, 30);
            if (isSemester1) {
                yrStart = LocalDate.of(years[0], 11, 1);
                yrEnd = LocalDate.of(years[1], 2, 28);
            } else if (isSemester2) {
                yrStart = LocalDate.of(years[1], 3, 1);
                yrEnd = LocalDate.of(years[1], 8, 31);
            }

            List<Attendance> allAbsences = Collections.emptyList();
            if (students != null && !students.isEmpty()) {
                allAbsences = attendanceRepository.findByStudentInAndDateBetween(students, yrStart, yrEnd);
            }

            Map<Long, List<Attendance>> studentAttMap = allAbsences.stream()
                    .filter(a -> !a.isPresent())
                    .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

            for (Student s : students) {
                YearlyStudentAbsence row = new YearlyStudentAbsence(s);
                List<Attendance> sAtts = studentAttMap.getOrDefault(s.getId(), Collections.emptyList());
                for (Attendance a : sAtts) {
                    LocalDate d = a.getDate();
                    row.addAbsence(d.getYear(), d.getMonthValue(), a.isPermission());
                }
                row.calculateTotals();
                yearlyList.add(row);
            }

            model.addAttribute("yearlyAbsences", yearlyList);
        } else {
            int[] my = parseMonthAndYear(month);
            int monthNum = my[0];
            int year = my[1];
            LocalDate start = LocalDate.of(year, monthNum, 1);
            LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

            List<Attendance> monthAtts = Collections.emptyList();
            if (students != null && !students.isEmpty()) {
                monthAtts = attendanceRepository.findByStudentInAndDateBetween(students, start, end);
            }

            Map<String, String> absentDayTypes = new HashMap<>();
            Map<Long, Integer> studentPermissionCounts = new HashMap<>();
            Map<Long, Integer> studentUnexcusedCounts = new HashMap<>();
            Map<Long, Integer> studentTotalCounts = new HashMap<>();

            for (Attendance a : monthAtts) {
                if (!a.isPresent()) {
                    Long sId = a.getStudent().getId();
                    int day = a.getDate().getDayOfMonth();
                    String type = a.isPermission() ? "PERMISSION" : "UNEXCUSED";
                    absentDayTypes.put(sId + "_" + day, type);
                    if ("PERMISSION".equals(type)) {
                        studentPermissionCounts.put(sId, studentPermissionCounts.getOrDefault(sId, 0) + 1);
                    } else {
                        studentUnexcusedCounts.put(sId, studentUnexcusedCounts.getOrDefault(sId, 0) + 1);
                    }
                    studentTotalCounts.put(sId, studentTotalCounts.getOrDefault(sId, 0) + 1);
                }
            }

            List<com.nubb.classroom.model.LeaveRequest> leaveRequests = getLeaveRequestsForTeacher(accountId, className);
            Map<String, String> leaveReasonTooltips = new HashMap<>();
            if (leaveRequests != null) {
                for (com.nubb.classroom.model.LeaveRequest lr : leaveRequests) {
                    if (lr.getStudent() == null || lr.getStudent().getId() == null
                            || lr.getStartDate() == null || lr.getEndDate() == null) continue;
                    if (lr.getEndDate().isBefore(start) || lr.getStartDate().isAfter(end)) continue;

                    Long sId = lr.getStudent().getId();
                    LocalDate d = lr.getStartDate().isBefore(start) ? start : lr.getStartDate();
                    LocalDate dEnd = lr.getEndDate().isAfter(end) ? end : lr.getEndDate();
                    int guard = 0;
                    while (!d.isAfter(dEnd) && guard < 35) {
                        int day = d.getDayOfMonth();
                        String key = sId + "_" + day;
                        String tip = "សុំច្បាប់ (" + (lr.getLeaveType() != null ? lr.getLeaveType() : "ច្បាប់") + ")៖ "
                                + (lr.getReason() != null ? lr.getReason() : "")
                                + " [" + lr.getStatusKhmer() + "]";
                        leaveReasonTooltips.put(key, tip);

                        if (!lr.isRejected() && !absentDayTypes.containsKey(key)) {
                            absentDayTypes.put(key, "PERMISSION");
                            studentPermissionCounts.put(sId, studentPermissionCounts.getOrDefault(sId, 0) + 1);
                            studentTotalCounts.put(sId, studentTotalCounts.getOrDefault(sId, 0) + 1);
                        }
                        d = d.plusDays(1);
                        guard++;
                    }
                }
            }

            model.addAttribute("absentDayTypes", absentDayTypes);
            model.addAttribute("leaveReasonTooltips", leaveReasonTooltips);
            model.addAttribute("studentPermissionCounts", studentPermissionCounts);
            model.addAttribute("studentUnexcusedCounts", studentUnexcusedCounts);
            model.addAttribute("studentTotalCounts", studentTotalCounts);
        }

        List<com.nubb.classroom.model.LeaveRequest> teacherLeaveRequests = getLeaveRequestsForTeacher(accountId, className);
        long pendingLeaveCount = teacherLeaveRequests.stream().filter(com.nubb.classroom.model.LeaveRequest::isPending).count();

        model.addAttribute("leaveRequests", teacherLeaveRequests);
        model.addAttribute("pendingLeaveCount", pendingLeaveCount);
        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("isYearly", isYearly);
        model.addAttribute("isSemester1", isSemester1);
        model.addAttribute("isSemester2", isSemester2);

        return "attendance";
    }

    @PostMapping("/attendance/leave-request/{id}/approve")
    public String approveLeaveRequest(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "teacherNote", required = false) String teacherNote,
            RedirectAttributes redirectAttributes) {

        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isPresent()) {
            com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
            lr.setStatus("APPROVED");
            lr.setReviewedAt(java.time.LocalDateTime.now());
            if (teacherNote != null && !teacherNote.trim().isEmpty()) {
                lr.setTeacherNote(teacherNote.trim());
            }
            leaveRequestRepository.save(lr);
            syncLeaveRequestToAttendance(lr, true);
            String stName = lr.getStudent() != null ? lr.getStudent().getName() : "សិស្ស";
            redirectAttributes.addFlashAttribute("successMessage", "✅ បានអនុម័តច្បាប់ឈប់សម្រាករបស់សិស្ស «" + stName + "» និងកត់ត្រាចូលបញ្ជីអវត្តមានរួចរាល់!");
        }
        String safeClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ថ្នាក់រៀនទាំងអស់";
        String safeMonth = (month != null && !month.trim().isEmpty()) ? month.trim() : "";
        return "redirect:/attendance?className=" + URLEncoder.encode(safeClass, StandardCharsets.UTF_8)
                + (safeMonth.isEmpty() ? "" : "&month=" + URLEncoder.encode(safeMonth, StandardCharsets.UTF_8));
    }

    @PostMapping("/attendance/leave-request/{id}/reject")
    public String rejectLeaveRequest(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "teacherNote", required = false) String teacherNote,
            RedirectAttributes redirectAttributes) {

        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isPresent()) {
            com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
            lr.setStatus("REJECTED");
            lr.setReviewedAt(java.time.LocalDateTime.now());
            if (teacherNote != null && !teacherNote.trim().isEmpty()) {
                lr.setTeacherNote(teacherNote.trim());
            }
            leaveRequestRepository.save(lr);
            syncLeaveRequestToAttendance(lr, false);
            String stName = lr.getStudent() != null ? lr.getStudent().getName() : "សិស្ស";
            redirectAttributes.addFlashAttribute("successMessage", "❌ បានបដិសេធសំណើសុំច្បាប់របស់សិស្ស «" + stName + "»!");
        }
        String safeClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ថ្នាក់រៀនទាំងអស់";
        String safeMonth = (month != null && !month.trim().isEmpty()) ? month.trim() : "";
        return "redirect:/attendance?className=" + URLEncoder.encode(safeClass, StandardCharsets.UTF_8)
                + (safeMonth.isEmpty() ? "" : "&month=" + URLEncoder.encode(safeMonth, StandardCharsets.UTF_8));
    }

    @PostMapping("/attendance/leave-request/{id}/delete")
    public String deleteLeaveRequest(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "month", required = false) String month,
            RedirectAttributes redirectAttributes) {

        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isPresent()) {
            com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
            boolean wasApproved = lr.isApproved();
            leaveRequestRepository.delete(lr);
            if (!wasApproved) {
                syncLeaveRequestToAttendance(lr, false);
            }
            redirectAttributes.addFlashAttribute("successMessage", "🗑️ បានលុបសំណើសុំច្បាប់ដោយជោគជ័យ!");
        }
        String safeClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ថ្នាក់រៀនទាំងអស់";
        String safeMonth = (month != null && !month.trim().isEmpty()) ? month.trim() : "";
        return "redirect:/attendance?className=" + URLEncoder.encode(safeClass, StandardCharsets.UTF_8)
                + (safeMonth.isEmpty() ? "" : "&month=" + URLEncoder.encode(safeMonth, StandardCharsets.UTF_8));
    }

    @GetMapping("/api/leave-requests/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLeaveRequestNotifications() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("pendingCount", 0, "requests", Collections.emptyList()));
        }
        String role = (String) session.getAttribute("ACCOUNT_ROLE");
        List<com.nubb.classroom.model.LeaveRequest> allVisible;
        if ("ADMIN".equalsIgnoreCase(role) || accountId == 1L) {
            allVisible = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
        } else {
            allVisible = getLeaveRequestsForTeacher(accountId, null);
        }

        Map<Long, String> teacherNames = new HashMap<>();
        try {
            for (Account acc : accountRepository.findAll()) {
                if (acc.getId() != null && acc.getFullName() != null) {
                    teacherNames.put(acc.getId(), acc.getFullName());
                }
            }
        } catch (Exception ignored) {}

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<Map<String, Object>> pendingList = new ArrayList<>();
        for (com.nubb.classroom.model.LeaveRequest lr : allVisible) {
            if (lr == null || !lr.isPending()) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", lr.getId());
            item.put("studentName", lr.getStudent() != null && lr.getStudent().getName() != null ? lr.getStudent().getName() : "សិស្ស");
            item.put("studentCode", lr.getStudent() != null && lr.getStudent().getStudentCode() != null ? lr.getStudent().getStudentCode() : "");
            item.put("gender", lr.getStudent() != null && lr.getStudent().getGender() != null ? lr.getStudent().getGender() : "");
            String cls = lr.getClassName() != null ? lr.getClassName() : (lr.getStudent() != null && lr.getStudent().getClassName() != null ? lr.getStudent().getClassName() : "");
            item.put("className", cls);
            Long tId = lr.getAccountId() != null ? lr.getAccountId() : 97L;
            if (tId == 1L) tId = 97L;
            item.put("teacherName", teacherNames.getOrDefault(tId, "គ្រូបង្រៀន"));
            String sDate = lr.getStartDate() != null ? lr.getStartDate().format(dtf) : "";
            String eDate = lr.getEndDate() != null ? lr.getEndDate().format(dtf) : sDate;
            item.put("startDate", sDate);
            item.put("endDate", eDate);
            item.put("dateRange", (!eDate.isEmpty() && !eDate.equals(sDate)) ? (sDate + " ដល់ " + eDate) : sDate);
            item.put("totalDays", lr.getTotalDays());
            item.put("leaveType", lr.getLeaveType() != null ? lr.getLeaveType() : "ច្បាប់");
            item.put("leaveSession", lr.getLeaveSession() != null ? lr.getLeaveSession() : "ពេញមួយថ្ងៃ");
            item.put("reason", lr.getReason() != null ? lr.getReason() : "");
            item.put("parentPhone", lr.getParentPhone() != null ? lr.getParentPhone() : "");
            pendingList.add(item);
        }

        return ResponseEntity.ok(Map.of(
                "pendingCount", pendingList.size(),
                "totalCount", allVisible.size(),
                "requests", pendingList
        ));
    }

    @PostMapping("/api/leave-requests/{id}/approve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiApproveLeaveRequest(@org.springframework.web.bind.annotation.PathVariable Long id) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញសំណើសុំច្បាប់នេះទេ"));
        }
        com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
        lr.setStatus("APPROVED");
        lr.setReviewedAt(java.time.LocalDateTime.now());
        leaveRequestRepository.save(lr);
        syncLeaveRequestToAttendance(lr, true);
        String stName = lr.getStudent() != null ? lr.getStudent().getName() : "សិស្ស";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✅ បានអនុម័តច្បាប់ឈប់សម្រាករបស់សិស្ស «" + stName + "» រួចរាល់!"
        ));
    }

    @PostMapping("/api/leave-requests/{id}/reject")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiRejectLeaveRequest(@org.springframework.web.bind.annotation.PathVariable Long id) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញសំណើសុំច្បាប់នេះទេ"));
        }
        com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
        lr.setStatus("REJECTED");
        lr.setReviewedAt(java.time.LocalDateTime.now());
        leaveRequestRepository.save(lr);
        syncLeaveRequestToAttendance(lr, false);
        String stName = lr.getStudent() != null ? lr.getStudent().getName() : "សិស្ស";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "❌ បានបដិសេធសំណើសុំច្បាប់របស់សិស្ស «" + stName + "»!"
        ));
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(
            @RequestParam("month") String month,
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "attendanceData", required = false) List<String> attendanceData,
            RedirectAttributes redirectAttributes) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            accountId = 97L;
        }

        String safeClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ថ្នាក់រៀនទាំងអស់";
        String safeMonth = (month != null && !month.trim().isEmpty()) ? month.trim() : "ខែវិច្ឆិកា ២០២៦";

        if ("ប្រចាំឆ្នាំ".equals(safeMonth) || "ឆមាសទី១".equals(safeMonth) || "ឆមាសទី២".equals(safeMonth) || "តេស្តដើមឆ្នាំ".equals(safeMonth)) {
            redirectAttributes.addFlashAttribute("successMessage", "ℹ️ ទិន្នន័យ " + safeMonth + " ជាទិន្នន័យសរុបស្វ័យប្រវត្តិនៃអវត្តមានសិស្ស!");
            return "redirect:/attendance?className=" + URLEncoder.encode(safeClass, StandardCharsets.UTF_8) + "&month=" + URLEncoder.encode(safeMonth, StandardCharsets.UTF_8);
        }

        int[] my = parseMonthAndYear(safeMonth);
        int monthNum = my[0];
        int year = my[1];

        LocalDate start = LocalDate.of(year, monthNum, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());

        String currentYear = getCurrentAcademicYear();
        List<Student> students;
        if (!safeClass.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountIdAndAcademicYear(safeClass, accountId, currentYear);
        } else {
            students = studentRepository.findByAccountIdAndAcademicYear(accountId, currentYear);
        }

        if (students != null && !students.isEmpty()) {
            try {
                attendanceRepository.deleteByStudentInAndDateBetween(students, start, end);
            } catch (Exception ignored) {}
        }

        if (attendanceData != null && !attendanceData.isEmpty()) {
            Map<Long, Student> studentMap = (students != null) ? students.stream().collect(Collectors.toMap(Student::getId, s -> s, (s1, s2) -> s1)) : Collections.emptyMap();
            List<Attendance> toSave = new ArrayList<>();
            for (String item : attendanceData) {
                if (item == null || item.trim().isEmpty()) continue;
                String[] parts = item.trim().split("_");
                if (parts.length >= 2) {
                    try {
                        Long sId = Long.parseLong(parts[0]);
                        int day = Integer.parseInt(parts[1]);
                        String type = (parts.length >= 3) ? parts[2] : "UNEXCUSED";
                        if (!"PERMISSION".equalsIgnoreCase(type) && !"UNEXCUSED".equalsIgnoreCase(type)) {
                            type = "UNEXCUSED";
                        }
                        Student st = studentMap.get(sId);
                        if (st == null) {
                            st = studentRepository.findById(sId).orElse(null);
                        }
                        if (st != null && day >= 1 && day <= end.getDayOfMonth()) {
                            LocalDate attDate = LocalDate.of(year, monthNum, day);
                            Attendance att = new Attendance(st, attDate, false, type);
                            att.setAccountId(accountId);
                            toSave.add(att);
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (!toSave.isEmpty()) {
                attendanceRepository.saveAll(toSave);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "✅ បានរក្សាទុកទិន្នន័យអវត្តមាន " + safeMonth + " សម្រាប់ " + safeClass + " ដោយជោគជ័យ!");
        return "redirect:/attendance?className=" + URLEncoder.encode(safeClass, StandardCharsets.UTF_8) + "&month=" + URLEncoder.encode(safeMonth, StandardCharsets.UTF_8);
    }

    public static String cleanKhmerText(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .replace("\u17C1\u17B6", "\u17C4")
                .replace("\u17C1\u17B8", "\u17BE")
                .replace("\u17C6\u17BB", "\u17BB\u17C6")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public static String cleanKhmerForPrimarySort(String s) {
        return cleanKhmerText(s).replaceAll("[\\u17C9-\\u17D1\\u17DD]", "");
    }

    public static int extractClassGradeOrder(String className) {
        if (className == null) return 999;
        String cleaned = cleanKhmerText(className);
        StringBuilder digits = new StringBuilder();
        for (char ch : cleaned.toCharArray()) {
            if (ch >= '០' && ch <= '៩') {
                digits.append((char) ('0' + (ch - '០')));
            } else if (ch >= '0' && ch <= '9') {
                digits.append(ch);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() > 0) {
            try {
                return Integer.parseInt(digits.toString());
            } catch (Exception ignored) {}
        }
        return 999;
    }

    public static String normalizeClassSuffix(String className) {
        if (className == null) return "";
        String cleaned = cleanKhmerText(className)
                .replace("ថ្នាក់ទី", "")
                .replace("ថ្នាក់", "")
                .trim();
        StringBuilder sb = new StringBuilder();
        for (char ch : cleaned.toCharArray()) {
            if (ch >= '០' && ch <= '៩') {
                sb.append((char) ('0' + (ch - '០')));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString().trim().toUpperCase();
    }

    public static int compareKhmerStrings(String a, String b) {
        String s1 = cleanKhmerText(a);
        String s2 = cleanKhmerText(b);
        if (s1.equals(s2)) return 0;
        if (s1.isEmpty()) return 1;
        if (s2.isEmpty()) return -1;

        String p1 = cleanKhmerForPrimarySort(s1);
        String p2 = cleanKhmerForPrimarySort(s2);
        int primaryCmp = p1.compareTo(p2);
        if (primaryCmp != 0) {
            return primaryCmp;
        }
        return s1.compareTo(s2);
    }

    public static List<Student> sortStudentsByClassAndKhmerAlphabet(List<Student> students) {
        if (students == null || students.size() <= 1) return students;
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort((s1, s2) -> {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return 1;
            if (s2 == null) return -1;

            String c1 = s1.getClassName() != null ? s1.getClassName() : "";
            String c2 = s2.getClassName() != null ? s2.getClassName() : "";
            int g1 = extractClassGradeOrder(c1);
            int g2 = extractClassGradeOrder(c2);
            if (g1 != g2) {
                return Integer.compare(g1, g2);
            }
            String normC1 = normalizeClassSuffix(c1);
            String normC2 = normalizeClassSuffix(c2);
            int classCmp = compareKhmerStrings(normC1, normC2);
            if (classCmp != 0) {
                return classCmp;
            }

            int nameCmp = compareKhmerStrings(s1.getName(), s2.getName());
            if (nameCmp != 0) {
                return nameCmp;
            }
            Long id1 = s1.getId() != null ? s1.getId() : 0L;
            Long id2 = s2.getId() != null ? s2.getId() : 0L;
            return id1.compareTo(id2);
        });
        return sorted;
    }

    private static final Map<Long, Boolean> ACCOUNT_KHMER_SORT_PREF = new java.util.concurrent.ConcurrentHashMap<>();

    public boolean isKhmerSortActive(Long accountId, String sortParam) {
        Long effId = (accountId == null || accountId == 1L) ? 97L : accountId;
        if (sortParam != null && !sortParam.trim().isEmpty()) {
            boolean enable = !"default".equalsIgnoreCase(sortParam.trim()) && !"none".equalsIgnoreCase(sortParam.trim());
            ACCOUNT_KHMER_SORT_PREF.put(effId, enable);
            try {
                if (enable) {
                    session.setAttribute("STUDENT_SORT_MODE", "khmer");
                } else {
                    session.setAttribute("STUDENT_SORT_MODE", "default");
                }
            } catch (Exception ignored) {}
            return enable;
        }
        try {
            String sessMode = (String) session.getAttribute("STUDENT_SORT_MODE");
            if ("default".equalsIgnoreCase(sessMode)) {
                return false;
            }
            if ("khmer".equalsIgnoreCase(sessMode)) {
                ACCOUNT_KHMER_SORT_PREF.put(effId, true);
                return true;
            }
        } catch (Exception ignored) {}
        return ACCOUNT_KHMER_SORT_PREF.getOrDefault(effId, true);
    }

    public List<Student> getOrderedStudentsForTeacher(Long accountId, String className, String currentYear, boolean sortKhmer) {
        Long effId = (accountId == null || accountId == 1L) ? 97L : accountId;
        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, effId, currentYear);
        } else {
            students = studentRepository.findByAccountIdAndAcademicYear(effId, currentYear);
        }
        if (sortKhmer && students != null) {
            students = sortStudentsByClassAndKhmerAlphabet(students);
        }
        return students;
    }

    @GetMapping("/students")
    public String students(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String sort,
            Model model) {
        String currentYear = getCurrentAcademicYear();
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null || accountId == 1L) accountId = 97L;

        boolean isKhmerSorted = isKhmerSortActive(accountId, sort);
        List<Student> students = getOrderedStudentsForTeacher(accountId, className, currentYear, isKhmerSorted);

        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
        model.addAttribute("sortMode", isKhmerSorted ? "khmer" : "default");
        model.addAttribute("isKhmerSorted", isKhmerSorted);
        return "students";
    }

    @GetMapping("/student/add")
    public String addStudentForm(Model model) {
        Student st = new Student();
        st.setAcademicYear(getCurrentAcademicYear());
        model.addAttribute("student", st);
        return "add-student";
    }

    @PostMapping("/student/add")
    public String addStudentSubmit(Student student) {
        Long accId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accId == null) {
            return "redirect:/login";
        }
        if (accId == 1L) {
            accId = 97L;
        }
        student.setAccountId(accId);
        if (student.getStudentCode() == null || student.getStudentCode().trim().isEmpty()) {
            student.setStudentCode("STU-" + (1000 + studentRepository.count() + 1));
        } else {
            student.setStudentCode(student.getStudentCode().trim());
        }
        if (student.getAcademicYear() == null || student.getAcademicYear().trim().isEmpty()) {
            student.setAcademicYear(getCurrentAcademicYear());
        }
        studentRepository.save(student);
        return "redirect:/students";
    }

    @PostMapping("/api/students/import-batch")
    @ResponseBody
    public ResponseEntity<?> importStudentsBatch(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> payload) {
        Long accId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }
        if (accId == 1L) accId = 97L;

        String targetClass = (String) payload.get("className");
        if (targetClass == null || targetClass.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "សូមជ្រើសរើស ឬបញ្ចូលឈ្មោះថ្នាក់រៀន!"));
        }
        targetClass = targetClass.trim();

        String currentYear = getCurrentAcademicYear();
        Boolean overwrite = Boolean.TRUE.equals(payload.get("overwrite"));
        List<Map<String, Object>> studentsRaw = (List<Map<String, Object>>) payload.get("students");

        if (studentsRaw == null || studentsRaw.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "មិនមានទិន្នន័យសិស្សសម្រាប់បញ្ចូលទេ!"));
        }

        // Auto-register classroom if it doesn't exist for this teacher
        if (!classroomRepository.existsByClassNameAndAccountIdAndAcademicYear(targetClass, accId, currentYear)) {
            Classroom newCls = new Classroom();
            newCls.setClassName(targetClass);
            newCls.setAccountId(accId);
            newCls.setAcademicYear(currentYear);
            newCls.setTrack(newCls.getTrack());
            classroomRepository.save(newCls);
        }

        // If overwrite is requested, clear old records for this class
        if (overwrite) {
            List<Student> existingInClass = studentRepository.findByClassNameAndAccountIdAndAcademicYear(targetClass, accId, currentYear);
            if (existingInClass != null && !existingInClass.isEmpty()) {
                for (Student s : existingInClass) {
                    try { leaveRequestRepository.deleteByStudent(s); } catch (Exception ignored) {}
                    try { attendanceRepository.deleteByStudent(s); } catch (Exception ignored) {}
                    try { scoreRepository.deleteByStudentId(s.getId()); } catch (Exception ignored) {}
                }
                studentRepository.deleteAll(existingInClass);
            }
        }

        long baseCount = studentRepository.count();
        List<Student> toSave = new ArrayList<>();
        int importedCount = 0;
        int femaleCount = 0;

        for (int i = 0; i < studentsRaw.size(); i++) {
            Map<String, Object> row = studentsRaw.get(i);
            String name = row.get("name") != null ? row.get("name").toString().trim() : "";
            if (name.isEmpty()) continue;

            String gender = row.get("gender") != null ? row.get("gender").toString().trim() : "ប្រុស";
            if (gender.equalsIgnoreCase("F") || gender.equalsIgnoreCase("Female") || gender.equals("ស") || gender.contains("ស្រី")) {
                gender = "ស្រី";
            } else {
                gender = "ប្រុស";
            }
            if ("ស្រី".equals(gender)) femaleCount++;

            String code = row.get("studentCode") != null ? row.get("studentCode").toString().trim() : "";
            if (code.isEmpty()) {
                code = "STU-" + (1000 + baseCount + toSave.size() + 1);
            }

            Student s = new Student();
            s.setName(name);
            s.setGender(gender);
            s.setClassName(targetClass);
            s.setStudentCode(code);
            s.setAccountId(accId);
            s.setAcademicYear(currentYear);

            s.setVillage(row.get("village") != null ? row.get("village").toString().trim() : "");
            s.setCommune(row.get("commune") != null ? row.get("commune").toString().trim() : "");
            s.setDistrict(row.get("district") != null ? row.get("district").toString().trim() : "");
            s.setProvince(row.get("province") != null ? row.get("province").toString().trim() : "");
            s.setFatherName(row.get("fatherName") != null ? row.get("fatherName").toString().trim() : "");
            s.setFatherOccupation(row.get("fatherOccupation") != null ? row.get("fatherOccupation").toString().trim() : "");
            s.setMotherName(row.get("motherName") != null ? row.get("motherName").toString().trim() : "");
            s.setMotherOccupation(row.get("motherOccupation") != null ? row.get("motherOccupation").toString().trim() : "");
            s.setStatus(row.get("status") != null && !row.get("status").toString().trim().isEmpty() ? row.get("status").toString().trim() : "កំពុងរៀន");

            toSave.add(s);
            importedCount++;
        }

        if (toSave.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "រកមិនឃើញទិន្នន័យសិស្សដែលមានឈ្មោះត្រឹមត្រូវទេ!"));
        }

        studentRepository.saveAll(toSave);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "បានបញ្ចូលសិស្សដោយជោគជ័យចំនួន " + importedCount + " នាក់ (ស្រី " + femaleCount + " នាក់) ទៅក្នុងថ្នាក់ " + targetClass + "!",
                "count", importedCount,
                "femaleCount", femaleCount,
                "className", targetClass
        ));
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
            if (student.getAcademicYear() != null && !student.getAcademicYear().trim().isEmpty()) {
                existing.setAcademicYear(student.getAcademicYear().trim());
            } else if (existing.getAcademicYear() == null || existing.getAcademicYear().trim().isEmpty()) {
                existing.setAcademicYear(getCurrentAcademicYear());
            }
            Long accId = (Long) session.getAttribute("ACCOUNT_ID");
            if (accId != null) {
                if (accId == 1L) accId = 97L;
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
        try { leaveRequestRepository.deleteByStudent(student); } catch (Exception ignored) {}
        attendanceRepository.deleteByStudent(student);
        scoreRepository.deleteByStudentId(id);
        studentRepository.delete(student);
        return "redirect:/students";
    }

    @GetMapping("/scores")
    public String scores(@org.springframework.web.bind.annotation.RequestParam(required = false) String className, Model model) {
        String currentYear = getCurrentAcademicYear();
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null || accountId == 1L) accountId = 97L;
        boolean isKhmerSorted = isKhmerSortActive(accountId, null);
        List<Student> students = getOrderedStudentsForTeacher(accountId, className, currentYear, isKhmerSorted);
        model.addAttribute("students", students);
        model.addAttribute("selectedClass", className);
                // Determine track for selected classroom
        String selectedClassTrack = "ទូទៅ";
        if (className != null && !className.isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(className)) {
            List<Classroom> found = classroomRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, currentYear);
            if (found != null && !found.isEmpty()) {
                selectedClassTrack = found.get(0).getTrack();
            } else {
                Classroom dummy = new Classroom();
                dummy.setClassName(className);
                selectedClassTrack = dummy.getTrack();
            }
        }
        model.addAttribute("selectedClassTrack", selectedClassTrack);
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

                public static boolean isSameSubject(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        String t1 = s1.trim();
        String t2 = s2.trim();
        if (t1.equalsIgnoreCase(t2)) return true;
        if ((t1.contains("ជីវវិទ្យា") || t1.contains("ជីវៈវិទ្យា")) && (t2.contains("ជីវវិទ្យា") || t2.contains("ជីវៈវិទ្យា"))) return true;
        if ((t1.contains("ភាសាខ្មែរ") || t1.contains("អក្សរសិល្ប៍")) && (t2.contains("ភាសាខ្មែរ") || t2.contains("អក្សរសិល្ប៍"))) return true;
        if ((t1.contains("សីលធម៌") || t1.contains("ពលរដ្ឋ")) && (t2.contains("សីលធម៌") || t2.contains("ពលរដ្ឋ"))) return true;
        if (t1.contains("ផែនដី") && t2.contains("ផែនដី")) return true;
        if ((t1.contains("កុំព្យូទ័រ") || t1.contains("ព័ត៌មានវិទ្យា")) && (t2.contains("កុំព្យូទ័រ") || t2.contains("ព័ត៌មានវិទ្យា"))) return true;
        if ((t1.contains("អង់គ្លេស") || t1.equalsIgnoreCase("english")) && (t2.contains("អង់គ្លេស") || t2.equalsIgnoreCase("english"))) return true;
        if ((t1.contains("បារាំង") || t1.equalsIgnoreCase("french")) && (t2.contains("បារាំង") || t2.equalsIgnoreCase("french"))) return true;
        return false;
    }

    private double computeDefaultMaxScoreForClass(String className, String subjectName, Long accountId, String academicYear) {
        if (className == null || className.trim().isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(className)) {
            return 100.0;
        }
        if (accountId == null || accountId == 1L) {
            accountId = 97L;
        }
        String track = "ទូទៅ";
        List<Classroom> found = classroomRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, academicYear);
        if (found != null && !found.isEmpty()) {
            track = found.get(0).getTrack();
        } else {
            Classroom dummy = new Classroom();
            dummy.setClassName(className);
            track = dummy.getTrack();
        }

        String sub = (subjectName != null) ? subjectName.trim() : "";

        if ("វិទ្យាសាស្ត្រ".equals(track)) {
            if (isSameSubject("គណិតវិទ្យា", sub)) {
                return 125.0;
            } else if (isSameSubject("រូបវិទ្យា", sub) || isSameSubject("គីមីវិទ្យា", sub) || isSameSubject("ជីវវិទ្យា", sub) || isSameSubject("ភាសាខ្មែរ", sub)) {
                return 75.0;
            } else if (isSameSubject("ភាសាអង់គ្លេស", sub) || isSameSubject("ភាសាបារាំង", sub) || isSameSubject("ប្រវត្តិវិទ្យា", sub)) {
                return 50.0;
            }
            return 100.0;
        } else if ("វិទ្យាសាស្ត្រសង្គម".equals(track)) {
            if (isSameSubject("ភាសាខ្មែរ", sub)) {
                return 125.0;
            } else if (isSameSubject("គណិតវិទ្យា", sub) || isSameSubject("ប្រវត្តិវិទ្យា", sub) || isSameSubject("ភូមិវិទ្យា", sub) || 
                       isSameSubject("សីលធម៌", sub) || isSameSubject("ផែនដីវិទ្យា", sub)) {
                return 75.0;
            } else if (isSameSubject("ភាសាអង់គ្លេស", sub) || isSameSubject("ភាសាបារាំង", sub)) {
                return 50.0;
            }
            return 100.0;
        }

        return 100.0;
    }

@GetMapping("/statistics")
    public String statistics(@org.springframework.web.bind.annotation.RequestParam(required = false) String month, 
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
                             @org.springframework.web.bind.annotation.RequestParam(required = false) String subjectName,
                             Model model) {
        Long sessionAccountId = (Long) session.getAttribute("ACCOUNT_ID");
        final Long accountId = (sessionAccountId == null || sessionAccountId == 1L) ? 97L : sessionAccountId;
        if (month == null || month.isEmpty()) {
            month = "តេស្តដើមឆ្នាំ";
        }
        
        String currentYear = getCurrentAcademicYear();
        // Determine track for selected classroom
        String selectedClassTrack = "ទូទៅ";
        if (className != null && !className.isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(className)) {
            List<Classroom> found = classroomRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, currentYear);
            if (found != null && !found.isEmpty()) {
                selectedClassTrack = found.get(0).getTrack();
            } else {
                Classroom dummy = new Classroom();
                dummy.setClassName(className);
                selectedClassTrack = dummy.getTrack();
            }
        }
        model.addAttribute("selectedClassTrack", selectedClassTrack);

        // Get enrolled students for this class or all classes
        List<com.nubb.classroom.model.Student> enrolledStudents;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            enrolledStudents = studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, currentYear);
        } else {
            enrolledStudents = studentRepository.findByAccountIdAndAcademicYear(accountId, currentYear);
        }
        int enrolledTotal = enrolledStudents.size();
        int enrolledFemale = (int) enrolledStudents.stream().filter(s -> "ស្រី".equals(s.getGender())).count();

        List<com.nubb.classroom.model.Score> allScores;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            allScores = scoreRepository.findByMonthAndClassNameAndAccountIdAndAcademicYear(month, className, accountId, currentYear);
            java.util.Set<Long> scoreIds = new java.util.HashSet<>();
            for (com.nubb.classroom.model.Score sc : allScores) {
                if (sc.getId() != null) scoreIds.add(sc.getId());
            }
            for (com.nubb.classroom.model.Student st : enrolledStudents) {
                List<com.nubb.classroom.model.Score> stScores = scoreRepository.findByStudentIdAndMonthAndAccountIdAndAcademicYear(st.getId(), month, accountId, currentYear);
                for (com.nubb.classroom.model.Score sc : stScores) {
                    if (sc.getId() != null && !scoreIds.contains(sc.getId())) {
                        allScores.add(sc);
                        scoreIds.add(sc.getId());
                    }
                }
            }
        } else {
            allScores = scoreRepository.findByMonthAndAccountIdAndAcademicYear(month, accountId, currentYear);
            className = "ថ្នាក់រៀនទាំងអស់";
        }

        // Student fast lookup map
        java.util.Map<Long, com.nubb.classroom.model.Student> studentMap = new java.util.HashMap<>();
        for (com.nubb.classroom.model.Student st : enrolledStudents) {
            if (st.getId() != null) {
                studentMap.put(st.getId(), st);
            }
        }
        for (com.nubb.classroom.model.Score sc : allScores) {
            if (sc.getStudentId() != null && !studentMap.containsKey(sc.getStudentId())) {
                studentRepository.findById(sc.getStudentId()).ifPresent(s -> {
                    if (accountId == null || accountId.equals(s.getAccountId())) {
                        studentMap.put(s.getId(), s);
                    }
                });
            }
        }
        allScores.removeIf(sc -> sc.getStudentId() == null || !studentMap.containsKey(sc.getStudentId()));

        // Helper to get max total score for subject
        java.util.function.BiFunction<String, String, Double> getMaxTotal = (cls, subj) -> {
            // 1. Explicit custom rule for this specific classroom
            if (cls != null && !cls.isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(cls)) {
                List<com.nubb.classroom.model.ScoreRule> rules = scoreRuleRepository.findRules(cls, subj, accountId);
                if (rules != null && !rules.isEmpty()) {
                    com.nubb.classroom.model.ScoreRule r = rules.get(0);
                    double max = r.getMaxMonthlyScore() + r.getMaxAttendanceScore() + r.getMaxAssignmentScore();
                    if (max > 0) return max;
                }
                // 2. Track standard default rule (125, 75, 50) MUST take priority over global 100 rule!
                double trackDefault = computeDefaultMaxScoreForClass(cls, subj, accountId, currentYear);
                if (trackDefault != 100.0) {
                    return trackDefault;
                }
            }
            // 3. Fallback to global rule for "ថ្នាក់រៀនទាំងអស់" if available
            List<com.nubb.classroom.model.ScoreRule> globalRules = scoreRuleRepository.findRules("ថ្នាក់រៀនទាំងអស់", subj, accountId);
            if (globalRules != null && !globalRules.isEmpty()) {
                com.nubb.classroom.model.ScoreRule r = globalRules.get(0);
                double max = r.getMaxMonthlyScore() + r.getMaxAttendanceScore() + r.getMaxAssignmentScore();
                if (max > 0) return max;
            }
            // 4. Default for class or 100.0
            return computeDefaultMaxScoreForClass(cls, subj, accountId, currentYear);
        };

        // Group scores by subject for the summary table
        java.util.Map<String, SubjectStat> subjectStatMap = new java.util.LinkedHashMap<>();
        for (com.nubb.classroom.model.Score score : allScores) {
            String subj = score.getSubjectName();
            if (subj == null || subj.isEmpty()) subj = "មិនស្គាល់";
            
            SubjectStat stat = subjectStatMap.getOrDefault(subj, new SubjectStat());
            stat.subjectName = subj;
            stat.total++;
            
            String studentCls = score.getClassName();
            com.nubb.classroom.model.Student st = studentMap.get(score.getStudentId());
            if (studentCls == null || studentCls.isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(studentCls)) {
                if (st != null && st.getClassName() != null) {
                    studentCls = st.getClassName();
                }
            }

            double maxTotal = getMaxTotal.apply(studentCls, subj);
            double percentage = (score.getTotal() / maxTotal) * 100.0;
            
            boolean isFemale = false;
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
                if (isSameSubject(subjectName, s.getSubjectName())) {
                    filteredScores.add(s);
                }
            }

            for (com.nubb.classroom.model.Score score : filteredScores) {
                String scoreCls = score.getClassName();
                if (scoreCls == null || scoreCls.isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(scoreCls)) {
                    com.nubb.classroom.model.Student st = studentMap.get(score.getStudentId());
                    if (st != null && st.getClassName() != null) {
                        scoreCls = st.getClassName();
                    }
                }
                if (scoreCls == null || scoreCls.isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(scoreCls)) {
                    scoreCls = className;
                }
                double maxTotal = getMaxTotal.apply(scoreCls, subjectName);
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
        
        double selectedMaxScore = 100.0;
        if (subjectName != null && !subjectName.equals("គ្រប់មុខវិជ្ជា")) {
            selectedMaxScore = getMaxTotal.apply(className, subjectName);
        }
        model.addAttribute("selectedMaxScore", selectedMaxScore);
        model.addAttribute("selectedMaxScoreFormatted", toKhmerNumber((int) selectedMaxScore));

        String rangeA = "៩០ - ១០០";
        String rangeB = "៨០ - ៨៩";
        String rangeC = "៧០ - ៧៩";
        String rangeD = "៦០ - ៦៩";
        String rangeE = "៥០ - ៥៩";
        String rangeF = "ក្រោម ៥០";
        
        if (subjectName != null && !subjectName.equals("គ្រប់មុខវិជ្ជា") && className != null) {
            double max = selectedMaxScore;
            if (max > 0 && max != 100.0) {
                rangeA = toKhmerNumber((int) Math.ceil(max * 0.90)) + " - " + toKhmerNumber((int) max);
                rangeB = toKhmerNumber((int) Math.ceil(max * 0.80)) + " - " + toKhmerNumber((int) (Math.ceil(max * 0.90) - 1));
                rangeC = toKhmerNumber((int) Math.ceil(max * 0.70)) + " - " + toKhmerNumber((int) (Math.ceil(max * 0.80) - 1));
                rangeD = toKhmerNumber((int) Math.ceil(max * 0.60)) + " - " + toKhmerNumber((int) (Math.ceil(max * 0.70) - 1));
                rangeE = toKhmerNumber((int) Math.ceil(max * 0.50)) + " - " + toKhmerNumber((int) (Math.ceil(max * 0.60) - 1));
                rangeF = "ក្រោម " + toKhmerNumber((int) Math.ceil(max * 0.50));
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
        
        // Sort by standard academic subject order
        List<String> standardOrder;
        if ("វិទ្យាសាស្ត្រ".equals(selectedClassTrack)) {
            standardOrder = java.util.Arrays.asList(
                "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", "ជីវៈវិទ្យា",
                "ភាសាខ្មែរ", "ភាសាអង់គ្លេស", "ភាសាបារាំង", "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា",
                "ពលរដ្ឋវិទ្យា", "សីលធម៌", "សីលធម៌-ពលរដ្ឋវិជ្ជា", "ផែនដីវិទ្យា", "ផែនដី", "ផែនដី និងបរិស្ថានវិទ្យា",
                "ព័ត៌មានវិទ្យា/កុំព្យូទ័រ", "កុំព្យូទ័រ", "ព័ត៌មានវិទ្យា (ICT)", "កីឡា"
            );
        } else if ("វិទ្យាសាស្ត្រសង្គម".equals(selectedClassTrack)) {
            standardOrder = java.util.Arrays.asList(
                "ភាសាខ្មែរ", "អក្សរសិល្ប៍", "គណិតវិទ្យា", "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា",
                "ពលរដ្ឋវិទ្យា", "សីលធម៌", "សីលធម៌-ពលរដ្ឋវិជ្ជា", "ផែនដីវិទ្យា", "ផែនដី", "ផែនដី និងបរិស្ថានវិទ្យា",
                "ភាសាអង់គ្លេស", "ភាសាបារាំង", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", "ជីវៈវិទ្យា",
                "ព័ត៌មានវិទ្យា/កុំព្យូទ័រ", "កុំព្យូទ័រ", "ព័ត៌មានវិទ្យា (ICT)", "កីឡា"
            );
        } else {
            standardOrder = java.util.Arrays.asList(
                "ភាសាខ្មែរ", "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", "ជីវៈវិទ្យា",
                "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា", "ពលរដ្ឋវិទ្យា", "សីលធម៌", "សីលធម៌-ពលរដ្ឋវិជ្ជា",
                "ផែនដីវិទ្យា", "ផែនដី", "ផែនដី និងបរិស្ថានវិទ្យា",
                "ភាសាអង់គ្លេស", "ភាសាបារាំង", "ព័ត៌មានវិទ្យា/កុំព្យូទ័រ", "កុំព្យូទ័រ", "ព័ត៌មានវិទ្យា (ICT)",
                "កីឡា", "អក្សរសិល្ប៍"
            );
        }
        List<SubjectStat> subjectStatList = new java.util.ArrayList<>(subjectStatMap.values());
        subjectStatList.sort((s1, s2) -> {
            int idx1 = standardOrder.indexOf(s1.getSubjectName());
            int idx2 = standardOrder.indexOf(s2.getSubjectName());
            if (idx1 != -1 && idx2 != -1) return Integer.compare(idx1, idx2);
            if (idx1 != -1) return -1;
            if (idx2 != -1) return 1;
            return s1.getSubjectName().compareTo(s2.getSubjectName());
        });

        SubjectStat totalStat = new SubjectStat();
        totalStat.subjectName = "សរុបរួម";
        for (SubjectStat s : subjectStatList) {
            totalStat.total += s.total;
            totalStat.totalFemale += s.totalFemale;
            totalStat.pass += s.pass;
            totalStat.passFemale += s.passFemale;
            totalStat.fail += s.fail;
            totalStat.failFemale += s.failFemale;
            totalStat.gradeA += s.gradeA;
            totalStat.gradeAFemale += s.gradeAFemale;
            totalStat.gradeB += s.gradeB;
            totalStat.gradeBFemale += s.gradeBFemale;
            totalStat.gradeC += s.gradeC;
            totalStat.gradeCFemale += s.gradeCFemale;
            totalStat.gradeD += s.gradeD;
            totalStat.gradeDFemale += s.gradeDFemale;
            totalStat.gradeE += s.gradeE;
            totalStat.gradeEFemale += s.gradeEFemale;
            totalStat.gradeF += s.gradeF;
            totalStat.gradeFFemale += s.gradeFFemale;
        }

        model.addAttribute("subjectStats", subjectStatList);
        model.addAttribute("totalSubjectStat", totalStat);
        
        model.addAttribute("myClassrooms", classroomRepository.findByAccountIdAndAcademicYear(accountId, currentYear));
        model.addAttribute("subjectsList", getSubjectList(accountId));
        
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
            @org.springframework.web.bind.annotation.RequestParam(value = "redirectTo", required = false) String redirectTo,
            jakarta.servlet.http.HttpSession session,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        String cleanCode = studentCode != null ? studentCode.trim() : "";
        java.util.Optional<Student> studentOpt = studentRepository.findAll().stream().filter(s -> s.getStudentCode() != null && s.getStudentCode().trim().equalsIgnoreCase(cleanCode)).max(java.util.Comparator.comparing(Student::getId, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())));
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            session.setAttribute("loggedInStudent", student);
            session.setAttribute("ACTIVE_USER_MODE", "STUDENT");

            String studentToken = tokenSecurityService.generateStudentToken(student);
            boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
            ResponseCookie cookie = ResponseCookie.from("REMEMBER_STUDENT_ID", studentToken)
                    .httpOnly(true)
                    .secure(isHttps)
                    .path("/")
                    .maxAge(30L * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            if ("/test/leave".equals(redirectTo) || "/lessons".equals(redirectTo) || "/certificate".equals(redirectTo)) {
                return "redirect:" + redirectTo;
            }
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
        if (student.getId() != null) { student = studentRepository.findById(student.getId()).orElse(student); session.setAttribute("loggedInStudent", student); }
        session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
        model.addAttribute("student", student);
        return "take-exam";
    }

    @GetMapping("/test/leave")
    public String studentLeavePage(jakarta.servlet.http.HttpSession session, Model model) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/test";
        }
        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }
        session.setAttribute("ACTIVE_USER_MODE", "STUDENT");

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();
        Account teacherAcc = accountRepository.findById(stuAccId).orElse(null);

        List<com.nubb.classroom.model.LeaveRequest> myRequests = new ArrayList<>();
        try {
            myRequests = leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        } catch (Exception ignored) {}

        long totalRequests = myRequests.size();
        long approvedCount = myRequests.stream().filter(com.nubb.classroom.model.LeaveRequest::isApproved).count();
        long pendingCount = myRequests.stream().filter(com.nubb.classroom.model.LeaveRequest::isPending).count();
        long rejectedCount = myRequests.stream().filter(com.nubb.classroom.model.LeaveRequest::isRejected).count();
        long totalLeaveDays = myRequests.stream().filter(r -> !r.isRejected()).mapToLong(com.nubb.classroom.model.LeaveRequest::getTotalDays).sum();

        model.addAttribute("student", student);
        model.addAttribute("studentTeacher", teacherAcc);
        model.addAttribute("leaveRequests", myRequests);
        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalLeaveDays", totalLeaveDays);
        model.addAttribute("today", LocalDate.now().toString());
        return "student-leave";
    }

    @PostMapping("/test/leave/submit")
    public String submitStudentLeave(
            @RequestParam("startDate") String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "leaveType", defaultValue = "ឈឺ") String leaveType,
            @RequestParam(value = "leaveSession", defaultValue = "ពេញមួយថ្ងៃ") String leaveSession,
            @RequestParam("reason") String reason,
            @RequestParam(value = "parentPhone", required = false) String parentPhone,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {

        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/test";
        }
        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }
        session.setAttribute("ACTIVE_USER_MODE", "STUDENT");

        LocalDate start;
        LocalDate end;
        try {
            start = (startDateStr != null && !startDateStr.trim().isEmpty())
                    ? LocalDate.parse(startDateStr.trim()) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now();
        }
        try {
            end = (endDateStr != null && !endDateStr.trim().isEmpty())
                    ? LocalDate.parse(endDateStr.trim()) : start;
        } catch (Exception e) {
            end = start;
        }
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isAfter(start.plusDays(30))) {
            end = start.plusDays(30);
        }

        String cleanReason = reason != null ? reason.trim() : "";
        if (cleanReason.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "សូមបញ្ចូលមូលហេតុនៃការសុំច្បាប់ឈប់សម្រាក!");
            return "redirect:/test/leave";
        }

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();

        com.nubb.classroom.model.LeaveRequest lr = new com.nubb.classroom.model.LeaveRequest();
        lr.setStudent(student);
        lr.setAccountId(stuAccId);
        lr.setClassName(student.getClassName());
        lr.setAcademicYear(student.getAcademicYear() != null && !student.getAcademicYear().trim().isEmpty()
                ? student.getAcademicYear().trim() : "២០២៦-២០២៧");
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setLeaveType(leaveType != null && !leaveType.trim().isEmpty() ? leaveType.trim() : "ឈឺ");
        lr.setLeaveSession(leaveSession != null && !leaveSession.trim().isEmpty() ? leaveSession.trim() : "ពេញមួយថ្ងៃ");
        lr.setReason(cleanReason);
        lr.setParentPhone(parentPhone != null ? parentPhone.trim() : "");
        lr.setStatus("PENDING");
        lr.setCreatedAt(java.time.LocalDateTime.now());

        leaveRequestRepository.save(lr);
        syncLeaveRequestToAttendance(lr, true);

        redirectAttributes.addFlashAttribute("successMessage", "✅ បានបញ្ជូនពាក្យសុំច្បាប់ទៅលោកគ្រូ/អ្នកគ្រូដោយជោគជ័យ!");
        return "redirect:/test/leave";
    }

    @PostMapping("/test/leave/cancel/{id}")
    public String cancelStudentLeave(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes) {

        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/test";
        }
        Optional<com.nubb.classroom.model.LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isPresent() && lrOpt.get().getStudent() != null
                && lrOpt.get().getStudent().getId().equals(student.getId())) {
            com.nubb.classroom.model.LeaveRequest lr = lrOpt.get();
            if (lr.isPending()) {
                leaveRequestRepository.delete(lr);
                syncLeaveRequestToAttendance(lr, false);
                redirectAttributes.addFlashAttribute("successMessage", "🗑️ បានបោះបង់សំណើសុំច្បាប់ដោយជោគជ័យ!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "មិនអាចលុបសំណើដែលលោកគ្រូ/អ្នកគ្រូបានពិនិត្យរួចហើយនោះទេ!");
            }
        }
        return "redirect:/test/leave";
    }

    @GetMapping("/test/logout")
    public String studentLogout(jakarta.servlet.http.HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("REMEMBER_STUDENT_ID", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        session.invalidate();
        return "redirect:/test";
    }

    public List<String> getSubjectList(Long accountId) {
        Set<String> subjects = new java.util.LinkedHashSet<>();
        subjects.add("គណិតវិទ្យា");
        subjects.add("ភាសាខ្មែរ");
        subjects.add("រូបវិទ្យា");
        subjects.add("គីមីវិទ្យា");
        subjects.add("ជីវវិទ្យា");
        subjects.add("ប្រវត្តិវិទ្យា");
        subjects.add("ភូមិវិទ្យា");
        subjects.add("ពលរដ្ឋវិទ្យា");
        subjects.add("សីលធម៌");
        subjects.add("ផែនដីវិទ្យា");
        subjects.add("ភាសាអង់គ្លេស");
        subjects.add("ភាសាបារាំង");
        subjects.add("ព័ត៌មានវិទ្យា/កុំព្យូទ័រ");
        subjects.add("កីឡា");
        subjects.add("អក្សរសិល្ប៍");
        
        try {
            if (accountId != null) {
                List<String> existing = scoreRepository.findDistinctSubjectNamesByAccountId(accountId);
                if (existing != null) {
                    for (String s : existing) {
                        if (s != null && !s.trim().isEmpty()) {
                            subjects.add(s.trim());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        
        return new ArrayList<>(subjects);
    }

    public static class ReportRow {
        private Student student;
        private double totalScore;
        private Double monthlyScore;
        private Double attendanceScore;
        private Double assignmentScore;
        private Double averageScore;
        private int subjectCount;
        private String subjectBreakdown;
        private String grade;
        private int rank;
        private long presentCount;
        private long absentCount;
        private long absentPermissionCount;
        private long absentUnexcusedCount;

        public ReportRow(Student student, double totalScore, String grade, long presentCount, long absentCount) {
            this.student = student;
            this.totalScore = totalScore;
            this.grade = grade;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }

        public Student getStudent() { return student; }
        public double getTotalScore() { return totalScore; }
        public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
        public Double getMonthlyScore() { return monthlyScore; }
        public void setMonthlyScore(Double monthlyScore) { this.monthlyScore = monthlyScore; }
        public Double getAttendanceScore() { return attendanceScore; }
        public void setAttendanceScore(Double attendanceScore) { this.attendanceScore = attendanceScore; }
        public Double getAssignmentScore() { return assignmentScore; }
        public void setAssignmentScore(Double assignmentScore) { this.assignmentScore = assignmentScore; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
        public int getSubjectCount() { return subjectCount; }
        public void setSubjectCount(int subjectCount) { this.subjectCount = subjectCount; }
        public String getSubjectBreakdown() { return subjectBreakdown; }
        public void setSubjectBreakdown(String subjectBreakdown) { this.subjectBreakdown = subjectBreakdown; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public long getPresentCount() { return presentCount; }
        public long getAbsentCount() { return absentCount; }
        public long getAbsentPermissionCount() { return absentPermissionCount; }
        public void setAbsentPermissionCount(long absentPermissionCount) { this.absentPermissionCount = absentPermissionCount; }
        public long getAbsentUnexcusedCount() { return absentUnexcusedCount; }
        public void setAbsentUnexcusedCount(long absentUnexcusedCount) { this.absentUnexcusedCount = absentUnexcusedCount; }
    }

    @GetMapping("/report")
    public String report(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String className,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String subjectName,
            Model model) {
        
        if (month == null || month.isEmpty()) {
            month = "តេស្តដើមឆ្នាំ";
        }

        if (subjectName == null || subjectName.trim().isEmpty()) {
            subjectName = "គ្រប់មុខវិជ្ជា";
        } else {
            subjectName = subjectName.trim();
        }

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        String currentYear = getCurrentAcademicYear();
        List<Student> students;
        if (className != null && !className.isEmpty() && !className.equals("ថ្នាក់រៀនទាំងអស់")) {
            students = studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, currentYear);
        } else {
            students = studentRepository.findByAccountIdAndAcademicYear(accountId, currentYear);
        }

        int[] years = parseAcademicYearNumbers(currentYear);
        int monthNum = 1;
        int year = years[1];
        if (month.contains("វិច្ឆិកា")) { monthNum = 11; year = years[0]; }
        else if (month.contains("ធ្នូ")) { monthNum = 12; year = years[0]; }
        else if (month.contains("មករា")) { monthNum = 1; year = years[1]; }
        else if (month.contains("កុម្ភៈ")) { monthNum = 2; year = years[1]; }
        else if (month.contains("មីនា")) { monthNum = 3; year = years[1]; }
        else if (month.contains("មេសា")) { monthNum = 4; year = years[1]; }
        else if (month.contains("ឧសភា")) { monthNum = 5; year = years[1]; }
        else if (month.contains("មិថុនា")) { monthNum = 6; year = years[1]; }
        else if (month.contains("កក្កដា")) { monthNum = 7; year = years[1]; }
        else if (month.contains("សីហា")) { monthNum = 8; year = years[1]; }
        else if (month.contains("កញ្ញា")) { monthNum = 9; year = years[1]; }
        else if (month.contains("តុលា")) { monthNum = 10; year = years[0]; }

        int extY = extractYear(month);
        if (extY > 2000 && extY < 2100) {
            year = extY;
        }

        java.time.LocalDate start = java.time.LocalDate.of(year, monthNum, 1);
        java.time.LocalDate end = start.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        if (month.contains("តេស្តដើមឆ្នាំ") || month.contains("ប្រចាំឆ្នាំ")) {
            start = java.time.LocalDate.of(years[0], 10, 1);
            end = java.time.LocalDate.of(years[1], 9, 30);
        } else if (month.contains("ឆមាសទី១")) {
            start = java.time.LocalDate.of(years[0], 11, 1);
            end = java.time.LocalDate.of(years[1], 2, 28);
        } else if (month.contains("ឆមាសទី២")) {
            start = java.time.LocalDate.of(years[1], 3, 1);
            end = java.time.LocalDate.of(years[1], 8, 31);
        }

        // Helper to get max score for a subject
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

        boolean isAllSubjects = "គ្រប់មុខវិជ្ជា".equals(subjectName);
        java.util.List<ReportRow> reportRows = new java.util.ArrayList<>();

        for (Student student : students) {
            List<com.nubb.classroom.model.Attendance> attList = attendanceRepository.findByStudentIdAndDateBetween(student.getId(), start, end);
            long presentCount = attList.stream().filter(a -> a.isPresent()).count();
            long absentCount = attList.stream().filter(a -> !a.isPresent()).count();
            long absentPermission = attList.stream().filter(a -> !a.isPresent() && a.isPermission()).count();
            long absentUnexcused = attList.stream().filter(a -> !a.isPresent() && a.isUnexcused()).count();

            if (isAllSubjects) {
                List<com.nubb.classroom.model.Score> studentScores = scoreRepository.findByStudentIdAndMonthAndAccountIdAndAcademicYear(
                        student.getId(), month, accountId, currentYear);

                double totalScore = 0;
                double sumPercentage = 0;
                java.util.List<String> breakdowns = new java.util.ArrayList<>();

                for (com.nubb.classroom.model.Score s : studentScores) {
                    totalScore += s.getTotal();
                    double maxT = getMaxTotal.apply(student.getClassName(), s.getSubjectName());
                    sumPercentage += (s.getTotal() / maxT) * 100.0;

                    String scoreFormatted = (s.getTotal() == Math.floor(s.getTotal())) ? String.valueOf((long) s.getTotal()) : String.format("%.1f", s.getTotal());
                    breakdowns.add(s.getSubjectName() + "៖ " + scoreFormatted);
                }

                String grade = "-";
                Double avgScore = null;
                int count = studentScores.size();
                if (count > 0) {
                    avgScore = Math.round((totalScore / count) * 100.0) / 100.0;
                    double avgPct = sumPercentage / count;
                    if (avgPct >= 90.0) grade = "A (ល្អណាស់)";
                    else if (avgPct >= 80.0) grade = "B (ល្អ)";
                    else if (avgPct >= 70.0) grade = "C (បង្គួរ)";
                    else if (avgPct >= 60.0) grade = "D (មធ្យម)";
                    else if (avgPct >= 50.0) grade = "E (ខ្សោយ)";
                    else grade = "F (ធ្លាក់)";
                }

                ReportRow row = new ReportRow(student, totalScore, grade, presentCount, absentCount);
                row.setAverageScore(avgScore);
                row.setSubjectCount(count);
                row.setSubjectBreakdown(String.join(" | ", breakdowns));
                row.setAbsentPermissionCount(absentPermission);
                row.setAbsentUnexcusedCount(absentUnexcused);
                reportRows.add(row);
            } else {
                List<com.nubb.classroom.model.Score> studentScores = scoreRepository.findByStudentIdAndMonthAndSubjectNameAndAccountIdAndAcademicYear(
                        student.getId(), month, subjectName, accountId, currentYear);

                double totalScore = 0;
                Double monthlyScore = null;
                Double attendanceScore = null;
                Double assignmentScore = null;
                String grade = "-";

                if (!studentScores.isEmpty()) {
                    com.nubb.classroom.model.Score s = studentScores.get(0);
                    monthlyScore = s.getMonthlyScore();
                    attendanceScore = s.getAttendanceScore();
                    assignmentScore = s.getAssignmentScore();
                    totalScore = s.getTotal();

                    double maxT = getMaxTotal.apply(student.getClassName(), subjectName);
                    double pct = (totalScore / maxT) * 100.0;
                    if (pct >= 90.0) grade = "A (ល្អណាស់)";
                    else if (pct >= 80.0) grade = "B (ល្អ)";
                    else if (pct >= 70.0) grade = "C (បង្គួរ)";
                    else if (pct >= 60.0) grade = "D (មធ្យម)";
                    else if (pct >= 50.0) grade = "E (ខ្សោយ)";
                    else grade = "F (ធ្លាក់)";
                }

                ReportRow row = new ReportRow(student, totalScore, grade, presentCount, absentCount);
                row.setMonthlyScore(monthlyScore);
                row.setAttendanceScore(attendanceScore);
                row.setAssignmentScore(assignmentScore);
                row.setSubjectCount(studentScores.isEmpty() ? 0 : 1);
                row.setAbsentPermissionCount(absentPermission);
                row.setAbsentUnexcusedCount(absentUnexcused);
                reportRows.add(row);
            }
        }

        // Sort descending
        reportRows.sort((r1, r2) -> {
            if (isAllSubjects) {
                double avg1 = r1.getAverageScore() != null ? r1.getAverageScore() : 0.0;
                double avg2 = r2.getAverageScore() != null ? r2.getAverageScore() : 0.0;
                int cmp = Double.compare(avg2, avg1);
                if (cmp != 0) return cmp;
            }
            return Double.compare(r2.getTotalScore(), r1.getTotalScore());
        });

        // Calculate ranks
        int currentRank = 1;
        for (int i = 0; i < reportRows.size(); i++) {
            ReportRow row = reportRows.get(i);
            if (row.getSubjectCount() == 0 || "-".equals(row.getGrade())) {
                row.setRank(0);
                continue;
            }
            if (i > 0) {
                ReportRow prev = reportRows.get(i - 1);
                double scorePrev = isAllSubjects ? (prev.getAverageScore() != null ? prev.getAverageScore() : prev.getTotalScore()) : prev.getTotalScore();
                double scoreCurr = isAllSubjects ? (row.getAverageScore() != null ? row.getAverageScore() : row.getTotalScore()) : row.getTotalScore();
                if (scoreCurr < scorePrev) {
                    currentRank = i + 1;
                }
            }
            row.setRank(currentRank);
        }

        model.addAttribute("reportRows", reportRows);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedSubject", subjectName);
        model.addAttribute("isAllSubjects", isAllSubjects);
        model.addAttribute("subjectsList", getSubjectList(accountId));

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

        public double getPassPercent() {
            return total > 0 ? Math.round((pass * 100.0) / total * 10.0) / 10.0 : 0.0;
        }

        public double getFailPercent() {
            return total > 0 ? Math.round((fail * 100.0) / total * 10.0) / 10.0 : 0.0;
        }

        public String getPassPercentFormatted() {
            return String.format(java.util.Locale.US, "%.1f%%", total > 0 ? (pass * 100.0) / total : 0.0);
        }

        public String getFailPercentFormatted() {
            return String.format(java.util.Locale.US, "%.1f%%", total > 0 ? (fail * 100.0) / total : 0.0);
        }
    }
}