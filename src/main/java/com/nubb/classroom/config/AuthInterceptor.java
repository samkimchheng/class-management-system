package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TokenSecurityService tokenSecurityService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String path = request.getRequestURI();

        // 0. Disallow access to H2 Console from public requests
        if (path.startsWith("/h2-console")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            return false;
        }

        // 1. Auto-restore Teacher/Admin session from persistent cookie with cryptographic signature check
        if (session.getAttribute("ACCOUNT_ID") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("REMEMBER_ACCOUNT_ID".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Optional<Account> accOpt = tokenSecurityService.validateAccountToken(c.getValue());
                            if (accOpt.isPresent()) {
                                Account acc = accOpt.get();
                                if (Boolean.FALSE.equals(acc.getActive())) {
                                    ResponseCookie clr = ResponseCookie.from("REMEMBER_ACCOUNT_ID", "")
                                            .httpOnly(true)
                                            .path("/")
                                            .maxAge(0)
                                            .sameSite("Lax")
                                            .build();
                                    response.addHeader(HttpHeaders.SET_COOKIE, clr.toString());
                                    response.sendRedirect("/login?disabled=true");
                                    return false;
                                }
                                session.setAttribute("ACCOUNT_ID", acc.getId());
                                session.setAttribute("ACCOUNT_ROLE", acc.getRole());
                                session.setAttribute("ACCOUNT_NAME", acc.getFullName());
                                if (acc.getCurrentAcademicYear() != null && !acc.getCurrentAcademicYear().trim().isEmpty()) {
                                    session.setAttribute("CURRENT_ACADEMIC_YEAR", acc.getCurrentAcademicYear().trim());
                                } else {
                                    session.setAttribute("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧");
                                }
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        // 2. Auto-restore Student session from persistent cookie with cryptographic signature check
        if (session.getAttribute("loggedInStudent") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("REMEMBER_STUDENT_ID".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Optional<Student> studentOpt = tokenSecurityService.validateStudentToken(c.getValue());
                            if (studentOpt.isPresent()) {
                                session.setAttribute("loggedInStudent", studentOpt.get());
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        // Admin paths require ADMIN role
        if (path.startsWith("/admin")) {
            String role = (String) session.getAttribute("ACCOUNT_ROLE");
            if (role == null || !"ADMIN".equals(role)) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }

        // Allow student portal API and test routes if loggedInStudent is present
        if (path.startsWith("/test") || path.startsWith("/api/student-portal")) {
            if (session.getAttribute("loggedInStudent") == null && !path.equals("/test/login") && !path.equals("/test")) {
                response.sendRedirect("/test");
                return false;
            }
            return true;
        }

        // Public certificate verification routes ONLY (for scanning QR codes)
        if (path.startsWith("/certificate/verify") || path.startsWith("/api/certificate/verify")) {
            return true;
        }

        // Allow /lessons for logged in students as well as teachers
        if (path.startsWith("/lessons") || path.startsWith("/api/lessons")) {
            if (session.getAttribute("ACCOUNT_ID") == null && session.getAttribute("loggedInStudent") == null) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }

        // Other paths (including /certificate and /api/certificate/signature) require an authenticated teacher or admin
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            if (path.startsWith("/api/")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }

        // Verify account is still active (not disabled by admin)
        try {
            Optional<Account> currentAccOpt = accountRepository.findById(accountId);
            if (currentAccOpt.isPresent() && Boolean.FALSE.equals(currentAccOpt.get().getActive())) {
                session.invalidate();
                ResponseCookie clr = ResponseCookie.from("REMEMBER_ACCOUNT_ID", "")
                        .httpOnly(true)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, clr.toString());
                response.sendRedirect("/login?disabled=true");
                return false;
            }
        } catch (Exception ignored) {}

        return true;
    }
}
