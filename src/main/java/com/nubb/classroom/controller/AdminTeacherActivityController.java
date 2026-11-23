package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminTeacherActivityController {

    private static final ZoneId CAMBODIA_ZONE = ZoneId.of("Asia/Phnom_Penh");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired private AccountRepository accountRepository;
    @Autowired(required = false) private TeacherActivityLogRepository teacherActivityLogRepository;
    @Autowired(required = false) private ClassroomRepository classroomRepository;
    @Autowired(required = false) private StudentRepository studentRepository;
    @Autowired(required = false) private AttendanceRepository attendanceRepository;
    @Autowired(required = false) private ScoreRepository scoreRepository;
    @Autowired(required = false) private HomeroomScoreRepository homeroomScoreRepository;
    @Autowired(required = false) private HomeroomClassAdminRepository homeroomClassAdminRepository;
    @Autowired(required = false) private DailyTeachingLogRepository dailyTeachingLogRepository;
    @Autowired(required = false) private ExamRepository examRepository;
    @Autowired(required = false) private LessonRepository lessonRepository;

    @GetMapping("/teacher-activity")
    public String teacherActivityPage() {
        return "admin/teacher-activity";
    }

    @GetMapping("/api/teacher-activity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTeacherActivityData(
            @RequestParam(defaultValue = "TODAY") String range,
            @RequestParam(required = false) String date) {

        LocalDate today = LocalDate.now(CAMBODIA_ZONE);
        LocalDate startDate = today;
        LocalDate endDate = today;
        String rangeLabel = "ថ្ងៃនេះ (" + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";

        if ("DATE".equalsIgnoreCase(range) && date != null && !date.isBlank()) {
            try {
                LocalDate parsed = LocalDate.parse(date.trim());
                startDate = parsed;
                endDate = parsed;
                rangeLabel = "ថ្ងៃទី " + parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ignored) {}
        } else if ("7D".equalsIgnoreCase(range)) {
            startDate = today.minusDays(6);
            rangeLabel = "៧ ថ្ងៃចុងក្រោយ";
        } else if ("30D".equalsIgnoreCase(range)) {
            startDate = today.minusDays(29);
            rangeLabel = "៣០ ថ្ងៃចុងក្រោយ";
        } else if ("ALL".equalsIgnoreCase(range)) {
            rangeLabel = "សរុបទាំងអស់ក្នុងប្រព័ន្ធ";
        }

        List<Account> allTeachers = accountRepository.findAll().stream()
                .filter(a -> a.getRole() == null || !"ADMIN".equalsIgnoreCase(a.getRole()))
                .collect(Collectors.toList());

        List<TeacherActivityLog> rangeLogs = List.of();
        List<TeacherActivityLog> allTimeLogs = List.of();
        if (teacherActivityLogRepository != null) {
            allTimeLogs = teacherActivityLogRepository.findAllByOrderByLastUsedAtDesc();
            if ("ALL".equalsIgnoreCase(range)) {
                rangeLogs = allTimeLogs;
            } else if (startDate.equals(endDate)) {
                rangeLogs = teacherActivityLogRepository.findByActivityDateOrderByLastUsedAtDesc(startDate);
            } else {
                rangeLogs = teacherActivityLogRepository.findByActivityDateBetweenOrderByLastUsedAtDesc(startDate, endDate);
            }
        }

        // Group range logs by accountId -> featureKey -> aggregated stats
        Map<Long, Map<String, FeatureAgg>> rangeByAcc = new HashMap<>();
        Map<Long, LocalDateTime> lastSeenByAcc = new HashMap<>();
        Map<Long, String> lastPathByAcc = new HashMap<>();
        Map<Long, Integer> totalHitsByAcc = new HashMap<>();

        for (TeacherActivityLog log : rangeLogs) {
            if (log.getAccountId() == null) continue;
            Long aid = log.getAccountId();
            int hits = log.getHitCount() == null ? 1 : log.getHitCount();
            totalHitsByAcc.merge(aid, hits, Integer::sum);

            if (log.getLastUsedAt() != null) {
                LocalDateTime prev = lastSeenByAcc.get(aid);
                if (prev == null || log.getLastUsedAt().isAfter(prev)) {
                    lastSeenByAcc.put(aid, log.getLastUsedAt());
                    lastPathByAcc.put(aid, log.getLastPath());
                }
            }

            rangeByAcc.computeIfAbsent(aid, k -> new LinkedHashMap<>())
                    .compute(log.getFeatureKey(), (fk, existing) -> {
                        if (existing == null) {
                            return new FeatureAgg(fk, log.getFeatureLabel(), hits, log.getLastUsedAt());
                        }
                        existing.hits += hits;
                        if (log.getLastUsedAt() != null && (existing.lastUsedAt == null || log.getLastUsedAt().isAfter(existing.lastUsedAt))) {
                            existing.lastUsedAt = log.getLastUsedAt();
                        }
                        return existing;
                    });
        }

        // All-time distinct feature keys per teacher from activity logs
        Map<Long, Set<String>> allTimeFeaturesByAcc = new HashMap<>();
        Map<Long, LocalDateTime> allTimeLastSeenByAcc = new HashMap<>();
        for (TeacherActivityLog log : allTimeLogs) {
            if (log.getAccountId() == null) continue;
            allTimeFeaturesByAcc.computeIfAbsent(log.getAccountId(), k -> new HashSet<>()).add(log.getFeatureKey());
            if (log.getLastUsedAt() != null) {
                LocalDateTime prev = allTimeLastSeenByAcc.get(log.getAccountId());
                if (prev == null || log.getLastUsedAt().isAfter(prev)) {
                    allTimeLastSeenByAcc.put(log.getAccountId(), log.getLastUsedAt());
                }
            }
        }

        // Existing system data counts per teacher (so Admin immediately sees system-wide adoption %)
        Map<Long, Integer> classCountMap = new HashMap<>();
        Map<Long, Integer> studentCountMap = new HashMap<>();
        Map<Long, Integer> attCountMap = new HashMap<>();
        Map<Long, Integer> scoreCountMap = new HashMap<>();
        Map<Long, Integer> hrScoreCountMap = new HashMap<>();
        Map<Long, Integer> hrAdminCountMap = new HashMap<>();
        Map<Long, Integer> dailyPlanCountMap = new HashMap<>();
        Map<Long, Integer> examCountMap = new HashMap<>();
        Map<Long, Integer> lessonCountMap = new HashMap<>();

        try {
            if (classroomRepository != null) {
                for (Classroom c : classroomRepository.findAll()) {
                    if (c.getAccountId() != null) classCountMap.merge(c.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (studentRepository != null) {
                for (Student s : studentRepository.findAll()) {
                    if (s.getAccountId() != null) studentCountMap.merge(s.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (attendanceRepository != null) {
                for (Attendance a : attendanceRepository.findAll()) {
                    if (a.getAccountId() != null) attCountMap.merge(a.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (scoreRepository != null) {
                for (Score sc : scoreRepository.findAll()) {
                    if (sc.getAccountId() != null) scoreCountMap.merge(sc.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (homeroomScoreRepository != null) {
                for (HomeroomScore hs : homeroomScoreRepository.findAll()) {
                    if (hs.getAccountId() != null) hrScoreCountMap.merge(hs.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (homeroomClassAdminRepository != null) {
                for (HomeroomClassAdmin ha : homeroomClassAdminRepository.findAll()) {
                    if (ha.getAccountId() != null) hrAdminCountMap.merge(ha.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (dailyTeachingLogRepository != null) {
                for (DailyTeachingLog dl : dailyTeachingLogRepository.findAll()) {
                    if (dl.getAccountId() != null) dailyPlanCountMap.merge(dl.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (examRepository != null) {
                for (Exam ex : examRepository.findAll()) {
                    if (ex.getAccountId() != null) examCountMap.merge(ex.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}
        try {
            if (lessonRepository != null) {
                for (Lesson ls : lessonRepository.findAll()) {
                    if (ls.getAccountId() != null) lessonCountMap.merge(ls.getAccountId(), 1, Integer::sum);
                }
            }
        } catch (Exception ignored) {}

        // Build per-teacher rows + global feature stats
        Map<String, FeatureGlobalAgg> globalFeatureMap = new LinkedHashMap<>();
        List<Map<String, Object>> teacherRows = new ArrayList<>();

        int totalTeachers = allTeachers.size();
        int activeInRange = 0;
        int totalHomeroom = 0, activeHomeroom = 0;
        int totalRegular = 0, activeRegular = 0;
        int grandTotalHits = 0;

        for (Account acc : allTeachers) {
            Long aid = acc.getId();
            boolean isHomeroom = Boolean.TRUE.equals(acc.getHomeroomPermission());
            if (isHomeroom) totalHomeroom++; else totalRegular++;

            Map<String, FeatureAgg> fMap = rangeByAcc.getOrDefault(aid, Collections.emptyMap());
            boolean usedInRange = !fMap.isEmpty();
            if (usedInRange) {
                activeInRange++;
                if (isHomeroom) activeHomeroom++; else activeRegular++;
            }

            int hits = totalHitsByAcc.getOrDefault(aid, 0);
            grandTotalHits += hits;

            // Core modules denominator: 6 for regular teacher, 8 for homeroom teacher
            int coreTarget = isHomeroom ? 8 : 6;
            int distinctPeriodModules = fMap.size();
            int periodUsagePct = Math.min(100, (int) Math.round((distinctPeriodModules * 100.0) / coreTarget));

            // System-wide adoption % (combines real DB data + all-time activity logs)
            int clsCnt = classCountMap.getOrDefault(aid, 0);
            int stuCnt = studentCountMap.getOrDefault(aid, 0);
            int attCnt = attCountMap.getOrDefault(aid, 0);
            int scCnt = scoreCountMap.getOrDefault(aid, 0);
            int hrScCnt = hrScoreCountMap.getOrDefault(aid, 0);
            int hrAdmCnt = hrAdminCountMap.getOrDefault(aid, 0);
            int dpCnt = dailyPlanCountMap.getOrDefault(aid, 0);
            int exCnt = examCountMap.getOrDefault(aid, 0);
            int lsCnt = lessonCountMap.getOrDefault(aid, 0);

            Set<String> adoptedModules = new HashSet<>(allTimeFeaturesByAcc.getOrDefault(aid, Collections.emptySet()));
            if (clsCnt > 0) adoptedModules.add("CLASSROOMS");
            if (stuCnt > 0) adoptedModules.add("STUDENTS");
            if (attCnt > 0) adoptedModules.add("ATTENDANCE");
            if (scCnt > 0) adoptedModules.add("SCORES");
            if (dpCnt > 0) adoptedModules.add("DAILY_PLAN");
            if (exCnt > 0) adoptedModules.add("EXAMS");
            if (lsCnt > 0) adoptedModules.add("LESSONS");
            if (hrScCnt > 0) adoptedModules.add("HOMEROOM_SCORES");
            if (hrAdmCnt > 0) adoptedModules.add("HOMEROOM_ADMIN");

            int systemUsagePct = Math.min(100, (int) Math.round((adoptedModules.size() * 100.0) / coreTarget));

            List<Map<String, Object>> featuresList = new ArrayList<>();
            for (FeatureAgg fa : fMap.values()) {
                Map<String, Object> fm = new LinkedHashMap<>();
                fm.put("key", fa.key);
                fm.put("label", fa.label);
                fm.put("hits", fa.hits);
                fm.put("lastTime", fa.lastUsedAt != null ? fa.lastUsedAt.format(startDate.equals(endDate) && !"ALL".equalsIgnoreCase(range) ? TIME_FMT : DATETIME_FMT) : "");
                featuresList.add(fm);

                globalFeatureMap.compute(fa.key, (k, g) -> {
                    if (g == null) g = new FeatureGlobalAgg(fa.key, fa.label);
                    g.teacherCount++;
                    g.totalHits += fa.hits;
                    return g;
                });
            }
            featuresList.sort((a, b) -> Integer.compare((int) b.get("hits"), (int) a.get("hits")));

            LocalDateTime lastSeen = lastSeenByAcc.get(aid);
            LocalDateTime anyLastSeen = lastSeen != null ? lastSeen : allTimeLastSeenByAcc.get(aid);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", aid);
            row.put("fullName", acc.getFullName() != null ? acc.getFullName() : acc.getUsername());
            row.put("username", acc.getUsername());
            row.put("active", !Boolean.FALSE.equals(acc.getActive()));
            row.put("isHomeroom", isHomeroom);
            row.put("usedInRange", usedInRange);
            row.put("periodUsagePct", periodUsagePct);
            row.put("systemUsagePct", systemUsagePct);
            row.put("totalHits", hits);
            row.put("featureCount", distinctPeriodModules);
            row.put("lastUsedAt", lastSeen != null ? lastSeen.format(DATETIME_FMT) : (anyLastSeen != null ? anyLastSeen.format(DATETIME_FMT) : "-"));
            row.put("lastUsedTime", lastSeen != null ? lastSeen.format(TIME_FMT) : "-");
            row.put("lastPath", lastPathByAcc.getOrDefault(aid, ""));
            row.put("featuresUsed", featuresList);

            Map<String, Object> sysCounts = new LinkedHashMap<>();
            sysCounts.put("classes", clsCnt);
            sysCounts.put("students", stuCnt);
            sysCounts.put("attendance", attCnt);
            sysCounts.put("scores", scCnt);
            sysCounts.put("homeroomScores", hrScCnt);
            sysCounts.put("dailyPlans", dpCnt);
            sysCounts.put("exams", exCnt);
            sysCounts.put("lessons", lsCnt);
            row.put("systemCounts", sysCounts);

            teacherRows.add(row);
        }

        // Sort: active in range first, then by periodUsagePct desc, then totalHits desc, then systemUsagePct desc
        teacherRows.sort((a, b) -> {
            int c1 = Boolean.compare((boolean) b.get("usedInRange"), (boolean) a.get("usedInRange"));
            if (c1 != 0) return c1;
            int c2 = Integer.compare((int) b.get("periodUsagePct"), (int) a.get("periodUsagePct"));
            if (c2 != 0) return c2;
            int c3 = Integer.compare((int) b.get("totalHits"), (int) a.get("totalHits"));
            if (c3 != 0) return c3;
            return Integer.compare((int) b.get("systemUsagePct"), (int) a.get("systemUsagePct"));
        });

        List<Map<String, Object>> featureStats = globalFeatureMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.teacherCount, a.teacherCount))
                .map(g -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("key", g.key);
                    m.put("label", g.label);
                    m.put("teacherCount", g.teacherCount);
                    m.put("teacherPct", totalTeachers > 0 ? Math.round((g.teacherCount * 100.0) / totalTeachers) : 0);
                    m.put("totalHits", g.totalHits);
                    return m;
                }).collect(Collectors.toList());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("todayDate", today.toString());
        summary.put("rangeLabel", rangeLabel);
        summary.put("totalTeachers", totalTeachers);
        summary.put("activeTeachersInRange", activeInRange);
        summary.put("overallUsagePct", totalTeachers > 0 ? Math.round((activeInRange * 100.0) / totalTeachers) : 0);
        summary.put("totalHomeroom", totalHomeroom);
        summary.put("activeHomeroomInRange", activeHomeroom);
        summary.put("homeroomUsagePct", totalHomeroom > 0 ? Math.round((activeHomeroom * 100.0) / totalHomeroom) : 0);
        summary.put("totalRegular", totalRegular);
        summary.put("activeRegularInRange", activeRegular);
        summary.put("regularUsagePct", totalRegular > 0 ? Math.round((activeRegular * 100.0) / totalRegular) : 0);
        summary.put("totalHits", grandTotalHits);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("summary", summary);
        out.put("featureStats", featureStats);
        out.put("teachers", teacherRows);
        return ResponseEntity.ok(out);
    }

    private static class FeatureAgg {
        String key;
        String label;
        int hits;
        LocalDateTime lastUsedAt;
        FeatureAgg(String key, String label, int hits, LocalDateTime lastUsedAt) {
            this.key = key;
            this.label = label;
            this.hits = hits;
            this.lastUsedAt = lastUsedAt;
        }
    }

    private static class FeatureGlobalAgg {
        String key;
        String label;
        int teacherCount = 0;
        int totalHits = 0;
        FeatureGlobalAgg(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }
}
