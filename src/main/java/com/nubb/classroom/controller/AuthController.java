package com.nubb.classroom.controller;

import com.nubb.classroom.config.LoginRateLimiter;
import com.nubb.classroom.config.TokenSecurityService;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final AccountRepository accountRepository;
    private final TokenSecurityService tokenSecurityService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AccountRepository accountRepository,
                          TokenSecurityService tokenSecurityService,
                          LoginRateLimiter loginRateLimiter) {
        this.accountRepository = accountRepository;
        this.tokenSecurityService = tokenSecurityService;
        this.loginRateLimiter = loginRateLimiter;
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
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Model model) {

        String clientIp = loginRateLimiter.getClientIp(request);

        // 1. Check if IP is currently blocked due to multiple failed attempts
        if (loginRateLimiter.isBlocked(clientIp)) {
            long remainingMins = loginRateLimiter.getRemainingLockoutMinutes(clientIp);
            model.addAttribute("error", "អ្នកបានព្យាយាមវាយលេខសម្ងាត់ខុសច្រើនដងពេក! ដើម្បីសុវត្ថិភាព សូមរង់ចាំប្រហែល " + remainingMins + " នាទីសិន ទើបអាចព្យាយាមម្តងទៀត។");
            return "login";
        }

        String cleanUsername = username != null ? username.trim() : "";
        String cleanPassword = password != null ? password.trim() : "";

        Optional<Account> accOpt = accountRepository.findByUsername(cleanUsername);
        if (accOpt.isEmpty()) {
            // 1. Case-insensitive search on username
            accOpt = accountRepository.findAll().stream()
                    .filter(a -> a.getUsername() != null && a.getUsername().trim().equalsIgnoreCase(cleanUsername))
                    .findFirst();
        }
        if (accOpt.isEmpty()) {
            // 2. Flexible fallback: match by phone number, email, or full name
            String strippedInput = cleanUsername.replaceAll("[\\s-]", "");
            accOpt = accountRepository.findAll().stream()
                    .filter(a -> (a.getPhone() != null && !strippedInput.isEmpty() && a.getPhone().trim().replaceAll("[\\s-]", "").equalsIgnoreCase(strippedInput))
                              || (a.getEmail() != null && a.getEmail().trim().equalsIgnoreCase(cleanUsername))
                              || (a.getFullName() != null && a.getFullName().trim().equalsIgnoreCase(cleanUsername)))
                    .findFirst();
        }

        if (accOpt.isPresent() && (accOpt.get().getPassword().equals(password) || accOpt.get().getPassword().equals(cleanPassword))) {
            Account acc = accOpt.get();
            if (Boolean.FALSE.equals(acc.getActive())) {
                model.addAttribute("error", "គណនីនេះត្រូវបានផ្អាក ឬបិទសិទ្ធិជាបណ្តោះអាសន្ន! សូមទាក់ទង Admin។");
                return "login";
            }

            // Successful login: reset rate limiter
            loginRateLimiter.recordSuccess(clientIp);

            session.setAttribute("ACCOUNT_ID", acc.getId());
            session.setAttribute("ACCOUNT_ROLE", acc.getRole());
            session.setAttribute("ACCOUNT_NAME", acc.getFullName());
            if (acc.getCurrentAcademicYear() != null && !acc.getCurrentAcademicYear().trim().isEmpty()) {
                session.setAttribute("CURRENT_ACADEMIC_YEAR", acc.getCurrentAcademicYear().trim());
            } else {
                session.setAttribute("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧");
            }

            // Generate tamper-proof cryptographic token
            String secureToken = tokenSecurityService.generateAccountToken(acc);

            boolean isHttps = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
            ResponseCookie cookie = ResponseCookie.from("REMEMBER_ACCOUNT_ID", secureToken)
                    .httpOnly(true)
                    .secure(isHttps)
                    .path("/")
                    .maxAge(30L * 24 * 60 * 60) // 30 days
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            if ("ADMIN".equals(acc.getRole())) {
                return "redirect:/admin";
            } else {
                return "redirect:/";
            }
        }

        // Failed login: record attempt
        loginRateLimiter.recordFailedAttempt(clientIp);
        model.addAttribute("error", "បញ្ចូលឈ្មោះ ឬលេខសម្ងាត់មិនត្រឹមត្រូវ!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // Clear persistent cookie
        ResponseCookie cookie = ResponseCookie.from("REMEMBER_ACCOUNT_ID", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        session.invalidate();
        return "redirect:/login";
    }
}
