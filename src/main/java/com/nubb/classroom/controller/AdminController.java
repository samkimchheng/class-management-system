package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.ExternalLearner;
import com.nubb.classroom.model.Feedback;
import com.nubb.classroom.model.LeaveRequest;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.StudentPhonicsProgress;
import com.nubb.classroom.model.StudentVocabularyProgress;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ExternalLearnerRepository;
import com.nubb.classroom.repository.FeedbackRepository;
import com.nubb.classroom.repository.LeaveRequestRepository;
import com.nubb.classroom.repository.PhonicsItemRepository;
import com.nubb.classroom.repository.StudentPhonicsProgressRepository;
import com.nubb.classroom.repository.StudentRepository;
import com.nubb.classroom.repository.StudentVocabularyProgressRepository;
import com.nubb.classroom.repository.VocabularyWordRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.nubb.classroom.service.BackupRestoreService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired(required = false)
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired(required = false)
    private ExternalLearnerRepository externalLearnerRepository;

    @Autowired(required = false)
    private PhonicsItemRepository phonicsItemRepository;

    @Autowired(required = false)
    private StudentPhonicsProgressRepository studentPhonicsProgressRepository;

    @Autowired(required = false)
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired(required = false)
    private StudentVocabularyProgressRepository studentVocabularyProgressRepository;

    @Autowired
    private BackupRestoreService backupRestoreService;

    public AdminController(AccountRepository accountRepository, 
                           StudentRepository studentRepository,
                           FeedbackRepository feedbackRepository) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalAccounts", accountRepository.count());
        long totalFb = 0, pendingFb = 0;
        List<Feedback> recentFb = List.of();
        try {
            totalFb = feedbackRepository.count();
            pendingFb = feedbackRepository.countByStatus("PENDING");
            recentFb = feedbackRepository.findAllByOrderByCreatedAtDesc().stream().limit(5).toList();
        } catch (Exception ignored) {}
        model.addAttribute("totalFeedbacks", totalFb);
        model.addAttribute("pendingFeedbacks", pendingFb);
        model.addAttribute("recentFeedbacks", recentFb);

        List<LeaveRequest> leaveReqs = List.of();
        long pendingLeaves = 0;
        Map<Long, String> teacherNames = new HashMap<>();
        try {
            for (Account acc : accountRepository.findAll()) {
                if (acc.getId() != null && acc.getFullName() != null) {
                    teacherNames.put(acc.getId(), acc.getFullName());
                }
            }
            if (leaveRequestRepository != null) {
                leaveReqs = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
                pendingLeaves = leaveReqs.stream().filter(LeaveRequest::isPending).count();
            }
        } catch (Exception ignored) {}
        model.addAttribute("leaveRequests", leaveReqs);
        model.addAttribute("totalLeaveRequests", leaveReqs.size());
        model.addAttribute("pendingLeaveRequests", pendingLeaves);
        model.addAttribute("teacherNames", teacherNames);
        return "admin/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        List<Account> accounts = accountRepository.findAll();
        model.addAttribute("accounts", accounts);

        long totalTeachers = accounts.stream()
                .filter(a -> !"ADMIN".equalsIgnoreCase(a.getRole()))
                .count();
        long activeTeachers = accounts.stream()
                .filter(a -> !"ADMIN".equalsIgnoreCase(a.getRole()) && (a.getActive() == null || a.getActive()))
                .count();
        model.addAttribute("totalTeachers", totalTeachers);
        model.addAttribute("activeTeachers", activeTeachers);

        long pendingFb = 0;
        try {
            pendingFb = feedbackRepository.countByStatus("PENDING");
        } catch (Exception ignored) {}
        model.addAttribute("pendingFeedbacks", pendingFb);
        return "admin/accounts";
    }

    @PostMapping("/accounts/add")
    public String addAccount(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        if (account.getActive() == null) {
            account.setActive(true);
        }
        if (account.getProvince() == null || account.getProvince().trim().isEmpty()) {
            account.setProvince("ខេត្តបាត់ដំបង");
        }
        if (account.getPrincipalName() == null || account.getPrincipalName().trim().isEmpty()) {
            account.setPrincipalName("នាយក សាលា");
        }
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "បានបង្កើតគណនី «" + account.getFullName() + "» ដោយជោគជ័យ!");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/update")
    public String updateAccount(
            @RequestParam("id") Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "teachingClasses", required = false) String teachingClasses,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "telegram", required = false) String telegram,
            @RequestParam(value = "vocabularyPermission", required = false) Boolean vocabularyPermission,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            acc.setFullName(fullName);
            acc.setUsername(username);
            acc.setPassword(password);
            if (role != null && !role.trim().isEmpty()) {
                acc.setRole(role);
            }
            if (subject != null) acc.setSubject(subject.trim());
            if (teachingClasses != null) acc.setTeachingClasses(teachingClasses.trim());
            if (phone != null) acc.setPhone(phone.trim());
            if (telegram != null) acc.setTelegram(telegram.trim());
            if (vocabularyPermission != null) {
                acc.setVocabularyPermission(vocabularyPermission);
            }
            accountRepository.save(acc);

            // Update session name if Admin edited their own account
            Long myId = (Long) session.getAttribute("ACCOUNT_ID");
            if (id.equals(myId)) {
                session.setAttribute("ACCOUNT_NAME", acc.getFullName());
            }

            redirectAttributes.addFlashAttribute("successMessage", "បានកែប្រែព័ត៌មានគណនី «" + fullName + "» ដោយជោគជ័យ!");
        } else {
            redirectAttributes.addFlashAttribute("error", "រកមិនឃើញគណនីដែលត្រូវកែប្រែទេ!");
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/toggle-status/{id}")
    public String toggleAccountStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long myId = (Long) session.getAttribute("ACCOUNT_ID");
        if (id.equals(myId)) {
            redirectAttributes.addFlashAttribute("error", "មិនអាចបិទដំណើរការគណនីផ្ទាល់ខ្លួនបានទេ!");
            return "redirect:/admin/accounts";
        }

        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            boolean currentStatus = acc.getActive() != null ? acc.getActive() : true;
            acc.setActive(!currentStatus);
            accountRepository.save(acc);

            String statusText = acc.getActive() ? "បើកដំណើរការ" : "បិទដំណើរការ";
            redirectAttributes.addFlashAttribute("successMessage", "បាន" + statusText + "គណនី «" + acc.getFullName() + "» ដោយជោគជ័យ!");
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/toggle-all")
    public String toggleAllTeachers(
            @RequestParam(value = "active", required = false) Boolean active,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long myId = (Long) session.getAttribute("ACCOUNT_ID");
        List<Account> allAccounts = accountRepository.findAll();

        if (active == null) {
            boolean anyActive = allAccounts.stream()
                    .filter(a -> !"ADMIN".equalsIgnoreCase(a.getRole()) && (myId == null || !myId.equals(a.getId())))
                    .anyMatch(a -> a.getActive() == null || a.getActive());
            active = !anyActive;
        }

        int affectedCount = 0;
        for (Account acc : allAccounts) {
            // Never disable Admin accounts or current user
            if ("ADMIN".equalsIgnoreCase(acc.getRole()) || (myId != null && myId.equals(acc.getId()))) {
                continue;
            }
            acc.setActive(active);
            accountRepository.save(acc);
            affectedCount++;
        }

        String actionText = active ? "បើកដំណើរការ" : "បិទដំណើរការ";
        redirectAttributes.addFlashAttribute("successMessage", 
                "បាន" + actionText + "គណនីគ្រូបង្រៀនទាំងអស់ (ចំនួន " + affectedCount + " គណនី) ដោយជោគជ័យ!");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/delete/{id}")
    public String deleteAccount(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long myId = (Long) session.getAttribute("ACCOUNT_ID");
        if (id.equals(myId)) {
            redirectAttributes.addFlashAttribute("error", "មិនអាចលុបគណនីផ្ទាល់ខ្លួនបានទេ!");
            return "redirect:/admin/accounts";
        }
        accountRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "បានលុបគណនីដោយជោគជ័យ!");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/toggle-vocabulary/{id}")
    public String toggleVocabularyPermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            boolean current = Boolean.TRUE.equals(acc.getVocabularyPermission());
            acc.setVocabularyPermission(!current);
            accountRepository.save(acc);
            String state = !current ? "បានបើកសិទ្ធិប្រើប្រាស់វាក្យសព្ទ" : "បានបិទសិទ្ធិប្រើប្រាស់វាក្យសព្ទ";
            redirectAttributes.addFlashAttribute("successMessage", state + " សម្រាប់ «" + acc.getFullName() + "» ដោយជោគជ័យ!");
        }
        return "redirect:/admin/accounts";
    }

    // ================= Feedback Management (មតិកែលម្អប្រព័ន្ធ) =================

    @GetMapping("/feedbacks")
    public String feedbacks(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Feedback> list = List.of();
        long total = 0, pending = 0, inProgress = 0, resolved = 0;
        try {
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                list = feedbackRepository.findByStatusOrderByCreatedAtDesc(status.trim());
            } else {
                list = feedbackRepository.findAllByOrderByCreatedAtDesc();
                status = "ALL";
            }
            total = feedbackRepository.count();
            pending = feedbackRepository.countByStatus("PENDING");
            inProgress = feedbackRepository.countByStatus("IN_PROGRESS");
            resolved = feedbackRepository.countByStatus("RESOLVED");
        } catch (Exception ignored) {}

        model.addAttribute("feedbacks", list);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("totalCount", total);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("inProgressCount", inProgress);
        model.addAttribute("resolvedCount", resolved);

        return "admin/feedbacks";
    }

    @PostMapping("/feedbacks/{id}/status")
    public String updateFeedbackStatus(@PathVariable("id") Long id,
                                       @RequestParam("status") String newStatus,
                                       RedirectAttributes redirectAttributes) {
        Optional<Feedback> fbOpt = feedbackRepository.findById(id);
        if (fbOpt.isPresent()) {
            Feedback fb = fbOpt.get();
            fb.setStatus(newStatus);
            fb.setUpdatedAt(LocalDateTime.now());
            feedbackRepository.save(fb);
            redirectAttributes.addFlashAttribute("successMessage", "បានធ្វើបច្ចុប្បន្នភាពស្ថានភាពទៅជា «" + fb.getStatusKhmer() + "» ដោយជោគជ័យ!");
        }
        return "redirect:/admin/feedbacks";
    }

    @PostMapping("/feedbacks/{id}/reply")
    public String replyFeedback(@PathVariable("id") Long id,
                                @RequestParam("adminReply") String adminReply,
                                @RequestParam(value = "status", required = false) String status,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Optional<Feedback> fbOpt = feedbackRepository.findById(id);
        if (fbOpt.isPresent()) {
            Feedback fb = fbOpt.get();
            fb.setAdminReply(adminReply != null ? adminReply.trim() : "");
            fb.setAdminReplyBy((String) session.getAttribute("ACCOUNT_NAME"));
            fb.setRepliedAt(LocalDateTime.now());
            fb.setUpdatedAt(LocalDateTime.now());
            if (status != null && !status.trim().isEmpty()) {
                fb.setStatus(status.trim());
            }
            feedbackRepository.save(fb);
            redirectAttributes.addFlashAttribute("successMessage", "បានឆ្លើយតបមតិកែលម្អដោយជោគជ័យ!");
        }
        return "redirect:/admin/feedbacks";
    }

    @PostMapping("/feedbacks/{id}/delete")
    public String deleteFeedback(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "បានលុបមតិកែលម្អដោយជោគជ័យ!");
        }
        return "redirect:/admin/feedbacks";
    }

    // ================= Database Backup & Restore =================

    @GetMapping("/backup/download")
    public ResponseEntity<byte[]> downloadBackup() {
        try {
            String sql = backupRestoreService.generateSqlBackup();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "classroom_backup_" + timestamp + ".sql";

            byte[] bytes = sql.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("/* Backup Error: " + e.getMessage() + " */").getBytes(StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/backup/restore")
    public String restoreBackup(
            @RequestParam("sqlFile") MultipartFile sqlFile,
            RedirectAttributes redirectAttributes) {
        if (sqlFile == null || sqlFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "សូមជ្រើសរើសឯកសារ Backup SQL ជាមុនសិន!");
            return "redirect:/admin";
        }

        try {
            String sqlContent = new String(sqlFile.getBytes(), StandardCharsets.UTF_8);
            BackupRestoreService.RestoreResult result = backupRestoreService.restoreFromSql(sqlContent);
            if (result.errorCount > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "បានបញ្ចូលទិន្នន័យ Restore រួចរាល់! (" + result.executedCount + " statements ជោគជ័យ, " + result.errorCount + " warnings)");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "🎉 បានបញ្ចូលទិន្នន័យ Restore ជោគជ័យ ១០០%! (សរុប " + result.executedCount + " statements)");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "កំហុសក្នុងការ Restore ទិន្នន័យ៖ " + e.getMessage());
        }
        return "redirect:/admin";
    }

    // ================= External English Learners (Admin-Only) =================

    @GetMapping("/english-learners")
    public String englishLearnersPage(Model model) {
        List<ExternalLearner> learners = externalLearnerRepository != null
                ? externalLearnerRepository.findAllByOrderByIdDesc()
                : List.of();

        long totalPhonicsItems = 209;
        try {
            if (phonicsItemRepository != null) {
                long c = phonicsItemRepository.findByActiveTrueOrderByPhonicsLevelAscItemNumberAsc().size();
                if (c > 0) totalPhonicsItems = c;
            }
        } catch (Exception ignored) {}

        long totalVocabWords = 1057;
        try {
            if (vocabularyWordRepository != null) {
                long g10 = vocabularyWordRepository.countByGradeLevelAndActiveTrue(10);
                long g11 = vocabularyWordRepository.countByGradeLevelAndActiveTrue(11);
                long g12 = vocabularyWordRepository.countByGradeLevelAndActiveTrue(12);
                if (g10 + g11 + g12 > 0) totalVocabWords = g10 + g11 + g12;
            }
        } catch (Exception ignored) {}

        List<Map<String, Object>> learnerRows = new java.util.ArrayList<>();
        long activeCount = 0;

        for (ExternalLearner ext : learners) {
            if (!Boolean.FALSE.equals(ext.getActive())) {
                activeCount++;
            }
            Long virtualId = ext.getVirtualStudentId();

            List<StudentPhonicsProgress> phonicsProg = (studentPhonicsProgressRepository != null)
                    ? studentPhonicsProgressRepository.findByStudentId(virtualId)
                    : List.of();
            long phonicsMastered = phonicsProg.stream().filter(p -> Boolean.TRUE.equals(p.getMastered())).count();
            long phonicsListen = phonicsProg.stream().filter(p -> Boolean.TRUE.equals(p.getListeningCompleted())).count();
            long phonicsRead = phonicsProg.stream().filter(p -> Boolean.TRUE.equals(p.getReadingCompleted())).count();
            long phonicsSpeak = phonicsProg.stream().filter(p -> Boolean.TRUE.equals(p.getSpeakingCompleted())).count();
            long phonicsWrite = phonicsProg.stream().filter(p -> Boolean.TRUE.equals(p.getWritingCompleted())).count();
            int phonicsPercent = (int) Math.round(((double) phonicsMastered / Math.max(1, totalPhonicsItems)) * 100.0);

            List<StudentVocabularyProgress> vocabProg = (studentVocabularyProgressRepository != null)
                    ? studentVocabularyProgressRepository.findByStudentId(virtualId)
                    : List.of();
            long vocabMastered = vocabProg.stream().filter(p -> Boolean.TRUE.equals(p.getMastered())).count();
            int vocabPercent = (int) Math.round(((double) vocabMastered / Math.max(1, totalVocabWords)) * 100.0);

            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("learner", ext);
            row.put("phonicsMastered", phonicsMastered);
            row.put("phonicsTotal", totalPhonicsItems);
            row.put("phonicsPercent", phonicsPercent);
            row.put("phonicsListen", phonicsListen);
            row.put("phonicsRead", phonicsRead);
            row.put("phonicsSpeak", phonicsSpeak);
            row.put("phonicsWrite", phonicsWrite);
            row.put("vocabMastered", vocabMastered);
            row.put("vocabTotal", totalVocabWords);
            row.put("vocabPercent", vocabPercent);
            learnerRows.add(row);
        }

        long pendingFb = 0;
        try {
            pendingFb = feedbackRepository.countByStatus("PENDING");
        } catch (Exception ignored) {}

        model.addAttribute("learners", learners);
        model.addAttribute("learnerRows", learnerRows);
        model.addAttribute("totalLearners", learners.size());
        model.addAttribute("activeLearners", activeCount);
        model.addAttribute("pendingFeedbacks", pendingFb);
        return "admin/english-learners";
    }

    @PostMapping("/english-learners/add")
    public String addExternalLearner(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "gender", defaultValue = "ប្រុស") String gender,
            @RequestParam("learnerCode") String learnerCode,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "allowPhonics", defaultValue = "false") boolean allowPhonics,
            @RequestParam(value = "allowVocabulary", defaultValue = "false") boolean allowVocabulary,
            RedirectAttributes redirectAttributes) {

        if (externalLearnerRepository == null) {
            return "redirect:/admin/english-learners";
        }

        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanCode = learnerCode != null ? learnerCode.trim() : "";

        if (cleanName.isEmpty() || cleanCode.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "សូមបំពេញឈ្មោះ និងលេខកូដចូលរៀនឱ្យបានត្រឹមត្រូវ!");
            return "redirect:/admin/english-learners";
        }

        if (externalLearnerRepository.findByLearnerCodeIgnoreCase(cleanCode).isPresent()
                || studentRepository.findAll().stream().anyMatch(s -> s.getStudentCode() != null && s.getStudentCode().trim().equalsIgnoreCase(cleanCode))) {
            redirectAttributes.addFlashAttribute("error", "លេខកូដ «" + cleanCode + "» មានរួចហើយ សូមជ្រើសរើសលេខកូដផ្សេង!");
            return "redirect:/admin/english-learners";
        }

        ExternalLearner ext = new ExternalLearner();
        ext.setFullName(cleanName);
        ext.setGender(gender);
        ext.setLearnerCode(cleanCode);
        ext.setPhone(phone != null ? phone.trim() : "");
        ext.setNote(note != null ? note.trim() : "");
        ext.setAllowPhonics(allowPhonics);
        ext.setAllowVocabulary(allowVocabulary);
        ext.setActive(true);
        ext.setCreatedAt(LocalDateTime.now());
        externalLearnerRepository.save(ext);

        redirectAttributes.addFlashAttribute("successMessage", "✅ បានបញ្ចូលអ្នករៀនក្រៅថ្នាក់ «" + cleanName + "» (កូដ៖ " + cleanCode + ") ដោយជោគជ័យ!");
        return "redirect:/admin/english-learners";
    }

    @PostMapping("/english-learners/edit")
    public String editExternalLearner(
            @RequestParam("id") Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "gender", defaultValue = "ប្រុស") String gender,
            @RequestParam("learnerCode") String learnerCode,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "allowPhonics", defaultValue = "false") boolean allowPhonics,
            @RequestParam(value = "allowVocabulary", defaultValue = "false") boolean allowVocabulary,
            RedirectAttributes redirectAttributes) {

        if (externalLearnerRepository == null) {
            return "redirect:/admin/english-learners";
        }

        Optional<ExternalLearner> opt = externalLearnerRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "រកមិនឃើញគណនីអ្នករៀននេះទេ!");
            return "redirect:/admin/english-learners";
        }

        String cleanName = fullName != null ? fullName.trim() : "";
        String cleanCode = learnerCode != null ? learnerCode.trim() : "";

        Optional<ExternalLearner> existingCode = externalLearnerRepository.findByLearnerCodeIgnoreCase(cleanCode);
        if (existingCode.isPresent() && !existingCode.get().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "លេខកូដ «" + cleanCode + "» ត្រូវបានប្រើប្រាស់រួចហើយ!");
            return "redirect:/admin/english-learners";
        }

        ExternalLearner ext = opt.get();
        ext.setFullName(cleanName);
        ext.setGender(gender);
        ext.setLearnerCode(cleanCode);
        ext.setPhone(phone != null ? phone.trim() : "");
        ext.setNote(note != null ? note.trim() : "");
        ext.setAllowPhonics(allowPhonics);
        ext.setAllowVocabulary(allowVocabulary);
        externalLearnerRepository.save(ext);

        redirectAttributes.addFlashAttribute("successMessage", "✅ បានកែប្រែព័ត៌មានអ្នករៀន «" + cleanName + "» ដោយជោគជ័យ!");
        return "redirect:/admin/english-learners";
    }

    @PostMapping("/english-learners/toggle-status/{id}")
    public String toggleExternalLearnerStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (externalLearnerRepository != null) {
            externalLearnerRepository.findById(id).ifPresent(ext -> {
                boolean next = Boolean.FALSE.equals(ext.getActive());
                ext.setActive(next);
                externalLearnerRepository.save(ext);
                redirectAttributes.addFlashAttribute("successMessage",
                        (next ? "🔓 បានបើកដំណើរការគណនី «" : "🔒 បានផ្អាកដំណើរការគណនី «") + ext.getFullName() + "» រួចរាល់!");
            });
        }
        return "redirect:/admin/english-learners";
    }

    @PostMapping("/english-learners/toggle-phonics/{id}")
    public String toggleExternalLearnerPhonics(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (externalLearnerRepository != null) {
            externalLearnerRepository.findById(id).ifPresent(ext -> {
                boolean next = Boolean.FALSE.equals(ext.getAllowPhonics());
                ext.setAllowPhonics(next);
                externalLearnerRepository.save(ext);
                redirectAttributes.addFlashAttribute("successMessage",
                        "បានកែប្រែសិទ្ធិរៀនប្រកបអង់គ្លេស (Phonics L1–L5) សម្រាប់ «" + ext.getFullName() + "»!");
            });
        }
        return "redirect:/admin/english-learners";
    }

    @PostMapping("/english-learners/toggle-vocab/{id}")
    public String toggleExternalLearnerVocab(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (externalLearnerRepository != null) {
            externalLearnerRepository.findById(id).ifPresent(ext -> {
                boolean next = Boolean.FALSE.equals(ext.getAllowVocabulary());
                ext.setAllowVocabulary(next);
                externalLearnerRepository.save(ext);
                redirectAttributes.addFlashAttribute("successMessage",
                        "បានកែប្រែសិទ្ធិរៀនវាក្យសព្ទ (Vocab ថ្នាក់ទី១០-១២) សម្រាប់ «" + ext.getFullName() + "»!");
            });
        }
        return "redirect:/admin/english-learners";
    }

    @PostMapping("/english-learners/delete/{id}")
    public String deleteExternalLearner(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        if (externalLearnerRepository != null) {
            externalLearnerRepository.findById(id).ifPresent(ext -> {
                Long virtualId = ext.getVirtualStudentId();
                try {
                    if (studentPhonicsProgressRepository != null) {
                        studentPhonicsProgressRepository.deleteByStudentId(virtualId);
                    }
                    if (studentVocabularyProgressRepository != null) {
                        studentVocabularyProgressRepository.deleteByStudentId(virtualId);
                    }
                } catch (Exception ignored) {}
                externalLearnerRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "🗑️ បានលុបគណនីអ្នករៀនក្រៅថ្នាក់ «" + ext.getFullName() + "» រួចរាល់!");
            });
        }
        return "redirect:/admin/english-learners";
    }
}
