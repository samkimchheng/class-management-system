package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(StudentRepository studentRepository, 
                                      AccountRepository accountRepository,
                                      ClassroomRepository classroomRepository) {
        return args -> {
            // 1. Admin Account
            Account admin;
            if (accountRepository.findByUsername("admin").isEmpty()) {
                admin = accountRepository.save(new Account("admin", "admin123", "ADMIN", "Administrator"));
            } else {
                admin = accountRepository.findByUsername("admin").orElse(null);
            }

            // 2. Teacher Accounts
            Account teacher1;
            if (accountRepository.findByUsername("Teacher1001").isEmpty()) {
                teacher1 = accountRepository.save(new Account("Teacher1001", "@techer1001", "TEACHER", "ស៊ឹង សុមុនី"));
            } else {
                teacher1 = accountRepository.findByUsername("Teacher1001").orElse(null);
            }

            if (accountRepository.findByUsername("samkimchheng").isEmpty()) {
                accountRepository.save(new Account("samkimchheng", "@techer1002", "TEACHER", "សំ គឹមឆេង"));
            }

            if (accountRepository.findByUsername("t1").isEmpty()) {
                accountRepository.save(new Account("t1", "123", "TEACHER", "Test Teacher"));
            }

            // 3. Classrooms
            if (teacher1 != null && !classroomRepository.existsByClassNameAndAccountId("10E", teacher1.getId())) {
                Classroom c = new Classroom();
                c.setClassName("10E");
                c.setAccountId(teacher1.getId());
                classroomRepository.save(c);
            }

            // 4. Initial Students
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
