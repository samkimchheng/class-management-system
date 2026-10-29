package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Feedback;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.FeedbackRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final FeedbackRepository feedbackRepository;

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
}
