package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Feedback;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.FeedbackRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public String showFeedbackPage(HttpSession session, Model model) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        Account teacherAccount = accountRepository.findById(accountId).orElse(null);
        model.addAttribute("teacherAccount", teacherAccount);

        List<Feedback> myFeedbacks = feedbackRepository.findByTeacherAccountIdOrderByCreatedAtDesc(accountId);
        model.addAttribute("myFeedbacks", myFeedbacks);

        return "feedback";
    }

    @PostMapping
    public String submitFeedback(@RequestParam("category") String category,
                                 @RequestParam(value = "priority", defaultValue = "ធម្មតា") String priority,
                                 @RequestParam("title") String title,
                                 @RequestParam("content") String content,
                                 @RequestParam(value = "phone", required = false) String phone,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "សូមបញ្ចូលចំណងជើង និងខ្លឹមសារលម្អិតនៃមតិកែលម្អ!");
            return "redirect:/feedback";
        }

        Account teacherAccount = accountRepository.findById(accountId).orElse(null);

        Feedback feedback = new Feedback();
        feedback.setTeacherAccountId(accountId);
        if (teacherAccount != null) {
            feedback.setTeacherName(teacherAccount.getFullName());
            feedback.setTeacherUsername(teacherAccount.getUsername());
            feedback.setSchoolName(teacherAccount.getSchoolName());
            feedback.setProvince(teacherAccount.getProvince());
            feedback.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : teacherAccount.getPhone());
        } else {
            feedback.setTeacherName((String) session.getAttribute("ACCOUNT_NAME"));
            feedback.setTeacherUsername("teacher");
            feedback.setPhone(phone);
        }

        feedback.setCategory(category != null && !category.trim().isEmpty() ? category.trim() : "ចំណុចខ្វះខាតប្រព័ន្ធ");
        feedback.setPriority(priority);
        feedback.setTitle(title.trim());
        feedback.setContent(content.trim());
        feedback.setStatus("PENDING");
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);

        redirectAttributes.addFlashAttribute("successMessage", "អរគុណច្រើនលោកគ្រូ/អ្នកគ្រូ! មតិកែលម្អត្រូវបានបញ្ជូនទៅកាន់ Admin ដោយជោគជ័យ។");
        return "redirect:/feedback";
    }

    @PostMapping("/delete/{id}")
    public String deleteMyFeedback(@PathVariable("id") Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        Optional<Feedback> fbOpt = feedbackRepository.findById(id);
        if (fbOpt.isPresent()) {
            Feedback fb = fbOpt.get();
            if (accountId.equals(fb.getTeacherAccountId())) {
                feedbackRepository.delete(fb);
                redirectAttributes.addFlashAttribute("successMessage", "បានលុបមតិកែលម្អរួចរាល់។");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "លោកគ្រូមិនមានសិទ្ធិលុបមតិយោបល់នេះទេ!");
            }
        }

        return "redirect:/feedback";
    }
}
