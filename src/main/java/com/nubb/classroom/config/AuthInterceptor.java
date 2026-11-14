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
                                session.setAttribute("ACCOUNT_USERNAME", acc.getUsername());
                                boolean canAccessVocab = com.nubb.classroom.controller.PhonicsController.isAuthorizedForPhonics(acc);
                                session.setAttribute("VOCABULARY_PERMISSION", canAccessVocab);
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

        // Friendly route aliases so teachers/students typing common variations never encounter 404
        if ("/home".equals(path) || "/dashboard".equals(path) || "/index".equals(path)) {
            response.sendRedirect("/");
            return false;
        }
        if ("/exam".equals(path)) {
            response.sendRedirect("/exams");
            return false;
        }
        if ("/lesson".equals(path)) {
            response.sendRedirect("/lessons");
            return false;
        }
        if ("/student".equals(path)) {
            response.sendRedirect("/students");
            return false;
        }
        if ("/score".equals(path)) {
            response.sendRedirect("/scores");
            return false;
        }
        if ("/classes".equals(path) || "/classroom".equals(path)) {
            response.sendRedirect("/classrooms");
            return false;
        }

        // If a student is logged in and visits root "/", redirect to their student portal /test/take
        if ("/".equals(path) && session.getAttribute("loggedInStudent") != null && session.getAttribute("ACCOUNT_ID") == null) {
            response.sendRedirect("/test/take");
            return false;
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
            if (session.getAttribute("loggedInStudent") != null) {
                session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
            }
            return true;
        }

        // Allow temporary PDF download routes (UUID-based token, auto-expires in 5 minutes)
        if (path.startsWith("/api/pdf/") || path.startsWith("/api/diagnostic")) {
            return true;
        }

        // Public certificate verification routes ONLY (for scanning QR codes)
        if (path.startsWith("/certificate/verify") || path.startsWith("/api/certificate/verify")) {
            return true;
        }

        // Allow /lessons and /certificate for logged in students as well as teachers
        if (path.startsWith("/lessons") || path.startsWith("/api/lessons") || path.equals("/certificate")) {
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

        // When navigating teacher-only dashboard routes (not /certificate which is shared), mark active mode as TEACHER
        if (!path.startsWith("/certificate") && !path.startsWith("/api/certificate")) {
            session.setAttribute("ACTIVE_USER_MODE", "TEACHER");
        }

        // Verify account is still active and keep VOCABULARY_PERMISSION synced with DB
        try {
            Optional<Account> currentAccOpt = accountRepository.findById(accountId);
            if (currentAccOpt.isPresent()) {
                Account curr = currentAccOpt.get();
                if (Boolean.FALSE.equals(curr.getActive())) {
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
                boolean canAccessVocab = com.nubb.classroom.controller.PhonicsController.isAuthorizedForPhonics(curr);
                session.setAttribute("VOCABULARY_PERMISSION", canAccessVocab);
            }
        } catch (Exception ignored) {}

        return true;
    }
}
