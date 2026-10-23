package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;

    public AdminController(AccountRepository accountRepository, StudentRepository studentRepository) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalStudents", studentRepository.count());
        model.addAttribute("totalAccounts", accountRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        model.addAttribute("accounts", accountRepository.findAll());
        return "admin/accounts";
    }

    @PostMapping("/accounts/add")
    public String addAccount(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        if (account.getActive() == null) {
            account.setActive(true);
        }
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "បានបង្កើតគណនី «" + account.getFullName() + "» ដោយជោគជ័យ!");
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/toggle-status/{id}")
    public String toggleAccountStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Long myId = (Long) session.getAttribute("ACCOUNT_ID");
        if (id.equals(myId)) {
            redirectAttributes.addFlashAttribute("error", "មិនអាចបិទសិទ្ធិគណនីផ្ទាល់ខ្លួនរបស់អ្នកបានទេ!");
            return "redirect:/admin/accounts";
        }

        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isPresent()) {
            Account acc = accOpt.get();
            boolean currentStatus = acc.getActive() != null ? acc.getActive() : true;
            acc.setActive(!currentStatus);
            accountRepository.save(acc);

            String statusText = acc.getActive() ? "បើកសិទ្ធិ" : "បិទសិទ្ធិ";
            redirectAttributes.addFlashAttribute("successMessage", "បាន" + statusText + "គណនី «" + acc.getFullName() + "» ដោយជោគជ័យ!");
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/delete/{id}")
    public String deleteAccount(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long myId = (Long) session.getAttribute("ACCOUNT_ID");
        if (id.equals(myId)) {
            redirectAttributes.addFlashAttribute("error", "មិនអាចលុបគណនីផ្ទាល់ខ្លួនរបស់អ្នកបានទេ!");
            return "redirect:/admin/accounts";
        }
        accountRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "បានលុបគណនីដោយជោគជ័យ!");
        return "redirect:/admin/accounts";
    }
}
