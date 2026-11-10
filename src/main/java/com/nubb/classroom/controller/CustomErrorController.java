package com.nubb.classroom.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object messageObj = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (Exception ignored) {}
        }

        String message = (messageObj != null && !messageObj.toString().isEmpty())
                ? messageObj.toString()
                : (statusCode == 404 ? "រកមិនឃើញទំព័រដែលបានស្នើសុំ" : "មានបញ្ហាបច្ចេកទេសក្នុងដំណើរការប្រព័ន្ធ");

        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (throwable == null) {
            throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        }
        if (throwable == null) {
            throwable = (Throwable) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION");
        }
        String detailedEx = throwable != null ? throwable.toString() : (exceptionObj != null ? exceptionObj.toString() : "No exception object");
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
            detailedEx += " | Caused by: " + current.toString();
        }

        java.io.StringWriter sw = new java.io.StringWriter();
        if (throwable != null) {
            throwable.printStackTrace(new java.io.PrintWriter(sw));
        }

        System.err.println(">>> [CustomErrorController] Handled error status " + statusCode +
                " on URI: " + requestUri + " | Message: " + message +
                " | Exception: " + detailedEx);

        // If JSON/API request, return JSON response
        String acceptHeader = request.getHeader("Accept");
        if (requestUri != null && requestUri.startsWith("/api/") || (acceptHeader != null && acceptHeader.contains("application/json")) || "true".equalsIgnoreCase(request.getParameter("json"))) {
            Map<String, Object> errBody = new LinkedHashMap<>();
            errBody.put("status", statusCode);
            errBody.put("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
            errBody.put("message", message);
            errBody.put("path", requestUri);
            errBody.put("exception", detailedEx);
            errBody.put("stackTrace", sw.toString());
            return ResponseEntity.status(statusCode).body(errBody);
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());
        model.addAttribute("message", message);
        model.addAttribute("requestUri", requestUri);
        model.addAttribute("detailedEx", detailedEx);

        return "error";
    }
}
