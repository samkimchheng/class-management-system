package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.ExternalLearner;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.TeacherActivityLog;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ExternalLearnerRepository;
import com.nubb.classroom.repository.HomeroomClassAdminRepository;
import com.nubb.classroom.repository.StudentRepository;
import com.nubb.classroom.repository.TeacherActivityLogRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private ExternalLearnerRepository externalLearnerRepository;

    @Autowired(required = false)
    private HomeroomClassAdminRepository homeroomClassAdminRepository;

    @Autowired(required = false)
    private TeacherActivityLogRepository teacherActivityLogRepository;

    @Autowired
    private TokenSecurityService tokenSecurityService;

    private static final ZoneId CAMBODIA_ZONE = ZoneId.of("Asia/Phnom_Penh");

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
        if (session.getAttribute("ACCOUNT_ID") == null && session.getAttribute("loggedInStudent") == null) {
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
                                boolean canAccessHomeroom = Boolean.TRUE.equals(acc.getHomeroomPermission())
                                        || "ADMIN".equalsIgnoreCase(acc.getRole());
                                session.setAttribute("HOMEROOM_PERMISSION", canAccessHomeroom);
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

        // 2. Auto-restore Student or External Learner session ONLY when no Teacher/Admin is logged in
        if (session.getAttribute("loggedInStudent") == null && session.getAttribute("ACCOUNT_ID") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("REMEMBER_STUDENT_ID".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Optional<Student> studentOpt = tokenSecurityService.validateStudentToken(c.getValue());
                            if (studentOpt.isPresent()) {
                                session.setAttribute("loggedInStudent", studentOpt.get());
                                session.removeAttribute("IS_EXTERNAL_LEARNER");
                                session.removeAttribute("loggedInExternalLearner");
                            }
                        } catch (Exception ignored) {}
                        break;
                    } else if ("REMEMBER_EXTERNAL_LEARNER".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                        try {
                            Optional<ExternalLearner> extOpt = tokenSecurityService.validateExternalLearnerToken(c.getValue());
                            if (extOpt.isPresent()) {
                                ExternalLearner ext = extOpt.get();
                                session.setAttribute("IS_EXTERNAL_LEARNER", true);
                                session.setAttribute("loggedInExternalLearner", ext);
                                session.setAttribute("EXT_ALLOW_PHONICS", !Boolean.FALSE.equals(ext.getAllowPhonics()));
                                session.setAttribute("EXT_ALLOW_VOCAB", !Boolean.FALSE.equals(ext.getAllowVocabulary()));
                                session.setAttribute("loggedInStudent", ext.toVirtualStudent());
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        // 2b. If an External Learner is logged in (and not a Teacher/Admin), keep status synced and restrict ONLY to Phonics & Vocabulary
        if (Boolean.TRUE.equals(session.getAttribute("IS_EXTERNAL_LEARNER")) && session.getAttribute("ACCOUNT_ID") == null) {
            ExternalLearner sessionExt = (ExternalLearner) session.getAttribute("loggedInExternalLearner");
            ExternalLearner freshExt = null;
            if (sessionExt != null && sessionExt.getId() != null && externalLearnerRepository != null) {
                freshExt = externalLearnerRepository.findById(sessionExt.getId()).orElse(null);
            }
            if (freshExt == null || Boolean.FALSE.equals(freshExt.getActive())
                    || (Boolean.FALSE.equals(freshExt.getAllowPhonics()) && Boolean.FALSE.equals(freshExt.getAllowVocabulary()))) {
                session.invalidate();
                ResponseCookie clr = ResponseCookie.from("REMEMBER_EXTERNAL_LEARNER", "")
                        .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build();
                response.addHeader(HttpHeaders.SET_COOKIE, clr.toString());
                response.sendRedirect("/test?disabled=true");
                return false;
            }
            boolean allowPhonics = !Boolean.FALSE.equals(freshExt.getAllowPhonics());
            boolean allowVocab = !Boolean.FALSE.equals(freshExt.getAllowVocabulary());
            session.setAttribute("loggedInExternalLearner", freshExt);
            session.setAttribute("EXT_ALLOW_PHONICS", allowPhonics);
            session.setAttribute("EXT_ALLOW_VOCAB", allowVocab);
            session.setAttribute("loggedInStudent", freshExt.toVirtualStudent());

            String defaultTarget = allowPhonics ? "/test/phonics" : "/test/vocabulary";

            if (path.equals("/test") || path.equals("/test/login") || path.equals("/test/logout") || path.equals("/logout") || path.startsWith("/api/tts/")) {
                return true;
            }
            if (allowPhonics && (path.startsWith("/test/phonics") || path.startsWith("/api/student-portal/phonics"))) {
                session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
                return true;
            }
            if (allowVocab && (path.startsWith("/test/vocabulary") || path.startsWith("/api/student-portal/vocabulary"))) {
                session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
                return true;
            }

            // Block External Learner from accessing exams (/test/take), lessons, leave requests, certificates, etc.
            response.sendRedirect(defaultTarget);
            return false;
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

        // If a student is logged in (and not an authenticated Teacher/Admin), strictly enforce student boundary
        if (session.getAttribute("loggedInStudent") != null && session.getAttribute("ACCOUNT_ID") == null) {
            session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
            try {
                Student curStu = (Student) session.getAttribute("loggedInStudent");
                boolean isMon = com.nubb.classroom.controller.HomeroomController
                        .resolveAuthorizedMonitorAdmin(curStu, studentRepository, homeroomClassAdminRepository) != null;
                session.setAttribute("IS_CLASS_MONITOR", isMon);
            } catch (Exception ignored) {}
            // If student visits root "/", redirect to their student portal /test/take
            if ("/".equals(path)) {
                response.sendRedirect("/test/take");
                return false;
            }
            // Allow ONLY student-accessible endpoints
            boolean isAllowedStudentRoute = path.startsWith("/test")
                    || path.startsWith("/api/student-portal")
                    || path.startsWith("/lessons")
                    || path.startsWith("/api/lessons/progress")
                    || path.startsWith("/api/lessons/") && path.endsWith("/transcripts")
                    || path.equals("/certificate")
                    || path.startsWith("/certificate/view")
                    || path.startsWith("/certificate/verify")
                    || path.startsWith("/api/certificate/verify")
                    || path.startsWith("/api/tts")
                    || path.equals("/logout")
                    || path.equals("/test/logout")
                    || path.startsWith("/css")
                    || path.startsWith("/js")
                    || path.startsWith("/images")
                    || path.startsWith("/uploads")
                    || path.startsWith("/docs");
            if (!isAllowedStudentRoute) {
                if (path.startsWith("/api/")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    return false;
                }
                // If student tries to access admin or teacher dashboard pages (/students, /attendance, /scores, etc.), redirect to student portal
                response.sendRedirect("/test/take");
                return false;
            }
        }

        // Admin paths require ADMIN role (or an active Admin currently inspecting a teacher account)
        if (path.startsWith("/admin")) {
            String role = (String) session.getAttribute("ACCOUNT_ROLE");
            Long impAdminId = (Long) session.getAttribute("IMPERSONATOR_ADMIN_ID");

            // Lightweight status endpoint used by the Admin Inspector banner on teacher pages
            if ("/admin/api/impersonation-status".equals(path)) {
                return true;
            }

            // Allow impersonation switch / exit when caller is ADMIN or currently impersonating as ADMIN
            if (path.equals("/admin/impersonate/exit") || path.startsWith("/admin/accounts/impersonate/")) {
                if ("ADMIN".equals(role) || impAdminId != null) {
                    return true;
                }
                response.sendRedirect("/login");
                return false;
            }

            if ("ADMIN".equals(role)) {
                return true;
            }

            // If Admin navigates directly back to /admin/** while inspecting a teacher, restore Admin session cleanly
            if (impAdminId != null) {
                Optional<Account> adminOpt = accountRepository.findById(impAdminId);
                if (adminOpt.isPresent() && "ADMIN".equalsIgnoreCase(adminOpt.get().getRole())
                        && !Boolean.FALSE.equals(adminOpt.get().getActive())) {
                    Account adminAcc = adminOpt.get();
                    session.setAttribute("ACCOUNT_ID", adminAcc.getId());
                    session.setAttribute("ACCOUNT_ROLE", "ADMIN");
                    session.setAttribute("ACCOUNT_NAME", adminAcc.getFullName());
                    session.setAttribute("ACCOUNT_USERNAME", adminAcc.getUsername());
                    session.setAttribute("VOCABULARY_PERMISSION", true);
                    session.setAttribute("HOMEROOM_PERMISSION", true);
                    if (adminAcc.getCurrentAcademicYear() != null && !adminAcc.getCurrentAcademicYear().trim().isEmpty()) {
                        session.setAttribute("CURRENT_ACADEMIC_YEAR", adminAcc.getCurrentAcademicYear().trim());
                    } else {
                        session.setAttribute("CURRENT_ACADEMIC_YEAR", "២០២៦-២០២៧");
                    }
                    session.removeAttribute("IMPERSONATOR_ADMIN_ID");
                    session.removeAttribute("IMPERSONATOR_ADMIN_NAME");
                    session.removeAttribute("IMPERSONATOR_ADMIN_USERNAME");
                    session.removeAttribute("IMPERSONATOR_RETURN_URL");
                    session.removeAttribute("LAST_SELECTED_CLASS");
                    session.removeAttribute("STUDENT_SORT_MODE");
                    ResponseCookie clrInspect = ResponseCookie.from("CMS_ADMIN_INSPECT", "")
                            .httpOnly(false)
                            .path("/")
                            .maxAge(0)
                            .sameSite("Lax")
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, clrInspect.toString());
                    return true;
                }
            }

            response.sendRedirect("/login");
            return false;
        }

        // Allow student portal API and test routes if loggedInStudent is present (or teacher previewing vocabulary/phonics)
        if (path.startsWith("/test") || path.startsWith("/api/student-portal")) {
            boolean isTeacherPreview = session.getAttribute("ACCOUNT_ID") != null
                    && (path.startsWith("/test/vocabulary") || path.startsWith("/test/phonics")
                        || path.startsWith("/api/student-portal/phonics") || path.startsWith("/api/student-portal/vocabulary"));
            if (session.getAttribute("loggedInStudent") == null && !isTeacherPreview && !path.equals("/test/login") && !path.equals("/test")) {
                response.sendRedirect("/test");
                return false;
            }
            if (session.getAttribute("loggedInStudent") != null) {
                session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
            } else if (session.getAttribute("ACCOUNT_ID") != null && session.getAttribute("IMPERSONATOR_ADMIN_ID") == null) {
                String r = (String) session.getAttribute("ACCOUNT_ROLE");
                if (r == null || !"ADMIN".equalsIgnoreCase(r)) {
                    recordTeacherActivity((Long) session.getAttribute("ACCOUNT_ID"), path);
                }
            }
            return true;
        }

        // Allow English female voice TTS audio endpoint for both students and teachers
        if (path.startsWith("/api/tts/")) {
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
            if (session.getAttribute("loggedInStudent") != null) {
                session.setAttribute("ACTIVE_USER_MODE", "STUDENT");
            } else if (session.getAttribute("ACCOUNT_ID") != null && session.getAttribute("IMPERSONATOR_ADMIN_ID") == null) {
                String r = (String) session.getAttribute("ACCOUNT_ROLE");
                if (r == null || !"ADMIN".equalsIgnoreCase(r)) {
                    recordTeacherActivity((Long) session.getAttribute("ACCOUNT_ID"), path);
                }
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
        if (!path.startsWith("/certificate") && !path.startsWith("/api/certificate") && !path.startsWith("/lessons") && !path.startsWith("/api/lessons")) {
            if (session.getAttribute("loggedInStudent") == null) {
                session.setAttribute("ACTIVE_USER_MODE", "TEACHER");
            }
        }

        // Verify account is still active and keep VOCABULARY_PERMISSION synced with DB
        try {
            Long impAdminId = (Long) session.getAttribute("IMPERSONATOR_ADMIN_ID");
            boolean isAdminImpersonating = impAdminId != null;
            if (isAdminImpersonating) {
                Optional<Account> adminCheck = accountRepository.findById(impAdminId);
                if (adminCheck.isEmpty() || !"ADMIN".equalsIgnoreCase(adminCheck.get().getRole())
                        || Boolean.FALSE.equals(adminCheck.get().getActive())) {
                    session.invalidate();
                    ResponseCookie clrInspect = ResponseCookie.from("CMS_ADMIN_INSPECT", "")
                            .httpOnly(false).path("/").maxAge(0).sameSite("Lax").build();
                    response.addHeader(HttpHeaders.SET_COOKIE, clrInspect.toString());
                    response.sendRedirect("/login");
                    return false;
                }
            }

            Optional<Account> currentAccOpt = accountRepository.findById(accountId);
            if (currentAccOpt.isPresent()) {
                Account curr = currentAccOpt.get();
                if (!isAdminImpersonating && Boolean.FALSE.equals(curr.getActive())) {
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
                boolean canAccessHomeroom = Boolean.TRUE.equals(curr.getHomeroomPermission())
                        || "ADMIN".equalsIgnoreCase(curr.getRole());
                session.setAttribute("HOMEROOM_PERMISSION", canAccessHomeroom);

                if (path.equals("/homeroom") || path.startsWith("/homeroom/") || path.startsWith("/api/homeroom")) {
                    if (!canAccessHomeroom && !isAdminImpersonating) {
                        if (path.startsWith("/api/")) {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Homeroom permission required");
                        } else {
                            response.sendRedirect("/?homeroomDenied=true");
                        }
                        return false;
                    }
                }
                if (!isAdminImpersonating && !"ADMIN".equalsIgnoreCase(curr.getRole())) {
                    recordTeacherActivity(curr.getId(), path);
                }
            }
        } catch (Exception ignored) {}

        return true;
    }

    private void recordTeacherActivity(Long accountId, String path) {
        if (teacherActivityLogRepository == null || accountId == null || path == null) return;
        try {
            String[] feature = resolveTeacherFeature(path);
            if (feature == null) return;
            String featureKey = feature[0];
            String featureLabel = feature[1];
            LocalDateTime now = LocalDateTime.now(CAMBODIA_ZONE);
            LocalDate today = now.toLocalDate();

            synchronized (("TAL_" + accountId + "_" + featureKey).intern()) {
                Optional<TeacherActivityLog> existingOpt =
                        teacherActivityLogRepository.findByAccountIdAndActivityDateAndFeatureKey(accountId, today, featureKey);
                if (existingOpt.isPresent()) {
                    TeacherActivityLog log = existingOpt.get();
                    log.setHitCount((log.getHitCount() == null ? 0 : log.getHitCount()) + 1);
                    log.setLastUsedAt(now);
                    log.setLastPath(path.length() > 250 ? path.substring(0, 250) : path);
                    teacherActivityLogRepository.save(log);
                } else {
                    TeacherActivityLog log = new TeacherActivityLog(
                            accountId, today, featureKey, featureLabel, now,
                            path.length() > 250 ? path.substring(0, 250) : path
                    );
                    teacherActivityLogRepository.save(log);
                }
            }
        } catch (Exception ignored) {}
    }

    private String[] resolveTeacherFeature(String path) {
        if (path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/images")
                || path.startsWith("/icons") || path.startsWith("/uploads") || path.startsWith("/docs")
                || path.startsWith("/api/tts") || path.startsWith("/api/diagnostic")
                || path.equals("/logout") || path.equals("/login") || path.equals("/manifest.json")
                || path.equals("/sw.js") || path.equals("/favicon.ico")) {
            return null;
        }
        if ("/".equals(path)) return new String[]{"DASHBOARD", "🏠 ទំព័រដើម (Dashboard)"};
        if (path.startsWith("/api/homeroom/scores")) return new String[]{"HOMEROOM_SCORES", "👑 បញ្ចូលពិន្ទុគ្រូបន្ទុក"};
        if (path.startsWith("/api/homeroom/admin")) return new String[]{"HOMEROOM_ADMIN", "👑 វត្តមានតាមខែ & រដ្ឋបាលថ្នាក់"};
        if (path.startsWith("/api/homeroom/student-extra")) return new String[]{"HOMEROOM_CARDS", "👑 ប្រវត្តិសិស្ស & កាតសិស្ស"};
        if (path.equals("/homeroom") || path.startsWith("/homeroom/") || path.startsWith("/api/homeroom")) {
            return new String[]{"HOMEROOM_MAIN", "👑 ប្រព័ន្ធគ្រូបន្ទុកថ្នាក់"};
        }
        if (path.startsWith("/classrooms") || path.startsWith("/api/classrooms")) return new String[]{"CLASSROOMS", "🏫 បញ្ជីថ្នាក់រៀន"};
        if (path.startsWith("/students") || path.startsWith("/api/students")) return new String[]{"STUDENTS", "👥 គ្រប់គ្រងសិស្ស"};
        if (path.startsWith("/attendance") || path.startsWith("/api/attendance")) return new String[]{"ATTENDANCE", "📅 ស្រង់វត្តមាន"};
        if (path.startsWith("/scores") || path.startsWith("/api/scores")) return new String[]{"SCORES", "📊 ពិន្ទុសិស្ស"};
        if (path.startsWith("/schedule") || path.startsWith("/api/schedules")) return new String[]{"SCHEDULE", "🗓️ កាលវិភាគបង្រៀន"};
        if (path.startsWith("/daily-plan") || path.startsWith("/api/daily-plan")) return new String[]{"DAILY_PLAN", "📝 ផែនការប្រចាំថ្ងៃ"};
        if (path.startsWith("/exams") || path.startsWith("/api/exams")) return new String[]{"EXAMS", "✍️ ប្រឡងអនឡាញ"};
        if (path.startsWith("/lessons") || path.startsWith("/api/lessons")) return new String[]{"LESSONS", "📖 មេរៀនអនឡាញ"};
        if (path.startsWith("/certificate") || path.startsWith("/api/certificate")) return new String[]{"CERTIFICATE", "🎓 ប័ណ្ណសរសើរ"};
        if (path.startsWith("/vocabulary") || path.startsWith("/api/vocabulary")) return new String[]{"VOCABULARY", "🔤 វាក្យសព្ទអង់គ្លេស"};
        if (path.startsWith("/phonics") || path.startsWith("/api/phonics")) return new String[]{"PHONICS", "🔊 ការអានសំឡេង (Phonics)"};
        if (path.startsWith("/feedback")) return new String[]{"FEEDBACK", "💬 មតិកែលម្អ"};
        return null;
    }
}
