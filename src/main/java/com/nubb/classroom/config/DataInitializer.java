package com.nubb.classroom.config;

import com.nubb.classroom.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository, DataSource dataSource) {
        return args -> {
            if (accountRepository.findByUsername("samkimchheng").isEmpty()) {
                System.out.println(">>> [DataInitializer] Restoring full database for samkimchheng and all teachers...");
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("DROP ALL OBJECTS");
                } catch (Exception e) {
                    System.err.println(">>> [DataInitializer] Notice dropping objects: " + e.getMessage());
                }

                try {
                    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                    populator.addScript(new ClassPathResource("data_dump.sql"));
                    populator.setContinueOnError(true);
                    populator.setIgnoreFailedDrops(true);
                    populator.execute(dataSource);
                    System.out.println(">>> [DataInitializer] Full database successfully restored!");
                } catch (Exception e) {
                    System.err.println(">>> [DataInitializer] Error restoring database: " + e.getMessage());
                }
            }

            // Ensure all lessons are set to គ្រប់ថ្នាក់ so all classes can access them
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE lessons SET class_name = 'គ្រប់ថ្នាក់' WHERE class_name = '១០A' OR class_name = '10A'");
            } catch (Exception ignored) {}
        };
    }
}
