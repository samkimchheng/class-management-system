package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeroomController {

    @Autowired
    private HttpSession session;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private HomeroomSubjectRepository homeroomSubjectRepository;

    @Autowired
    private HomeroomScoreRepository homeroomScoreRepository;

    @Autowired
    private HomeroomStudentExtraRepository homeroomStudentExtraRepository;

    @Autowired
    private HomeroomClassAdminRepository homeroomClassAdminRepository;

    private Long getAccountId() {
        return (Long) session.getAttribute("ACCOUNT_ID");
    }

    private String resolveAcademicYear(Account acc, String requestedYear) {
        if (requestedYear != null && !requestedYear.trim().isEmpty()) {
            return requestedYear.trim();
        }
        if (acc != null && acc.getCurrentAcademicYear() != null && !acc.getCurrentAcademicYear().trim().isEmpty()) {
            return acc.getCurrentAcademicYear().trim();
        }
        return "២០២៦-២០២៧";
    }

    @GetMapping("/homeroom")
    public String homeroomPage(
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "academicYear", required = false) String academicYear,
            Model model) {
        Long accountId = getAccountId();
        if (accountId == null) {
            return "redirect:/login";
        }
        Account acc = accountRepository.findById(accountId).orElse(null);
        if (acc == null) {
            return "redirect:/login";
        }

        String activeYear = resolveAcademicYear(acc, academicYear);
        List<Classroom> classrooms = classroomRepository.findByAccountIdAndAcademicYear(accountId, activeYear);
        if (classrooms.isEmpty()) {
            classrooms = classroomRepository.findByAccountId(accountId);
        }

        String selectedClass = className;
        if ((selectedClass == null || selectedClass.trim().isEmpty()) && !classrooms.isEmpty()) {
            selectedClass = classrooms.get(0).getClassName();
        }

        model.addAttribute("account", acc);
        model.addAttribute("fullName", acc.getFullName() != null ? acc.getFullName() : acc.getUsername());
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("selectedClass", selectedClass != null ? selectedClass : "");
        model.addAttribute("academicYear", activeYear);
        return "homeroom";
    }

    @GetMapping("/api/homeroom/bootstrap")
    @ResponseBody
    public ResponseEntity<?> bootstrap(
            @RequestParam("className") String className,
            @RequestParam(value = "academicYear", required = false) String academicYear) {
        Long accountId = getAccountId();
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        Account acc = accountRepository.findById(accountId).orElse(null);
        if (acc == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Account not found"));
        }
        String activeYear = resolveAcademicYear(acc, academicYear);

        // 1. Teacher & School Info
        Map<String, Object> teacherInfo = new LinkedHashMap<>();
        teacherInfo.put("fullName", acc.getFullName() != null ? acc.getFullName() : acc.getUsername());
        teacherInfo.put("gender", acc.getGender() != null ? acc.getGender() : "");
        teacherInfo.put("schoolName", acc.getSchoolName() != null ? acc.getSchoolName() : "វិទ្យាល័យ");
        teacherInfo.put("province", acc.getProvince() != null ? acc.getProvince() : "");
        teacherInfo.put("principalName", acc.getPrincipalName() != null ? acc.getPrincipalName() : "");
        teacherInfo.put("signature", acc.getSignature() != null ? acc.getSignature() : "");
        teacherInfo.put("principalSignature", acc.getPrincipalSignature() != null ? acc.getPrincipalSignature() : "");
        teacherInfo.put("schoolStamp", acc.getSchoolStamp() != null ? acc.getSchoolStamp() : "");
        teacherInfo.put("academicYear", activeYear);

        // 2. Existing Students in this class (read-only from existing table)
        List<Student> students = studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, activeYear);
        if (students.isEmpty()) {
            students = studentRepository.findByClassNameAndAccountId(className, accountId);
        }
        students.sort(Comparator.comparing(s -> s.getName() != null ? s.getName() : ""));

        // Extra info per student
        List<HomeroomStudentExtra> extras = homeroomStudentExtraRepository.findByAccountId(accountId);
        Map<Long, HomeroomStudentExtra> extraMap = new HashMap<>();
        for (HomeroomStudentExtra e : extras) {
            extraMap.put(e.getStudentId(), e);
        }

        // Attendance summary per student (read-only from existing attendance table)
        Map<Long, int[]> attCounts = new HashMap<>(); // [permission, unexcused, totalAbsent]
        Map<Long, Map<Integer, int[]>> attByMonth = new HashMap<>();
        List<Map<String, Object>> dailyAbsences = new ArrayList<>();
        if (!students.isEmpty()) {
            List<Attendance> allAtts = attendanceRepository.findByStudentIn(students);
            for (Attendance a : allAtts) {
                if (!a.isPresent() && a.getStudent() != null && a.getStudent().getId() != null) {
                    Long sid = a.getStudent().getId();
                    int[] c = attCounts.computeIfAbsent(sid, k -> new int[3]);
                    if (a.isPermission()) {
                        c[0]++;
                    } else {
                        c[1]++;
                    }
                    c[2]++;
                    if (a.getDate() != null) {
                        int m = a.getDate().getMonthValue();
                        int[] mc = attByMonth.computeIfAbsent(sid, k -> new HashMap<>())
                                .computeIfAbsent(m, k -> new int[3]);
                        if (a.isPermission()) mc[0]++; else mc[1]++;
                        mc[2]++;
                        Map<String, Object> da = new HashMap<>();
                        da.put("studentId", sid);
                        da.put("date", a.getDate().toString());
                        da.put("permission", a.isPermission());
                        dailyAbsences.add(da);
                    }
                }
            }
        }

        List<Map<String, Object>> studentDtos = new ArrayList<>();
        for (Student s : students) {
            HomeroomStudentExtra ex = extraMap.get(s.getId());
            int[] ac = attCounts.getOrDefault(s.getId(), new int[3]);
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", s.getId());
            dto.put("studentCode", s.getStudentCode() != null ? s.getStudentCode() : "");
            dto.put("name", s.getName() != null ? s.getName() : "");
            dto.put("gender", s.getGender() != null ? s.getGender() : "");
            dto.put("className", s.getClassName() != null ? s.getClassName() : className);
            dto.put("village", s.getVillage() != null ? s.getVillage() : "");
            dto.put("commune", s.getCommune() != null ? s.getCommune() : "");
            dto.put("district", s.getDistrict() != null ? s.getDistrict() : "");
            dto.put("province", s.getProvince() != null ? s.getProvince() : "");
            dto.put("fatherName", s.getFatherName() != null ? s.getFatherName() : "");
            dto.put("fatherOccupation", s.getFatherOccupation() != null ? s.getFatherOccupation() : "");
            dto.put("motherName", s.getMotherName() != null ? s.getMotherName() : "");
            dto.put("motherOccupation", s.getMotherOccupation() != null ? s.getMotherOccupation() : "");
            dto.put("status", s.getStatus() != null ? s.getStatus() : "កំពុងសិក្សា");
            dto.put("studying", s.isStudying());

            // Extra homeroom fields
            dto.put("dob", ex != null && ex.getDob() != null ? ex.getDob() : "");
            dto.put("pob", ex != null && ex.getPob() != null ? ex.getPob() : "");
            dto.put("phone", ex != null && ex.getPhone() != null ? ex.getPhone() : "");
            dto.put("photoUrl", ex != null && ex.getPhotoUrl() != null ? ex.getPhotoUrl() : "");
            dto.put("youthRole", ex != null && ex.getYouthRole() != null ? ex.getYouthRole() : "");
            dto.put("cleaningGroup", ex != null && ex.getCleaningGroup() != null ? ex.getCleaningGroup() : "");
            dto.put("seatRow", ex != null ? ex.getSeatRow() : null);
            dto.put("seatCol", ex != null ? ex.getSeatCol() : null);
            dto.put("previousSchool", ex != null && ex.getPreviousSchool() != null ? ex.getPreviousSchool() : "");
            dto.put("scholarshipOrNote", ex != null && ex.getScholarshipOrNote() != null ? ex.getScholarshipOrNote() : "");
            dto.put("teacherComment", ex != null && ex.getTeacherComment() != null ? ex.getTeacherComment() : "");

            // Attendance summary
            dto.put("absentPermission", ac[0]);
            dto.put("absentUnexcused", ac[1]);
            dto.put("absentTotal", ac[2]);
            dto.put("absentByMonth", attByMonth.getOrDefault(s.getId(), Collections.emptyMap()));
            studentDtos.add(dto);
        }

        // 3. Homeroom Subjects
        List<HomeroomSubject> subjects = homeroomSubjectRepository
                .findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(accountId, className, activeYear);

        // 4. Homeroom Scores (all periods for this class & year)
        List<HomeroomScore> scores = homeroomScoreRepository
                .findByAccountIdAndClassNameAndAcademicYear(accountId, className, activeYear);

        // 5. Homeroom Class Admin record
        HomeroomClassAdmin admin = homeroomClassAdminRepository
                .findByAccountIdAndClassNameAndAcademicYear(accountId, className, activeYear)
                .orElse(null);
        Map<String, Object> adminDto = new LinkedHashMap<>();
        adminDto.put("cleaningScheduleJson", admin != null ? admin.getCleaningScheduleJson() : null);
        adminDto.put("seatingLayoutJson", admin != null ? admin.getSeatingLayoutJson() : null);
        adminDto.put("youthCouncilJson", admin != null ? admin.getYouthCouncilJson() : null);
        adminDto.put("inventoryJson", admin != null ? admin.getInventoryJson() : null);
        adminDto.put("textbookLoansJson", admin != null ? admin.getTextbookLoansJson() : null);
        adminDto.put("geipPlanJson", admin != null ? admin.getGeipPlanJson() : null);
        adminDto.put("meetingLogsJson", admin != null ? admin.getMeetingLogsJson() : null);
        adminDto.put("configJson", admin != null ? admin.getConfigJson() : null);
        adminDto.put("monthlyAttendanceJson", admin != null ? admin.getMonthlyAttendanceJson() : null);
        adminDto.put("monitorStudentId", admin != null ? admin.getMonitorStudentId() : null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("teacher", teacherInfo);
        result.put("className", className);
        result.put("academicYear", activeYear);
        result.put("students", studentDtos);
        result.put("subjects", subjects);
        result.put("scores", scores);
        result.put("dailyAbsences", dailyAbsences);
        result.put("admin", adminDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/homeroom/subjects/preset")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> applySubjectPreset(@RequestBody Map<String, Object> payload) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        String className = String.valueOf(payload.getOrDefault("className", "")).trim();
        String academicYear = String.valueOf(payload.getOrDefault("academicYear", "២០២៦-២០២៧")).trim();
        String preset = String.valueOf(payload.getOrDefault("preset", "G10")).trim();

        if (className.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "សូមជ្រើសរើសថ្នាក់រៀនជាមុនសិន"));
        }

        // Only add preset subjects that don't already exist by name, to avoid wiping existing scores
        List<HomeroomSubject> existing = homeroomSubjectRepository
                .findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(accountId, className, academicYear);
        Set<String> existingNames = existing.stream()
                .map(s -> s.getSubjectName().trim().toLowerCase())
                .collect(Collectors.toSet());

        List<Object[]> template = getPresetSubjects(preset);
        int order = existing.size() + 1;
        for (Object[] row : template) {
            String name = (String) row[0];
            Double maxScore = (Double) row[1];
            Double coeff = (Double) row[2];
            if (!existingNames.contains(name.trim().toLowerCase())) {
                HomeroomSubject sub = new HomeroomSubject(accountId, className, academicYear, name, "", maxScore, coeff, order++);
                homeroomSubjectRepository.save(sub);
            }
        }

        List<HomeroomSubject> updated = homeroomSubjectRepository
                .findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(accountId, className, academicYear);
        return ResponseEntity.ok(Map.of("success", true, "subjects", updated));
    }

    private List<Object[]> getPresetSubjects(String preset) {
        return switch (preset) {
            case "G7_9" -> List.of(
                    new Object[]{"ភាសាខ្មែរ", 100.0, 2.0},
                    new Object[]{"គណិតវិទ្យា", 100.0, 2.0},
                    new Object[]{"រូបវិទ្យា", 50.0, 1.0},
                    new Object[]{"គីមីវិទ្យា", 50.0, 1.0},
                    new Object[]{"ជីវវិទ្យា", 50.0, 1.0},
                    new Object[]{"ផែនដី-បរិស្ថាន", 50.0, 1.0},
                    new Object[]{"ប្រវត្តិវិទ្យា", 50.0, 1.0},
                    new Object[]{"ភូមិវិទ្យា", 50.0, 1.0},
                    new Object[]{"សីលធម៌-ពលរដ្ឋ", 50.0, 1.0},
                    new Object[]{"គេហវិទ្យា", 50.0, 1.0},
                    new Object[]{"ភាសាអង់គ្លេស", 50.0, 1.0},
                    new Object[]{"អប់រំកាយ-កីឡា", 50.0, 1.0}
            );
            case "G11_12_SCI" -> List.of(
                    new Object[]{"ភាសាខ្មែរ", 75.0, 1.5},
                    new Object[]{"គណិតវិទ្យា", 125.0, 2.5},
                    new Object[]{"រូបវិទ្យា", 75.0, 1.5},
                    new Object[]{"គីមីវិទ្យា", 75.0, 1.5},
                    new Object[]{"ជីវវិទ្យា", 75.0, 1.5},
                    new Object[]{"ផែនដី-បរិស្ថាន", 50.0, 1.0},
                    new Object[]{"ប្រវត្តិវិទ្យា", 50.0, 1.0},
                    new Object[]{"ភូមិវិទ្យា", 50.0, 1.0},
                    new Object[]{"សីលធម៌-ពលរដ្ឋ", 50.0, 1.0},
                    new Object[]{"ភាសាអង់គ្លេស", 50.0, 1.0},
                    new Object[]{"ព័ត៌មានវិទ្យា (ICT)", 50.0, 1.0},
                    new Object[]{"អប់រំកាយ-កីឡា", 50.0, 1.0}
            );
            case "G11_12_SOC" -> List.of(
                    new Object[]{"ភាសាខ្មែរ", 125.0, 2.5},
                    new Object[]{"គណិតវិទ្យា", 75.0, 1.5},
                    new Object[]{"ប្រវត្តិវិទ្យា", 75.0, 1.5},
                    new Object[]{"ភូមិវិទ្យា", 75.0, 1.5},
                    new Object[]{"សីលធម៌-ពលរដ្ឋ", 75.0, 1.5},
                    new Object[]{"ផែនដី-បរិស្ថាន", 50.0, 1.0},
                    new Object[]{"រូបវិទ្យា", 50.0, 1.0},
                    new Object[]{"គីមីវិទ្យា", 50.0, 1.0},
                    new Object[]{"ជីវវិទ្យា", 50.0, 1.0},
                    new Object[]{"ភាសាអង់គ្លេស", 50.0, 1.0},
                    new Object[]{"ព័ត៌មានវិទ្យា (ICT)", 50.0, 1.0},
                    new Object[]{"អប់រំកាយ-កីឡា", 50.0, 1.0}
            );
            default -> List.of( // G10 standard
                    new Object[]{"ភាសាខ្មែរ", 100.0, 2.0},
                    new Object[]{"គណិតវិទ្យា", 100.0, 2.0},
                    new Object[]{"រូបវិទ្យា", 75.0, 1.5},
                    new Object[]{"គីមីវិទ្យា", 75.0, 1.5},
                    new Object[]{"ជីវវិទ្យា", 75.0, 1.5},
                    new Object[]{"ផែនដី-បរិស្ថាន", 50.0, 1.0},
                    new Object[]{"ប្រវត្តិវិទ្យា", 50.0, 1.0},
                    new Object[]{"ភូមិវិទ្យា", 50.0, 1.0},
                    new Object[]{"សីលធម៌-ពលរដ្ឋ", 50.0, 1.0},
                    new Object[]{"ភាសាអង់គ្លេស", 50.0, 1.0},
                    new Object[]{"ព័ត៌មានវិទ្យា (ICT)", 50.0, 1.0},
                    new Object[]{"អប់រំកាយ-កីឡា", 50.0, 1.0}
            );
        };
    }

    @PostMapping("/api/homeroom/subjects/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveSubject(@RequestBody Map<String, Object> payload) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        String className = String.valueOf(payload.getOrDefault("className", "")).trim();
        String academicYear = String.valueOf(payload.getOrDefault("academicYear", "២០២៦-២០២៧")).trim();
        String subjectName = String.valueOf(payload.getOrDefault("subjectName", "")).trim();
        String teacherName = String.valueOf(payload.getOrDefault("teacherName", "")).trim();
        double maxScore = parseDouble(payload.get("maxScore"), 100.0);
        double coefficient = parseDouble(payload.get("coefficient"), maxScore / 50.0);
        int displayOrder = (int) parseDouble(payload.get("displayOrder"), 0);

        if (className.isEmpty() || subjectName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "សូមបញ្ចូលឈ្មោះមុខវិជ្ជា"));
        }

        HomeroomSubject sub;
        if (payload.get("id") != null && !String.valueOf(payload.get("id")).isEmpty()) {
            Long id = Long.parseLong(String.valueOf(payload.get("id")));
            sub = homeroomSubjectRepository.findById(id).orElse(new HomeroomSubject());
            if (sub.getId() != null && !Objects.equals(sub.getAccountId(), accountId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }
        } else {
            sub = new HomeroomSubject();
            List<HomeroomSubject> existing = homeroomSubjectRepository
                    .findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(accountId, className, academicYear);
            if (displayOrder <= 0) displayOrder = existing.size() + 1;
        }

        sub.setAccountId(accountId);
        sub.setClassName(className);
        sub.setAcademicYear(academicYear);
        sub.setSubjectName(subjectName);
        sub.setTeacherName(teacherName);
        sub.setMaxScore(maxScore);
        sub.setCoefficient(coefficient);
        sub.setDisplayOrder(displayOrder);
        homeroomSubjectRepository.save(sub);

        List<HomeroomSubject> updated = homeroomSubjectRepository
                .findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(accountId, className, academicYear);
        return ResponseEntity.ok(Map.of("success", true, "subjects", updated));
    }

    @DeleteMapping("/api/homeroom/subjects/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        HomeroomSubject sub = homeroomSubjectRepository.findById(id).orElse(null);
        if (sub == null) return ResponseEntity.notFound().build();
        if (!Objects.equals(sub.getAccountId(), accountId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        homeroomScoreRepository.deleteBySubjectId(id);
        homeroomSubjectRepository.delete(sub);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/homeroom/scores/save")
    @ResponseBody
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> saveScores(@RequestBody Map<String, Object> payload) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        String className = String.valueOf(payload.getOrDefault("className", "")).trim();
        String academicYear = String.valueOf(payload.getOrDefault("academicYear", "២០២៦-២០២៧")).trim();
        String period = String.valueOf(payload.getOrDefault("period", "M1")).trim();
        List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entries");

        if (className.isEmpty() || entries == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }

        // Load existing scores for this period into a fast lookup map
        List<HomeroomScore> existingList = homeroomScoreRepository
                .findByAccountIdAndClassNameAndAcademicYearAndPeriod(accountId, className, academicYear, period);
        Map<String, HomeroomScore> existingMap = new HashMap<>();
        for (HomeroomScore hs : existingList) {
            existingMap.put(hs.getStudentId() + "_" + hs.getSubjectId(), hs);
        }

        int savedCount = 0;
        for (Map<String, Object> entry : entries) {
            if (entry.get("studentId") == null || entry.get("subjectId") == null) continue;
            Long studentId = Long.parseLong(String.valueOf(entry.get("studentId")));
            Long subjectId = Long.parseLong(String.valueOf(entry.get("subjectId")));
            Object scoreObj = entry.get("score");
            String key = studentId + "_" + subjectId;
            HomeroomScore existing = existingMap.get(key);

            if (scoreObj == null || String.valueOf(scoreObj).trim().isEmpty()) {
                if (existing != null) {
                    homeroomScoreRepository.delete(existing);
                }
                continue;
            }

            Double val = parseDoubleOrNull(scoreObj);
            if (val == null) continue;
            if (val < 0) val = 0.0;

            if (existing != null) {
                existing.setScore(val);
                homeroomScoreRepository.save(existing);
            } else {
                HomeroomScore hs = new HomeroomScore(accountId, studentId, className, academicYear, period, subjectId, val);
                homeroomScoreRepository.save(hs);
            }
            savedCount++;
        }

        List<HomeroomScore> allScores = homeroomScoreRepository
                .findByAccountIdAndClassNameAndAcademicYear(accountId, className, academicYear);
        return ResponseEntity.ok(Map.of("success", true, "savedCount", savedCount, "scores", allScores));
    }

    @PostMapping("/api/homeroom/student-extra/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveStudentExtra(@RequestBody Map<String, Object> payload) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        if (payload.get("studentId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "studentId required"));
        }
        Long studentId = Long.parseLong(String.valueOf(payload.get("studentId")));
        HomeroomStudentExtra ex = homeroomStudentExtraRepository
                .findByAccountIdAndStudentId(accountId, studentId)
                .orElseGet(() -> {
                    HomeroomStudentExtra n = new HomeroomStudentExtra();
                    n.setAccountId(accountId);
                    n.setStudentId(studentId);
                    return n;
                });

        if (payload.containsKey("className")) ex.setClassName(strOrEmpty(payload.get("className")));
        if (payload.containsKey("dob")) ex.setDob(strOrEmpty(payload.get("dob")));
        if (payload.containsKey("pob")) ex.setPob(strOrEmpty(payload.get("pob")));
        if (payload.containsKey("phone")) ex.setPhone(strOrEmpty(payload.get("phone")));
        if (payload.containsKey("photoUrl")) ex.setPhotoUrl(strOrEmpty(payload.get("photoUrl")));
        if (payload.containsKey("youthRole")) ex.setYouthRole(strOrEmpty(payload.get("youthRole")));
        if (payload.containsKey("cleaningGroup")) ex.setCleaningGroup(strOrEmpty(payload.get("cleaningGroup")));
        if (payload.containsKey("previousSchool")) ex.setPreviousSchool(strOrEmpty(payload.get("previousSchool")));
        if (payload.containsKey("scholarshipOrNote")) ex.setScholarshipOrNote(strOrEmpty(payload.get("scholarshipOrNote")));
        if (payload.containsKey("teacherComment")) ex.setTeacherComment(strOrEmpty(payload.get("teacherComment")));
        if (payload.containsKey("seatRow")) ex.setSeatRow(parseIntOrNull(payload.get("seatRow")));
        if (payload.containsKey("seatCol")) ex.setSeatCol(parseIntOrNull(payload.get("seatCol")));

        homeroomStudentExtraRepository.save(ex);
        return ResponseEntity.ok(Map.of("success", true, "extra", ex));
    }

    @PostMapping("/api/homeroom/student-extra/upload-photo")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> uploadStudentPhoto(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "className", required = false) String className,
            @RequestParam("file") MultipartFile file) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "សូមជ្រើសរើសរូបថត"));
        }
        try {
            File dir = new File("./uploads/images");
            if (!dir.exists()) dir.mkdirs();
            String orig = file.getOriginalFilename() != null ? file.getOriginalFilename() : "student.jpg";
            String cleanName = orig.replaceAll("[^a-zA-Z0-9.-]", "_");
            String filename = "stu_" + studentId + "_" + System.currentTimeMillis() + "_" + cleanName;
            Path path = dir.toPath().resolve(filename);
            Files.write(path, file.getBytes());
            String photoUrl = "/uploads/images/" + filename;

            HomeroomStudentExtra ex = homeroomStudentExtraRepository
                    .findByAccountIdAndStudentId(accountId, studentId)
                    .orElseGet(() -> {
                        HomeroomStudentExtra n = new HomeroomStudentExtra();
                        n.setAccountId(accountId);
                        n.setStudentId(studentId);
                        n.setClassName(className);
                        return n;
                    });
            ex.setPhotoUrl(photoUrl);
            homeroomStudentExtraRepository.save(ex);
            return ResponseEntity.ok(Map.of("success", true, "photoUrl", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "មិនអាចរក្សាទុករូបថតបានទេ: " + e.getMessage()));
        }
    }

    @PostMapping("/api/homeroom/admin/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveClassAdmin(@RequestBody Map<String, Object> payload) {
        Long accountId = getAccountId();
        if (accountId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        String className = String.valueOf(payload.getOrDefault("className", "")).trim();
        String academicYear = String.valueOf(payload.getOrDefault("academicYear", "២០២៦-២០២៧")).trim();
        if (className.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "className required"));
        }

        HomeroomClassAdmin admin = homeroomClassAdminRepository
                .findByAccountIdAndClassNameAndAcademicYear(accountId, className, academicYear)
                .orElseGet(() -> {
                    HomeroomClassAdmin n = new HomeroomClassAdmin();
                    n.setAccountId(accountId);
                    n.setClassName(className);
                    n.setAcademicYear(academicYear);
                    return n;
                });

        if (payload.containsKey("cleaningScheduleJson")) admin.setCleaningScheduleJson(strOrNull(payload.get("cleaningScheduleJson")));
        if (payload.containsKey("seatingLayoutJson")) admin.setSeatingLayoutJson(strOrNull(payload.get("seatingLayoutJson")));
        if (payload.containsKey("youthCouncilJson")) admin.setYouthCouncilJson(strOrNull(payload.get("youthCouncilJson")));
        if (payload.containsKey("inventoryJson")) admin.setInventoryJson(strOrNull(payload.get("inventoryJson")));
        if (payload.containsKey("textbookLoansJson")) admin.setTextbookLoansJson(strOrNull(payload.get("textbookLoansJson")));
        if (payload.containsKey("geipPlanJson")) admin.setGeipPlanJson(strOrNull(payload.get("geipPlanJson")));
        if (payload.containsKey("meetingLogsJson")) admin.setMeetingLogsJson(strOrNull(payload.get("meetingLogsJson")));
        if (payload.containsKey("configJson")) admin.setConfigJson(strOrNull(payload.get("configJson")));
        if (payload.containsKey("monthlyAttendanceJson")) admin.setMonthlyAttendanceJson(strOrNull(payload.get("monthlyAttendanceJson")));
        if (payload.containsKey("monitorStudentId")) {
            Object mObj = payload.get("monitorStudentId");
            if (mObj == null || String.valueOf(mObj).trim().isEmpty()) {
                admin.setMonitorStudentId(null);
            } else {
                admin.setMonitorStudentId(Long.parseLong(String.valueOf(mObj).trim()));
            }
        }
        admin.setUpdatedAt(LocalDateTime.now());

        homeroomClassAdminRepository.save(admin);
        return ResponseEntity.ok(Map.of("success", true, "message", "រក្សាទុកទិន្នន័យរដ្ឋបាលថ្នាក់ជោគជ័យ"));
    }

    // =========================================================================
    // CLASS MONITOR (ប្រធានថ្នាក់) DELEGATED MONTHLY ATTENDANCE PORTAL
    // Strictly scoped ONLY to the 1 student designated by the homeroom teacher
    // =========================================================================

    private HomeroomClassAdmin findAuthorizedMonitorAdmin(Student stu) {
        if (stu == null || stu.getId() == null) return null;
        List<HomeroomClassAdmin> list = homeroomClassAdminRepository.findByMonitorStudentId(stu.getId());
        for (HomeroomClassAdmin adm : list) {
            if (Objects.equals(adm.getAccountId(), stu.getAccountId())
                    && Objects.equals(adm.getClassName(), stu.getClassName())) {
                return adm;
            }
        }
        return list.isEmpty() ? null : list.get(0);
    }

    @GetMapping("/test/monthly-attendance")
    public String classMonitorMonthlyAttendancePage(Model model) {
        Student stu = (Student) session.getAttribute("loggedInStudent");
        if (stu == null) {
            return "redirect:/test";
        }
        if (stu.getId() != null) {
            stu = studentRepository.findById(stu.getId()).orElse(stu);
            session.setAttribute("loggedInStudent", stu);
        }
        HomeroomClassAdmin admin = findAuthorizedMonitorAdmin(stu);
        if (admin == null) {
            return "redirect:/test/take";
        }
        model.addAttribute("student", stu);
        model.addAttribute("className", admin.getClassName());
        model.addAttribute("academicYear", admin.getAcademicYear() != null ? admin.getAcademicYear() : "២០២៦-២០២៧");
        return "student-monthly-attendance";
    }

    @GetMapping("/api/student-portal/monthly-attendance")
    @ResponseBody
    public ResponseEntity<?> getMonitorMonthlyAttendance() {
        Student stu = (Student) session.getAttribute("loggedInStudent");
        if (stu == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        HomeroomClassAdmin admin = findAuthorizedMonitorAdmin(stu);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "អ្នកមិនមានសិទ្ធិប្រធានថ្នាក់សម្រាប់ស្រង់អវត្តមានទេ"));
        }

        Long accountId = admin.getAccountId();
        String className = admin.getClassName();
        String activeYear = admin.getAcademicYear() != null ? admin.getAcademicYear() : "២០២៦-២០២៧";
        Account teacher = accountId != null ? accountRepository.findById(accountId).orElse(null) : null;

        List<Student> students = studentRepository.findByClassNameAndAccountIdAndAcademicYear(className, accountId, activeYear);
        if (students.isEmpty()) {
            students = studentRepository.findByClassNameAndAccountId(className, accountId);
        }
        students.sort(Comparator.comparing(s -> s.getName() != null ? s.getName() : ""));

        List<Map<String, Object>> studentDtos = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", s.getId());
            dto.put("name", s.getName() != null ? s.getName() : "");
            dto.put("gender", s.getGender() != null ? s.getGender() : "");
            dto.put("studentCode", s.getStudentCode() != null ? s.getStudentCode() : "");
            studentDtos.add(dto);
        }

        Map<String, Object> teacherInfo = new LinkedHashMap<>();
        teacherInfo.put("fullName", teacher != null && teacher.getFullName() != null ? teacher.getFullName() : "");
        teacherInfo.put("schoolName", teacher != null && teacher.getSchoolName() != null ? teacher.getSchoolName() : "វិទ្យាល័យ");
        teacherInfo.put("principalName", teacher != null && teacher.getPrincipalName() != null ? teacher.getPrincipalName() : "");

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("className", className);
        res.put("academicYear", activeYear);
        res.put("monitorName", stu.getName());
        res.put("teacher", teacherInfo);
        res.put("students", studentDtos);
        res.put("monthlyAttendanceJson", admin.getMonthlyAttendanceJson());
        res.put("configJson", admin.getConfigJson());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/api/student-portal/monthly-attendance/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveMonitorMonthlyAttendance(@RequestBody Map<String, Object> payload) {
        Student stu = (Student) session.getAttribute("loggedInStudent");
        if (stu == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        HomeroomClassAdmin admin = findAuthorizedMonitorAdmin(stu);
        if (admin == null) {
            return ResponseEntity.status(403).body(Map.of("error", "អ្នកមិនមានសិទ្ធិប្រធានថ្នាក់សម្រាប់ស្រង់អវត្តមានទេ"));
        }
        if (payload.containsKey("monthlyAttendanceJson")) {
            admin.setMonthlyAttendanceJson(strOrNull(payload.get("monthlyAttendanceJson")));
            admin.setUpdatedAt(LocalDateTime.now());
            homeroomClassAdminRepository.save(admin);
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "ប្រធានថ្នាក់បានរក្សាទុកអវត្តមានតាមខែជោគជ័យ!"));
    }

    private double parseDouble(Object obj, double fallback) {
        Double d = parseDoubleOrNull(obj);
        return d != null ? d : fallback;
    }

    private Double parseDoubleOrNull(Object obj) {
        if (obj == null) return null;
        try {
            return Double.parseDouble(String.valueOf(obj).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIntOrNull(Object obj) {
        if (obj == null || String.valueOf(obj).trim().isEmpty()) return null;
        try {
            return Integer.parseInt(String.valueOf(obj).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String strOrEmpty(Object obj) {
        return obj == null ? "" : String.valueOf(obj).trim();
    }

    private String strOrNull(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}
