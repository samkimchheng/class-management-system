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
            // 1. Schema migration: Ensure active column exists in account table
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS \"ACTIVE\" BOOLEAN DEFAULT TRUE");
                stmt.execute("ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE");
                stmt.execute("UPDATE account SET active = TRUE WHERE active IS NULL");
                stmt.execute("ALTER TABLE IF EXISTS exam ALTER COLUMN class_name SET DATA TYPE VARCHAR(1000)");
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Schema migration notice: " + e.getMessage());
            }

            // 2. Check if samkimchheng exists, if not restore from data_dump.sql
            boolean needRestore = false;
            try {
                needRestore = accountRepository.findByUsername("samkimchheng").isEmpty();
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Query failed, will trigger restore: " + e.getMessage());
                needRestore = true;
            }

            if (needRestore) {
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

            // 3. Ensure all lessons are set to គ្រប់ថ្នាក់ so all classes can access them
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE lessons SET class_name = 'គ្រប់ថ្នាក់' WHERE class_name = '១០A' OR class_name = '10A'");
            } catch (Exception ignored) {}
        };
    }
}
