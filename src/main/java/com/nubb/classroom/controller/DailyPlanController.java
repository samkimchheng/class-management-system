package com.nubb.classroom.controller;

import com.nubb.classroom.WebController;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.DailyTeachingLog;
import com.nubb.classroom.model.Lesson;
import com.nubb.classroom.model.Schedule;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.DailyTeachingLogRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.ScheduleRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DailyPlanController {

    @Autowired
    private HttpSession session;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private DailyTeachingLogRepository dailyTeachingLogRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private static final String[] TIME_RANGES = {
        "07:00 - 08:00 ព្រឹក", // slot 0
        "08:00 - 09:00 ព្រឹក", // slot 1
        "09:00 - 10:00 ព្រឹក", // slot 2
        "10:00 - 11:00 ព្រឹក", // slot 3
        "01:00 - 02:00 រសៀល", // slot 4
        "02:00 - 03:00 រសៀល", // slot 5
        "03:00 - 04:00 រសៀល", // slot 6
        "04:00 - 05:00 រសៀល"  // slot 7
    };

    private static final String[] KHMER_MONTHS = {
        "មករា", "កុម្ភៈ", "មីនា", "មេសា", "ឧសភា", "មិថុនា",
        "កក្កដា", "សីហា", "កញ្ញា", "តុលា", "វិច្ឆិកា", "ធ្នូ"
    };

    public static String toKhmerDateString(LocalDate date) {
        if (date == null) return "";
        String dayName = getKhmerDayName(date.getDayOfWeek());
        String dayNum = WebController.toKhmerNumber(date.getDayOfMonth());
        String monthName = KHMER_MONTHS[date.getMonthValue() - 1];
        String yearNum = WebController.toKhmerNumber(date.getYear());
        return "ថ្ងៃ" + dayName + " ទី" + dayNum + " ខែ" + monthName + " ឆ្នាំ" + yearNum;
    }

    public static String getKhmerDayName(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "ច័ន្ទ";
            case TUESDAY: return "អង្គារ";
            case WEDNESDAY: return "ពុធ";
            case THURSDAY: return "ព្រហស្បតិ៍";
            case FRIDAY: return "សុក្រ";
            case SATURDAY: return "សៅរ៍";
            default: return "អាទិត្យ";
        }
    }

    @GetMapping("/daily-plan")
    public String dailyPlanPage(@RequestParam(value = "date", required = false) String dateStr,
                                Model model) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        Account teacher = accountRepository.findById(accountId).orElse(null);
        if (teacher == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        if (tomorrow.getDayOfWeek() == DayOfWeek.SUNDAY) {
            tomorrow = today.plusDays(2); // If tomorrow is Sunday, default to next Monday
        }

        LocalDate targetDate;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                targetDate = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                targetDate = today;
            }
        } else {
            // Default to today if today has logs or is a regular school day, otherwise tomorrow
            List<DailyTeachingLog> todayLogs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, today);
            if (!todayLogs.isEmpty() || today.getDayOfWeek() != DayOfWeek.SUNDAY) {
                targetDate = today;
            } else {
                targetDate = tomorrow;
            }
        }

        DayOfWeek dow = targetDate.getDayOfWeek();
        String khmerDay = getKhmerDayName(dow);
        int colIndex = dow.getValue() - 1; // 0 for Mon to 5 for Sat, 6 for Sun

        // 1. Fetch weekly schedule
        List<Schedule> weekly = scheduleRepository.findByClassNameAndAccountId("WEEKLY", accountId);
        if (weekly == null || weekly.isEmpty()) {
            weekly = scheduleRepository.findByAccountId(accountId);
        }

        // 2. Fetch existing logs for this date
        List<DailyTeachingLog> existingLogs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, targetDate);
        Map<Integer, DailyTeachingLog> logBySlot = new HashMap<>();
        for (DailyTeachingLog l : existingLogs) {
            if (l.getSlotIndex() != null) {
                logBySlot.put(l.getSlotIndex(), l);
            }
        }

        // 3. Build scheduled slots
        List<Map<String, Object>> slots = new ArrayList<>();
        if (colIndex >= 0 && colIndex <= 5 && weekly != null) {
            for (Schedule s : weekly) {
                if (s == null || s.getSubjectName() == null) continue;
                String classOrSubject = s.getSubjectName().trim();
                if (classOrSubject.isEmpty() || "-".equals(classOrSubject)) continue;

                if ((s.getBlockIndex() % 6) == colIndex) {
                    int slotIndex = s.getBlockIndex() / 6;
                    if (slotIndex < 0 || slotIndex >= 8) continue;

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("slotIndex", slotIndex);
                    item.put("periodNumber", slotIndex + 1);
                    item.put("periodKhmer", WebController.toKhmerNumber(slotIndex + 1));
                    item.put("timeRange", TIME_RANGES[slotIndex]);
                    item.put("isMorning", slotIndex < 4);
                    item.put("className", classOrSubject);

                    String subject = (teacher.getSubject() != null && !teacher.getSubject().trim().isEmpty())
                            ? teacher.getSubject().trim() : "ភាសាអង់គ្លេស";
                    item.put("defaultSubject", subject);

                    DailyTeachingLog saved = logBySlot.get(slotIndex);
                    if (saved != null) {
                        item.put("id", saved.getId());
                        item.put("hasSaved", true);
                        item.put("subjectName", saved.getSubjectName() != null ? saved.getSubjectName() : subject);
                        item.put("lessonTitle", saved.getLessonTitle() != null ? saved.getLessonTitle() : "");
                        item.put("contentSummary", saved.getContentSummary() != null ? saved.getContentSummary() : "");
                        item.put("objectives", saved.getObjectives() != null ? saved.getObjectives() : "");
                        item.put("homework", saved.getHomework() != null ? saved.getHomework() : "");
                        item.put("notes", saved.getNotes() != null ? saved.getNotes() : "");
                    } else {
                        item.put("id", null);
                        item.put("hasSaved", false);
                        item.put("subjectName", subject);
                        item.put("lessonTitle", "");
                        item.put("contentSummary", "");
                        item.put("objectives", "");
                        item.put("homework", "");
                        item.put("notes", "");
                    }

                    slots.add(item);
                }
            }
        }

        // Sort by slotIndex
        slots.sort(Comparator.comparingInt(m -> (Integer) m.get("slotIndex")));

        // 4. Lessons list for quick autocomplete / selection
        List<Lesson> teacherLessons = lessonRepository.findByAccountIdOrderByIdAsc(accountId);
        if (teacherLessons == null) teacherLessons = new ArrayList<>();

        // 5. Recent daily plans history (past 20 logs)
        List<DailyTeachingLog> recentLogs = dailyTeachingLogRepository.findByAccountIdOrderByLogDateDescSlotIndexAsc(accountId);
        // Group recent logs by date
        Map<LocalDate, List<DailyTeachingLog>> logsByDate = new LinkedHashMap<>();
        for (DailyTeachingLog l : recentLogs) {
            logsByDate.computeIfAbsent(l.getLogDate(), k -> new ArrayList<>()).add(l);
        }

        // Available academic years
        String currentAcademicYear = teacher.getCurrentAcademicYear() != null ? teacher.getCurrentAcademicYear() : "២០២៦-២០២៧";

        model.addAttribute("teacherAccount", teacher);
        model.addAttribute("currentAcademicYear", currentAcademicYear);
        model.addAttribute("targetDate", targetDate.toString());
        model.addAttribute("targetDateKhmer", toKhmerDateString(targetDate));
        model.addAttribute("isToday", targetDate.equals(today));
        model.addAttribute("isTomorrow", targetDate.equals(tomorrow));
        model.addAttribute("isYesterday", targetDate.equals(yesterday));
        model.addAttribute("todayStr", today.toString());
        model.addAttribute("tomorrowStr", tomorrow.toString());
        model.addAttribute("yesterdayStr", yesterday.toString());
        model.addAttribute("prevDateStr", targetDate.minusDays(1).toString());
        model.addAttribute("nextDateStr", targetDate.plusDays(1).toString());
        model.addAttribute("khmerDayName", khmerDay);

        long todaySavedCount = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, today).size();
        long tomorrowSavedCount = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, tomorrow).size();
        long yesterdaySavedCount = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, yesterday).size();
        model.addAttribute("todaySavedCount", todaySavedCount);
        model.addAttribute("tomorrowSavedCount", tomorrowSavedCount);
        model.addAttribute("yesterdaySavedCount", yesterdaySavedCount);
        model.addAttribute("hasTeachingSlots", !slots.isEmpty());
        model.addAttribute("slots", slots);
        model.addAttribute("teacherLessons", teacherLessons);
        model.addAttribute("logsByDate", logsByDate);
        model.addAttribute("totalSlots", slots.size());
        long completedSlots = slots.stream().filter(s -> Boolean.TRUE.equals(s.get("hasSaved"))).count();
        model.addAttribute("completedSlots", completedSlots);

        return "daily-plan";
    }

    @PostMapping("/api/daily-plan/save")
    @ResponseBody
    public ResponseEntity<?> saveDailyPlan(@RequestBody Map<String, Object> payload) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        String dateStr = (String) payload.get("date");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបញ្ជាក់កាលបរិច្ឆេទ!"));
        }

        LocalDate logDate;
        try {
            logDate = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "កាលបរិច្ឆេទមិនត្រឹមត្រូវ!"));
        }

        Account teacher = accountRepository.findById(accountId).orElse(null);
        String academicYear = (teacher != null && teacher.getCurrentAcademicYear() != null)
                ? teacher.getCurrentAcademicYear() : "២០២៦-២០២៧";

        String dayOfWeek = getKhmerDayName(logDate.getDayOfWeek());

        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "មិនមានទិន្នន័យខ្លឹមសារមេរៀនត្រូវរក្សាទុកទេ!"));
        }

        int savedCount = 0;
        for (Map<String, Object> item : items) {
            Integer slotIndex = null;
            if (item.get("slotIndex") != null) {
                try { slotIndex = Integer.parseInt(item.get("slotIndex").toString()); } catch (Exception ignored) {}
            }
            if (slotIndex == null) continue;

            DailyTeachingLog log = dailyTeachingLogRepository.findByAccountIdAndLogDateAndSlotIndex(accountId, logDate, slotIndex)
                    .orElse(new DailyTeachingLog());

            log.setAccountId(accountId);
            log.setAcademicYear(academicYear);
            log.setLogDate(logDate);
            log.setDayOfWeek(dayOfWeek);
            log.setSlotIndex(slotIndex);
            log.setPeriodNumber(slotIndex + 1);
            log.setTimeRange((slotIndex >= 0 && slotIndex < TIME_RANGES.length) ? TIME_RANGES[slotIndex] : "");

            if (item.get("className") != null) log.setClassName(item.get("className").toString().trim());
            if (item.get("subjectName") != null) log.setSubjectName(item.get("subjectName").toString().trim());
            if (item.get("lessonTitle") != null) log.setLessonTitle(item.get("lessonTitle").toString().trim());
            if (item.get("contentSummary") != null) log.setContentSummary(item.get("contentSummary").toString().trim());
            if (item.get("objectives") != null) log.setObjectives(item.get("objectives").toString().trim());
            if (item.get("homework") != null) log.setHomework(item.get("homework").toString().trim());
            if (item.get("notes") != null) log.setNotes(item.get("notes").toString().trim());

            dailyTeachingLogRepository.save(log);
            savedCount++;
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "បានរក្សាទុកសេវាប្រចាំថ្ងៃសម្រាប់ " + toKhmerDateString(logDate) + " ចំនួន " + savedCount + " ម៉ោងដោយជោគជ័យ!",
                "savedCount", savedCount,
                "date", dateStr
        ));
    }

    @PostMapping("/api/daily-plan/delete-day")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteDailyPlanDay(@RequestParam("date") String dateStr) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            List<DailyTeachingLog> logs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, date);
            if (!logs.isEmpty()) {
                dailyTeachingLogRepository.deleteAll(logs);
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "បានលុបកំណត់ត្រាសេវាប្រចាំថ្ងៃសម្រាប់ " + toKhmerDateString(date) + " ដោយជោគជ័យ!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "កំហុស៖ " + e.getMessage()));
        }
    }

    @PostMapping("/api/daily-plan/delete-slot")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteDailyPlanSlot(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "slotIndex", required = false) Integer slotIndex) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        try {
            if (id != null) {
                dailyTeachingLogRepository.findById(id).ifPresent(l -> {
                    if (accountId.equals(l.getAccountId())) {
                        dailyTeachingLogRepository.delete(l);
                    }
                });
            } else if (dateStr != null && slotIndex != null) {
                LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                dailyTeachingLogRepository.findByAccountIdAndLogDateAndSlotIndex(accountId, date, slotIndex)
                        .ifPresent(dailyTeachingLogRepository::delete);
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "បានសម្អាត និងលុបម៉ោងបង្រៀននេះដោយជោគជ័យ!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "កំហុស៖ " + e.getMessage()));
        }
    }

    @GetMapping("/api/daily-plan/get")
    @ResponseBody
    public ResponseEntity<?> getDailyPlanJson(@RequestParam("date") String dateStr) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            List<DailyTeachingLog> logs = dailyTeachingLogRepository.findByAccountIdAndLogDateOrderBySlotIndexAsc(accountId, date);
            return ResponseEntity.ok(Map.of("success", true, "date", dateStr, "logs", logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid date"));
        }
    }
}
