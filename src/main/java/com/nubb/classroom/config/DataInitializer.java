package com.nubb.classroom.config;

import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.AccountRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(StudentRepository studentRepository, AccountRepository accountRepository) {
        return args -> {
            Account admin = null;
            if (accountRepository.count() == 0) {
                admin = accountRepository.save(new Account("admin", "admin123", "ADMIN", "Administrator"));
            } else {
                admin = accountRepository.findByUsername("admin").orElse(null);
            }

            if (studentRepository.count() == 0 && admin != null) {
                Long adminId = admin.getId();
                Student s1 = new Student("សុខ សាន្ត", "ប្រុស", "ថ្នាក់ទី១០ A", "123456"); s1.setAccountId(adminId);
                Student s2 = new Student("ចាន់ ធីតា", "ស្រី", "ថ្នាក់ទី១០ A", "654321"); s2.setAccountId(adminId);
                Student s3 = new Student("មាស សុវណ្ណ", "ប្រុស", "ថ្នាក់ទី១០ B", "111111"); s3.setAccountId(adminId);
                Student s4 = new Student("កែវ និមល", "ស្រី", "ថ្នាក់ទី១០ C", "222222"); s4.setAccountId(adminId);
                Student s5 = new Student("រិទ្ធី ពិសិដ្ឋ", "ប្រុស", "ថ្នាក់ទី១០ D", "333333"); s5.setAccountId(adminId);
                
                studentRepository.saveAll(List.of(s1, s2, s3, s4, s5));
            }
        };
    }
}
