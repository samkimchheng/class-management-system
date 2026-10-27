package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.AccountRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final AccountRepository accountRepository;

    public AuthController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, HttpServletRequest request) {
        if (session.getAttribute("ACCOUNT_ROLE") != null) {
            String role = (String) session.getAttribute("ACCOUNT_ROLE");
            if ("ADMIN".equals(role)) return "redirect:/admin";
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        
        Optional<Account> accOpt = accountRepository.findByUsername(username);
        
        if (accOpt.isPresent() && accOpt.get().getPassword().equals(password)) {
            Account acc = accOpt.get();
            if (Boolean.FALSE.equals(acc.getActive())) {
                model.addAttribute("error", "គណនីនេះត្រូវបានផ្អាក ឬបិទសិទ្ធិជាបណ្តោះអាសន្ន! សូមទាក់ទង Admin។");
                return "login";
            }
            session.setAttribute("ACCOUNT_ID", acc.getId());
            session.setAttribute("ACCOUNT_ROLE", acc.getRole());
            session.setAttribute("ACCOUNT_NAME", acc.getFullName());
            if (acc.getCurrentAcademicYear() != null && !acc.getCurrentAcademicYear().trim().isEmpty()) {
                session.setAttribute("CURRENT_ACADEMIC_YEAR", acc.getCurrentAcademicYear().trim());
            } else {
                session.setAttribute("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧");
            }
            
            // Set 30-day persistent cookie
            Cookie cookie = new Cookie("REMEMBER_ACCOUNT_ID", String.valueOf(acc.getId()));
            cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            if ("ADMIN".equals(acc.getRole())) {
                return "redirect:/admin";
            } else {
                return "redirect:/";
            }
        }
        
        model.addAttribute("error", "បញ្ចូលឈ្មោះ ឬលេខសម្ងាត់មិនត្រឹមត្រូវ!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // Clear persistent cookie
        Cookie cookie = new Cookie("REMEMBER_ACCOUNT_ID", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        session.invalidate();
        return "redirect:/login";
    }
}
