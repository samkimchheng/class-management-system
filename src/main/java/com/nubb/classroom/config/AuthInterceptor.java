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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String path = request.getRequestURI();

        // 1. Auto-restore Teacher/Admin session from persistent cookie if missing
        if (session.getAttribute("ACCOUNT_ID") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("REMEMBER_ACCOUNT_ID".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Long accountId = Long.parseLong(c.getValue());
                            Optional<Account> accOpt = accountRepository.findById(accountId);
                            if (accOpt.isPresent()) {
                                Account acc = accOpt.get();
                                if (Boolean.FALSE.equals(acc.getActive())) {
                                    Cookie clr = new Cookie("REMEMBER_ACCOUNT_ID", "");
                                    clr.setMaxAge(0);
                                    clr.setPath("/");
                                    response.addCookie(clr);
                                    response.sendRedirect("/login?disabled=true");
                                    return false;
                                }
                                session.setAttribute("ACCOUNT_ID", acc.getId());
                                session.setAttribute("ACCOUNT_ROLE", acc.getRole());
                                session.setAttribute("ACCOUNT_NAME", acc.getFullName());
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        // 2. Auto-restore Student session from persistent cookie if missing
        if (session.getAttribute("loggedInStudent") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("REMEMBER_STUDENT_ID".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Long studentId = Long.parseLong(c.getValue());
                            Optional<Student> studentOpt = studentRepository.findById(studentId);
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

        // Public certificate routes (preview template, verification, and APIs)
        if (path.startsWith("/certificate") || path.startsWith("/api/certificate")) {
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

        // Other paths require any logged in account
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            response.sendRedirect("/login");
            return false;
        }

        // Verify account is still active (not disabled by admin)
        try {
            Optional<Account> currentAccOpt = accountRepository.findById(accountId);
            if (currentAccOpt.isPresent() && Boolean.FALSE.equals(currentAccOpt.get().getActive())) {
                session.invalidate();
                Cookie clr = new Cookie("REMEMBER_ACCOUNT_ID", "");
                clr.setMaxAge(0);
                clr.setPath("/");
                response.addCookie(clr);
                response.sendRedirect("/login?disabled=true");
                return false;
            }
        } catch (Exception ignored) {}
        
        return true;
    }
}
