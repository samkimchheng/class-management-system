package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String addAccount(@ModelAttribute Account account) {
        accountRepository.save(account);
        return "redirect:/admin/accounts";
    }

    @PostMapping("/accounts/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountRepository.deleteById(id);
        return "redirect:/admin/accounts";
    }
}
