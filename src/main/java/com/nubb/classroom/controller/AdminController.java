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
        model.addAttribute("totalFeedbacks", feedbackRepository.count());
        model.addAttribute("pendingFeedbacks", feedbackRepository.countByStatus("PENDING"));
        model.addAttribute("recentFeedbacks", feedbackRepository.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", accountRepository.findAll());
        model.addAttribute("pendingFeedbacks", feedbackRepository.countByStatus("PENDING"));
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
        List<Feedback> list;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            list = feedbackRepository.findByStatusOrderByCreatedAtDesc(status.trim());
        } else {
            list = feedbackRepository.findAllByOrderByCreatedAtDesc();
            status = "ALL";
        }

        model.addAttribute("feedbacks", list);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("totalCount", feedbackRepository.count());
        model.addAttribute("pendingCount", feedbackRepository.countByStatus("PENDING"));
        model.addAttribute("inProgressCount", feedbackRepository.countByStatus("IN_PROGRESS"));
        model.addAttribute("resolvedCount", feedbackRepository.countByStatus("RESOLVED"));

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
