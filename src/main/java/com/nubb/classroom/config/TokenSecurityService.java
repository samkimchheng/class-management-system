package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
public class TokenSecurityService {

    // High-entropy application secret key for HMAC SHA-256 signing
    private static final String SECRET_KEY = "NUBB_CLASSROOM_2027_SUPER_SECURE_HMAC_SECRET_KEY_PROD_#9982!";
    private static final String HMAC_ALGO = "HmacSHA256";

    private final AccountRepository accountRepository;
    private final StudentRepository studentRepository;

    public TokenSecurityService(AccountRepository accountRepository, StudentRepository studentRepository) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Generates a tamper-proof cryptographic token for a Teacher/Admin account.
     * Token format: accountId.expiryTimestamp.hmacSignature
     */
    public String generateAccountToken(Account account) {
        long expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L); // 30 days
        String payload = account.getId() + ":" + expiry + ":" + account.getUsername() + ":" + account.getPassword();
        String signature = sign(payload);
        return account.getId() + "." + expiry + "." + signature;
    }

    /**
     * Validates a Teacher/Admin account token and returns the Account if authentic and unexpired.
     */
    public Optional<Account> validateAccountToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            Long accountId = Long.parseLong(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            String receivedSignature = parts[2];

            // Check if expired
            if (System.currentTimeMillis() > expiry) {
                return Optional.empty();
            }

            Optional<Account> accOpt = accountRepository.findById(accountId);
            if (accOpt.isEmpty()) {
                return Optional.empty();
            }

            Account account = accOpt.get();
            String payload = account.getId() + ":" + expiry + ":" + account.getUsername() + ":" + account.getPassword();
            String expectedSignature = sign(payload);

            // Constant-time comparison against timing attacks
            if (MessageDigest.isEqual(
                    receivedSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.of(account);
            }
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Generates a tamper-proof cryptographic token for a Student account.
     */
    public String generateStudentToken(Student student) {
        long expiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L); // 30 days
        String payload = "STUDENT:" + student.getId() + ":" + expiry + ":" + student.getStudentCode();
        String signature = sign(payload);
        return student.getId() + "." + expiry + "." + signature;
    }

    /**
     * Validates a Student token and returns the Student if authentic and unexpired.
     */
    public Optional<Student> validateStudentToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            Long studentId = Long.parseLong(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            String receivedSignature = parts[2];

            if (System.currentTimeMillis() > expiry) {
                return Optional.empty();
            }

            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                return Optional.empty();
            }

            Student student = studentOpt.get();
            String payload = "STUDENT:" + student.getId() + ":" + expiry + ":" + student.getStudentCode();
            String expectedSignature = sign(payload);

            if (MessageDigest.isEqual(
                    receivedSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.of(student);
            }
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.empty();
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error signing security token", e);
        }
    }
}
