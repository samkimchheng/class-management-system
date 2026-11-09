package com.nubb.classroom.config;

import com.nubb.classroom.model.CertificateRecord;
import com.nubb.classroom.model.Attendance;
import com.nubb.classroom.model.Score;
import com.nubb.classroom.model.ScoreRule;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.Exam;
import com.nubb.classroom.model.Question;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.AttendanceRepository;
import com.nubb.classroom.repository.CertificateRecordRepository;
import com.nubb.classroom.repository.ExamRepository;
import com.nubb.classroom.repository.LessonRepository;
import com.nubb.classroom.repository.QuestionRepository;
import com.nubb.classroom.repository.ScoreRepository;
import com.nubb.classroom.repository.ScoreRuleRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    private void applySchemaMigrations(Statement stmt) {
        try { stmt.execute("ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS \"ACTIVE\" BOOLEAN DEFAULT TRUE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE account SET active = TRUE WHERE active IS NULL"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ALTER COLUMN class_name SET DATA TYPE VARCHAR(1000)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS reading_passage TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"READING_PASSAGE\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS word_bank TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"WORD_BANK\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section1title VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section1instruction VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section2title VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section2instruction VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section3title VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section3instruction VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section4title VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section4instruction VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS writing_prompt TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS writing_max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION4TITLE\" VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION4INSTRUCTION\" VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"WRITING_PROMPT\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"WRITING_MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS student_writing TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"STUDENT_WRITING\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS writing_score DOUBLE PRECISION"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"WRITING_SCORE\" DOUBLE PRECISION"); } catch (Exception ignored) {}

        String createCertTable = "CREATE TABLE IF NOT EXISTS \"PUBLIC\".\"CERTIFICATE_RECORDS\"(" +
                "\"ID\" BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, " +
                "\"STUDENT_ID\" BIGINT NOT NULL, " +
                "\"CLASS_NAME\" VARCHAR(100), " +
                "\"SERIAL_NUMBER\" VARCHAR(255) UNIQUE, " +
                "\"SCHOOL_NAME\" VARCHAR(500), " +
                "\"SUBJECT_NAME\" VARCHAR(255), " +
                "\"PROVINCE\" VARCHAR(255), " +
                "\"TEACHER_NAME\" VARCHAR(255), " +
                "\"ROLE_SIGNER_LEFT\" VARCHAR(255), " +
                "\"ROLE_SIGNER_RIGHT\" VARCHAR(255), " +
                "\"ISSUE_DATE_KHMER\" VARCHAR(255), " +
                "\"ISSUE_DATE_EN\" VARCHAR(255), " +
                "\"UPDATED_AT\" TIMESTAMP)";
        try { stmt.execute(createCertTable); } catch (Exception ignored) {}

        String createFeedbackTable = "CREATE TABLE IF NOT EXISTS \"PUBLIC\".\"FEEDBACKS\"(" +
                "\"ID\" BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, " +
                "\"TEACHER_ACCOUNT_ID\" BIGINT, " +
                "\"TEACHER_NAME\" VARCHAR(255), " +
                "\"TEACHER_USERNAME\" VARCHAR(255), " +
                "\"SCHOOL_NAME\" VARCHAR(500), " +
                "\"PROVINCE\" VARCHAR(255), " +
                "\"PHONE\" VARCHAR(255), " +
                "\"CATEGORY\" VARCHAR(255), " +
                "\"PRIORITY\" VARCHAR(100), " +
                "\"TITLE\" VARCHAR(500), " +
                "\"CONTENT\" TEXT, " +
                "\"STATUS\" VARCHAR(50) DEFAULT 'PENDING', " +
                "\"ADMIN_REPLY\" TEXT, " +
                "\"ADMIN_REPLY_BY\" VARCHAR(255), " +
                "\"REPLIED_AT\" TIMESTAMP, " +
                "\"CREATED_AT\" TIMESTAMP, " +
                "\"UPDATED_AT\" TIMESTAMP)";
        try { stmt.execute(createFeedbackTable); } catch (Exception ignored) {}
        try { stmt.execute("CREATE TABLE IF NOT EXISTS feedbacks (id BIGINT AUTO_INCREMENT PRIMARY KEY, teacher_account_id BIGINT, teacher_name VARCHAR(255), teacher_username VARCHAR(255), school_name VARCHAR(500), province VARCHAR(255), phone VARCHAR(255), category VARCHAR(255), priority VARCHAR(100), title VARCHAR(500), content TEXT, status VARCHAR(50), admin_reply TEXT, admin_reply_by VARCHAR(255), replied_at TIMESTAMP, created_at TIMESTAMP, updated_at TIMESTAMP)"); } catch (Exception ignored) {}

        String[] profileCols = {
            "GENDER VARCHAR(255)",
            "SCHOOL_NAME VARCHAR(255)",
            "SUBJECT VARCHAR(255)",
            "PROVINCE VARCHAR(255)",
            "PHONE VARCHAR(255)",
            "TELEGRAM VARCHAR(255)",
            "EMAIL VARCHAR(255)",
            "POSITION VARCHAR(255)",
            "TEACHING_CLASSES VARCHAR(255)",
            "BIO VARCHAR(1000)",
            "SIGNATURE TEXT",
            "PRINCIPAL_SIGNATURE TEXT",
            "SCHOOL_STAMP TEXT",
            "PRINCIPAL_NAME VARCHAR(255)",
            "CURRENT_ACADEMIC_YEAR VARCHAR(50) DEFAULT '២០២៦-២០២៧'"
        };
        for (String col : profileCols) {
            try { stmt.execute("ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS " + col); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS " + col); } catch (Exception ignored) {}
        }

        String[] certCols = {
            "SIGNATURE TEXT",
            "PRINCIPAL_SIGNATURE TEXT",
            "SCHOOL_STAMP TEXT",
            "PRINCIPAL_NAME VARCHAR(255)",
            "ACADEMIC_YEAR VARCHAR(50) DEFAULT '២០២៦-២០២៧'"
        };
        for (String col : certCols) {
            try { stmt.execute("ALTER TABLE IF EXISTS \"CERTIFICATE_RECORDS\" ADD COLUMN IF NOT EXISTS " + col); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE IF EXISTS certificate_records ADD COLUMN IF NOT EXISTS " + col); } catch (Exception ignored) {}
        }

        try { stmt.execute("ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS absence_type VARCHAR(50)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"ATTENDANCE\" ADD COLUMN IF NOT EXISTS \"ABSENCE_TYPE\" VARCHAR(50)"); } catch (Exception ignored) {}

        String[] yearCols = {
            "ALTER TABLE IF EXISTS students ADD COLUMN IF NOT EXISTS academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"STUDENTS\" ADD COLUMN IF NOT EXISTS \"ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS classroom ADD COLUMN IF NOT EXISTS academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"CLASSROOM\" ADD COLUMN IF NOT EXISTS \"ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS certificate_records ADD COLUMN IF NOT EXISTS academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"CERTIFICATE_RECORDS\" ADD COLUMN IF NOT EXISTS \"ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS current_academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS \"CURRENT_ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "UPDATE students SET academic_year = '២០២៦-២០២៧' WHERE academic_year IS NULL OR academic_year = ''",
            "UPDATE \"STUDENTS\" SET \"ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"ACADEMIC_YEAR\" IS NULL OR \"ACADEMIC_YEAR\" = ''",
            "UPDATE classroom SET academic_year = '២០២៦-២០២៧' WHERE academic_year IS NULL OR academic_year = ''",
            "UPDATE \"CLASSROOM\" SET \"ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"ACADEMIC_YEAR\" IS NULL OR \"ACADEMIC_YEAR\" = ''",
            "UPDATE score SET academic_year = '២០២៦-២០២៧' WHERE academic_year IS NULL OR academic_year = ''",
            "UPDATE \"SCORE\" SET \"ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"ACADEMIC_YEAR\" IS NULL OR \"ACADEMIC_YEAR\" = ''",
            "UPDATE certificate_records SET academic_year = '២០២៦-២០២៧' WHERE academic_year IS NULL OR academic_year = ''",
            "UPDATE \"CERTIFICATE_RECORDS\" SET \"ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"ACADEMIC_YEAR\" IS NULL OR \"ACADEMIC_YEAR\" = ''",
            "UPDATE account SET current_academic_year = '២០២៦-២០២៧' WHERE current_academic_year IS NULL OR current_academic_year = ''",
            "UPDATE \"ACCOUNT\" SET \"CURRENT_ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"CURRENT_ACADEMIC_YEAR\" IS NULL OR \"CURRENT_ACADEMIC_YEAR\" = ''",
            "UPDATE attendance SET absence_type = 'UNEXCUSED' WHERE absence_type IS NULL AND present = FALSE",
            "UPDATE \"ATTENDANCE\" SET \"ABSENCE_TYPE\" = 'UNEXCUSED' WHERE \"ABSENCE_TYPE\" IS NULL AND \"PRESENT\" = FALSE",
            "UPDATE account SET principal_name = 'លី ប៊ុនថេង' WHERE principal_name IS NULL OR principal_name = '' OR principal_name LIKE '%គឹម ហ៊ាង%'",
            "UPDATE \"ACCOUNT\" SET PRINCIPAL_NAME = 'លី ប៊ុនថេង' WHERE PRINCIPAL_NAME IS NULL OR PRINCIPAL_NAME = '' OR PRINCIPAL_NAME LIKE '%គឹម ហ៊ាង%'",
            "UPDATE certificate_records SET principal_name = 'លី ប៊ុនថេង' WHERE principal_name IS NULL OR principal_name = '' OR principal_name LIKE '%គឹម ហ៊ាង%'",
            "UPDATE \"CERTIFICATE_RECORDS\" SET PRINCIPAL_NAME = 'លី ប៊ុនថេង' WHERE PRINCIPAL_NAME IS NULL OR PRINCIPAL_NAME = '' OR PRINCIPAL_NAME LIKE '%គឹម ហ៊ាង%'",
            "UPDATE account SET province = 'ខេត្តពោធិ៍សាត់' WHERE province IS NULL OR province = ''",
            "UPDATE \"ACCOUNT\" SET PROVINCE = 'ខេត្តពោធិ៍សាត់' WHERE PROVINCE IS NULL OR PROVINCE = ''"
        };
        for (String q : yearCols) {
            try { stmt.execute(q); } catch (Exception ignored) {}
        }

        try {
            String activeSchool = null;
            try (ResultSet rs = stmt.executeQuery("SELECT school_name FROM account WHERE school_name IS NOT NULL AND school_name != '' AND school_name NOT LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' AND school_name NOT LIKE '%គ្រឹះស្ថានសិក្សា%' LIMIT 1")) {
                if (rs.next()) activeSchool = rs.getString(1);
            } catch (Exception ignored) {}
            if (activeSchool == null) {
                try (ResultSet rs = stmt.executeQuery("SELECT SCHOOL_NAME FROM \"ACCOUNT\" WHERE SCHOOL_NAME IS NOT NULL AND SCHOOL_NAME != '' AND SCHOOL_NAME NOT LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' AND SCHOOL_NAME NOT LIKE '%គ្រឹះស្ថានសិក្សា%' LIMIT 1")) {
                    if (rs.next()) activeSchool = rs.getString(1);
                } catch (Exception ignored) {}
            }
            if (activeSchool != null && !activeSchool.trim().isEmpty()) {
                String clean = activeSchool.trim().replace("'", "''");
                stmt.executeUpdate("UPDATE certificate_records SET school_name = '" + clean + "' WHERE school_name IS NULL OR school_name = '' OR school_name LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' OR school_name LIKE '%វិទ្យាល័យ/អនុវិទ្យាល័យ%' OR school_name LIKE '%គ្រឹះស្ថានសិក្សា%'");
                stmt.executeUpdate("UPDATE \"CERTIFICATE_RECORDS\" SET SCHOOL_NAME = '" + clean + "' WHERE SCHOOL_NAME IS NULL OR SCHOOL_NAME = '' OR SCHOOL_NAME LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' OR SCHOOL_NAME LIKE '%វិទ្យាល័យ/អនុវិទ្យាល័យ%' OR SCHOOL_NAME LIKE '%គ្រឹះស្ថានសិក្សា%'");
            }
        } catch (Exception ignored) {}
    }

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository,
                                     StudentRepository studentRepository,
                                     ScoreRepository scoreRepository,
                                     ScoreRuleRepository scoreRuleRepository,
                                     CertificateRecordRepository certificateRecordRepository,
                                     AttendanceRepository attendanceRepository,
                                     LessonRepository lessonRepository,
                                     ExamRepository examRepository,
                                     QuestionRepository questionRepository,
                                     DataSource dataSource) {
        return args -> {
            // 1. Schema migration: Ensure active and profile columns exist in account table
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                applySchemaMigrations(stmt);
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Schema migration notice: " + e.getMessage());
            }

            // 2. Detect if database is PostgreSQL or H2
            boolean isPostgres = false;
            try (Connection conn = dataSource.getConnection()) {
                String pName = conn.getMetaData().getDatabaseProductName();
                if (pName != null && pName.toLowerCase().contains("postgres")) {
                    isPostgres = true;
                }
            } catch (Exception ignored) {}

            boolean needRestore = false;
            if (!isPostgres) {
                // Local H2 mode: check if samkimchheng exists, if not restore from data_dump.sql
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM account WHERE username = 'samkimchheng'")) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            needRestore = true;
                        }
                    } catch (Exception ignored) {
                        try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM \"ACCOUNT\" WHERE username = 'samkimchheng'")) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                needRestore = true;
                            }
                        } catch (Exception ignored2) {
                            needRestore = true;
                        }
                    }
                } catch (Exception e) {
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

                        // Crucial: Re-apply schema migrations immediately after restore!
                        try (Connection conn = dataSource.getConnection();
                             Statement stmt = conn.createStatement()) {
                            applySchemaMigrations(stmt);
                        } catch (Exception e) {
                            System.err.println(">>> [DataInitializer] Error applying migrations after restore: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        System.err.println(">>> [DataInitializer] Error restoring database: " + e.getMessage());
                    }
                }
            } else {
                // PostgreSQL mode: If fresh database, seed official accounts and update serial sequences
                System.out.println(">>> [DataInitializer] Running on PostgreSQL Managed Database. Skipping H2 DROP ALL OBJECTS.");
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    // Seed Admin (ID: 1) if not present
                    stmt.executeUpdate("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, principal_name, current_academic_year) " +
                            "VALUES (1, 'admin', 'admin@manulife%27', 'admincl@mitpukae.com', 'ADMIN', 'អ្នកគ្រប់គ្រងប្រព័ន្ធ', true, 'ប្រុស', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'អ្នកគ្រប់គ្រង', 'លី ប៊ុនថេង', '២០២៦-២០២៧') " +
                            "ON CONFLICT (id) DO NOTHING");

                    // Seed Sam Kimchheng (ID: 97) if not present
                    stmt.executeUpdate("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, teaching_classes, telegram, principal_name, current_academic_year) " +
                            "VALUES (97, 'samkimchheng', 'techercl@%27', 'chhengcl@mitpukae.com', 'TEACHER', 'សំ គឹមឆេង', true, 'ប្រុស', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'គ្រូទទួលបន្ទុកថ្នាក់', '១០E, ១០F, ១០G, ១១F', '@Kimchheng12', 'លី ប៊ុនថេង', '២០២៦-២០២៧') " +
                            "ON CONFLICT (id) DO NOTHING");

                    // Seed Seung Somuny (ID: 33) if not present
                    stmt.executeUpdate("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, principal_name, current_academic_year) " +
                            "VALUES (33, 'Teacher1001', 'techercl@%21', 'teachercl21@mitpukae.com', 'TEACHER', 'ស៊ឹង សុមុនី', true, 'ស្រី', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'គ្រូបង្រៀន', 'លី ប៊ុនថេង', '២០២៦-២០២៧') " +
                            "ON CONFLICT (id) DO NOTHING");

                    // Seed Demo Teacher (ID: 98) if not present
                    stmt.executeUpdate("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, teaching_classes, principal_name, current_academic_year) " +
                            "VALUES (98, 'demo', 'demo@1234', 'demo@mitpukae.com', 'TEACHER', 'គ្រូបង្រៀន (គណនីសាកល្បង)', true, 'ប្រុស', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'គ្រូបង្រៀន (សាកល្បង)', 'ថ្នាក់សាកល្បង', 'លី ប៊ុនថេង', '២០២៦-២០២៧') " +
                            "ON CONFLICT (id) DO NOTHING");

                    // Align sequence so newly registered teacher accounts get clean auto-increment IDs without conflict
                    try {
                        stmt.execute("SELECT setval(pg_get_serial_sequence('account', 'id'), (SELECT COALESCE(MAX(id), 1) FROM account))");
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    System.err.println(">>> [DataInitializer] Notice seeding PostgreSQL accounts: " + e.getMessage());
                }
            }

            // 2c. Clean up legacy pre-seeded sample lessons (IDs 33, 65, 97, 98, 99) that the teacher deleted
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] oldLessonCleanups = {
                    "DELETE FROM lesson_progress WHERE lesson_id IN (SELECT id FROM lessons WHERE id IN (33, 65, 97, 98, 99) AND (title LIKE 'Unit1%' OR title LIKE 'Unit2%' OR title LIKE 'Unit3%' OR title LIKE 'Unit4%' OR title LIKE 'Unit5%'))",
                    "DELETE FROM \"LESSON_PROGRESS\" WHERE \"LESSON_ID\" IN (SELECT \"ID\" FROM \"LESSONS\" WHERE \"ID\" IN (33, 65, 97, 98, 99) AND (\"TITLE\" LIKE 'Unit1%' OR \"TITLE\" LIKE 'Unit2%' OR \"TITLE\" LIKE 'Unit3%' OR \"TITLE\" LIKE 'Unit4%' OR \"TITLE\" LIKE 'Unit5%'))",
                    "DELETE FROM lesson_transcripts WHERE lesson_id IN (SELECT id FROM lessons WHERE id IN (33, 65, 97, 98, 99) AND (title LIKE 'Unit1%' OR title LIKE 'Unit2%' OR title LIKE 'Unit3%' OR title LIKE 'Unit4%' OR title LIKE 'Unit5%'))",
                    "DELETE FROM \"LESSON_TRANSCRIPTS\" WHERE \"LESSON_ID\" IN (SELECT \"ID\" FROM \"LESSONS\" WHERE \"ID\" IN (33, 65, 97, 98, 99) AND (\"TITLE\" LIKE 'Unit1%' OR \"TITLE\" LIKE 'Unit2%' OR \"TITLE\" LIKE 'Unit3%' OR \"TITLE\" LIKE 'Unit4%' OR \"TITLE\" LIKE 'Unit5%'))",
                    "DELETE FROM lessons WHERE id IN (33, 65, 97, 98, 99) AND (title LIKE 'Unit1%' OR title LIKE 'Unit2%' OR title LIKE 'Unit3%' OR title LIKE 'Unit4%' OR title LIKE 'Unit5%')",
                    "DELETE FROM \"LESSONS\" WHERE \"ID\" IN (33, 65, 97, 98, 99) AND (\"TITLE\" LIKE 'Unit1%' OR \"TITLE\" LIKE 'Unit2%' OR \"TITLE\" LIKE 'Unit3%' OR \"TITLE\" LIKE 'Unit4%' OR \"TITLE\" LIKE 'Unit5%')"
                };
                for (String q : oldLessonCleanups) {
                    try { stmt.executeUpdate(q); } catch (Exception ignored) {}
                }
                if (isPostgres) {
                    try { stmt.execute("SELECT setval(pg_get_serial_sequence('lessons', 'id'), GREATEST((SELECT COALESCE(MAX(id), 100) FROM lessons), 100))"); } catch (Exception ignored) {}
                    try { stmt.execute("SELECT setval(pg_get_serial_sequence('lesson_transcripts', 'id'), GREATEST((SELECT COALESCE(MAX(id), 100) FROM lesson_transcripts), 100))"); } catch (Exception ignored) {}
                    try { stmt.execute("SELECT setval(pg_get_serial_sequence('lesson_progress', 'id'), GREATEST((SELECT COALESCE(MAX(id), 100) FROM lesson_progress), 100))"); } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 2d. Ensure default schedule exists for Sam Kimchheng if empty
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                boolean hasSched = false;
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM schedule WHERE account_id = 97 AND subject_name != '-' AND subject_name IS NOT NULL")) {
                    if (rs.next() && rs.getInt(1) > 0) hasSched = true;
                } catch (Exception ignored) {}

                if (!hasSched) {
                    System.out.println(">>> [DataInitializer] Seeding default weekly schedule for samkimchheng...");
                    String[] subjects = {
                        "10F", "11F", "10E", "11G", "-", "-",
                        "10F", "11F", "10E", "11G", "-", "-",
                        "10G", "11G", "10F", "11F", "-", "-",
                        "10G", "11G", "10F", "11F", "-", "-",
                        "-", "-", "-", "-", "-", "-",
                        "10E", "-", "-", "-", "10G", "-",
                        "10E", "-", "-", "-", "10G", "-",
                        "-", "-", "-", "-", "-", "-"
                    };
                    for (int i = 0; i < subjects.length; i++) {
                        stmt.executeUpdate("INSERT INTO schedule (account_id, block_index, class_name, subject_name) VALUES (97, " + i + ", 'WEEKLY', '" + subjects[i] + "')");
                    }
                }
            } catch (Exception ignored) {}

            // 3. Keep lessons class names as assigned by the teacher
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Do not force overwrite class names to allow teacher customizations
            } catch (Exception ignored) {}

            // 4. Update any default 'Administrator' or generic names to real teacher 'សំ គឹមឆេង'
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE \"ACCOUNT\" SET full_name = 'សំ គឹមឆេង' WHERE full_name = 'Administrator' OR (username = 'admin' AND (full_name IS NULL OR full_name = '' OR full_name = 'Administrator'))");
                stmt.executeUpdate("UPDATE account SET full_name = 'សំ គឹមឆេង' WHERE full_name = 'Administrator' OR (username = 'admin' AND (full_name IS NULL OR full_name = '' OR full_name = 'Administrator'))");
                stmt.executeUpdate("UPDATE \"CERTIFICATE_RECORDS\" SET teacher_name = 'សំ គឹមឆេង' WHERE teacher_name LIKE '%Administrator%' OR teacher_name LIKE '%admin%'");
                stmt.executeUpdate("UPDATE certificate_records SET teacher_name = 'សំ គឹមឆេង' WHERE teacher_name LIKE '%Administrator%' OR teacher_name LIKE '%admin%'");
                stmt.executeUpdate("UPDATE \"CERTIFICATE_RECORDS\" SET teacher_name = TRIM(REPLACE(REPLACE(teacher_name, 'លោកគ្រូ', ''), 'អ្នកគ្រូ', '')) WHERE teacher_name LIKE '%លោកគ្រូ%' OR teacher_name LIKE '%អ្នកគ្រូ%'");
                stmt.executeUpdate("UPDATE certificate_records SET teacher_name = TRIM(REPLACE(REPLACE(teacher_name, 'លោកគ្រូ', ''), 'អ្នកគ្រូ', '')) WHERE teacher_name LIKE '%លោកគ្រូ%' OR teacher_name LIKE '%អ្នកគ្រូ%'");
                stmt.executeUpdate("UPDATE account SET province = 'ខេត្តពោធិ៍សាត់' WHERE province IS NULL OR province = ''");
                stmt.executeUpdate("UPDATE \"ACCOUNT\" SET PROVINCE = 'ខេត្តពោធិ៍សាត់' WHERE PROVINCE IS NULL OR PROVINCE = ''");
                try {
                    String activeSchool = null;
                    try (ResultSet rs = stmt.executeQuery("SELECT school_name FROM account WHERE school_name IS NOT NULL AND school_name != '' AND school_name NOT LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' AND school_name NOT LIKE '%គ្រឹះស្ថានសិក្សា%' LIMIT 1")) {
                        if (rs.next()) activeSchool = rs.getString(1);
                    } catch (Exception ignored) {}
                    if (activeSchool == null) {
                        try (ResultSet rs = stmt.executeQuery("SELECT SCHOOL_NAME FROM \"ACCOUNT\" WHERE SCHOOL_NAME IS NOT NULL AND SCHOOL_NAME != '' AND SCHOOL_NAME NOT LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' AND SCHOOL_NAME NOT LIKE '%គ្រឹះស្ថានសិក្សា%' LIMIT 1")) {
                            if (rs.next()) activeSchool = rs.getString(1);
                        } catch (Exception ignored) {}
                    }
                    if (activeSchool != null && !activeSchool.trim().isEmpty()) {
                        String clean = activeSchool.trim().replace("'", "''");
                        stmt.executeUpdate("UPDATE certificate_records SET school_name = '" + clean + "' WHERE school_name IS NULL OR school_name = '' OR school_name LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' OR school_name LIKE '%វិទ្យាល័យ/អនុវិទ្យាល័យ%' OR school_name LIKE '%គ្រឹះស្ថានសិក្សា%'");
                        stmt.executeUpdate("UPDATE \"CERTIFICATE_RECORDS\" SET SCHOOL_NAME = '" + clean + "' WHERE SCHOOL_NAME IS NULL OR SCHOOL_NAME = '' OR SCHOOL_NAME LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%' OR SCHOOL_NAME LIKE '%វិទ្យាល័យ/អនុវិទ្យាល័យ%' OR SCHOOL_NAME LIKE '%គ្រឹះស្ថានសិក្សា%'");
                    }
                } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            // 5. Ensure any existing students or classrooms are assigned to account 97 (samkimchheng) & 1
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE \"STUDENTS\" SET account_id = 97 WHERE account_id IS NULL OR account_id = 1");
                stmt.executeUpdate("UPDATE students SET account_id = 97 WHERE account_id IS NULL OR account_id = 1");
                stmt.executeUpdate("UPDATE \"CLASSROOM\" SET account_id = 97 WHERE account_id IS NULL OR account_id = 1");
                stmt.executeUpdate("UPDATE classroom SET account_id = 97 WHERE account_id IS NULL OR account_id = 1");
            } catch (Exception ignored) {}

            // 6. Ensure default classrooms (១០E, ១០F, ១០G, ១១F) exist
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] classes = {"១០E", "១០F", "១០G", "១១F"};
                for (String cls : classes) {
                    try {
                        stmt.execute("INSERT INTO classroom (account_id, class_name) " +
                                "SELECT 97, '" + cls + "' " +
                                "WHERE NOT EXISTS (SELECT 1 FROM classroom WHERE class_name = '" + cls + "')");
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 7. Permanently delete all 10 demo sample students and their scores, attendances, and certificates
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] demoCodes = {"'1003'", "'1004'", "'1005'", "'1006'", "'1007'", "'123456'", "'654321'", "'111111'", "'222222'", "'333333'"};
                String codesIn = String.join(",", demoCodes);
                String[] demoNames = {"'តានិន'", "'សុខ ពិសី'", "'ហេង វ៉ាន់ដា'", "'ចាន់ ស្រីនីន'", "'ម៉ៅ វិបុល'", "'សុខ សាន្ត'", "'ចាន់ ធីតា'", "'មាស សុវណ្ណ'", "'កែវ និមល'", "'រិទ្ធី ពិសិដ្ឋ'"};
                String namesIn = String.join(",", demoNames);

                String[] studentTables = {"students", "\"STUDENTS\""};
                for (String stTable : studentTables) {
                    try {
                        String stSub = "SELECT id FROM " + stTable + " WHERE account_id != 98 AND (student_code IN (" + codesIn + ") OR name IN (" + namesIn + "))";
                        try { stmt.executeUpdate("DELETE FROM score WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}
                        try { stmt.executeUpdate("DELETE FROM \"SCORE\" WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}

                        try { stmt.executeUpdate("DELETE FROM attendance WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}
                        try { stmt.executeUpdate("DELETE FROM \"ATTENDANCE\" WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}

                        try { stmt.executeUpdate("DELETE FROM certificate_records WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}
                        try { stmt.executeUpdate("DELETE FROM \"CERTIFICATE_RECORDS\" WHERE student_id IN (" + stSub + ")"); } catch (Exception ignored) {}

                        stmt.executeUpdate("DELETE FROM " + stTable + " WHERE account_id != 98 AND (student_code IN (" + codesIn + ") OR name IN (" + namesIn + "))");
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 7c. Clean up orphan scores, leftover test score, test classroom and test score rules for account 33
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] cleanupQueries = {
                    "DELETE FROM score WHERE student_id NOT IN (SELECT id FROM students)",
                    "DELETE FROM \"SCORE\" WHERE student_id NOT IN (SELECT id FROM \"STUDENTS\")",
                    "DELETE FROM score WHERE account_id = 33 AND (class_name = '7A' OR student_id = 6)",
                    "DELETE FROM \"SCORE\" WHERE account_id = 33 AND (class_name = '7A' OR student_id = 6)",
                    "DELETE FROM classroom WHERE account_id = 33 AND class_name = '10E'",
                    "DELETE FROM \"CLASSROOM\" WHERE account_id = 33 AND class_name = '10E'",
                    "DELETE FROM score_rule WHERE account_id = 33",
                    "DELETE FROM \"SCORE_RULE\" WHERE account_id = 33"
                };
                for (String q : cleanupQueries) {
                    try { stmt.executeUpdate(q); } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 7b. Clean up telegram and teaching classes for other teachers' accounts so they don't inherit Sam Kimchheng's info
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE account SET telegram = NULL WHERE username NOT IN ('samkimchheng', 'admin') AND (telegram = '@Kimchheng12' OR telegram LIKE '%Kimchheng12%')");
                try { stmt.executeUpdate("UPDATE \"ACCOUNT\" SET telegram = NULL WHERE username NOT IN ('samkimchheng', 'admin') AND (telegram = '@Kimchheng12' OR telegram LIKE '%Kimchheng12%')"); } catch (Exception ignored) {}

                stmt.executeUpdate("UPDATE account SET teaching_classes = NULL WHERE username NOT IN ('samkimchheng', 'admin') AND teaching_classes = '១០E, ១០F, ១០G, ១១F'");
                try { stmt.executeUpdate("UPDATE \"ACCOUNT\" SET teaching_classes = NULL WHERE username NOT IN ('samkimchheng', 'admin') AND teaching_classes = '១០E, ១០F, ១០G, ១១F'"); } catch (Exception ignored) {}
            } catch (Exception ignored) {}

            // 8. Ensure Teacher Profile for 'សំ គឹមឆេង' is fully initialized on both account 97 and account 1
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] profileUpdates = {
                    "UPDATE account SET full_name = 'សំ គឹមឆេង' WHERE username = 'samkimchheng'",
                    "UPDATE account SET full_name = 'អ្នកគ្រប់គ្រងប្រព័ន្ធ' WHERE username = 'admin'",
                    "DELETE FROM account WHERE username = 't1' OR full_name = 'Test Teacher'",
                    "DELETE FROM \"ACCOUNT\" WHERE username = 't1' OR full_name = 'Test Teacher'",
                    "UPDATE account SET gender = 'ប្រុស' WHERE (gender IS NULL OR gender = '') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET school_name = 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត' WHERE (school_name IS NULL OR school_name = '' OR school_name LIKE '%វិទ្យាល័យ / អនុវិទ្យាល័យ%') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET subject = 'គ្រប់មុខវិជ្ជា' WHERE (subject IS NULL OR subject = '') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET province = 'ខេត្តពោធិ៍សាត់' WHERE (province IS NULL OR province = '') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET position = 'គ្រូទទួលបន្ទុកថ្នាក់' WHERE (position IS NULL OR position = '') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET teaching_classes = '១០E, ១០F, ១០G, ១១F' WHERE (teaching_classes IS NULL OR teaching_classes = '') AND username IN ('samkimchheng', 'admin')",
                    "UPDATE account SET telegram = '@Kimchheng12' WHERE (telegram IS NULL OR telegram = '') AND username IN ('samkimchheng', 'admin')"
                };
                for (String sql : profileUpdates) {
                    try { stmt.executeUpdate(sql); } catch (Exception ignored) {}
                    try { stmt.executeUpdate(sql.replace("account", "\"ACCOUNT\"")); } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 8b. Ensure default teacher signature is loaded and set on account 97 & 1 and certificate records
            try {
                String defaultSig = null;
                try {
                    org.springframework.core.io.Resource sigRes = new org.springframework.core.io.ClassPathResource("signature_default.txt");
                    if (sigRes.exists()) {
                        defaultSig = new String(sigRes.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
                    }
                } catch (Exception ignored) {}

                if (defaultSig != null && !defaultSig.isEmpty()) {
                    final String sigToSet = defaultSig;
                    accountRepository.findById(97L).ifPresent(a -> {
                        if (a.getSignature() == null || a.getSignature().isEmpty()) {
                            a.setSignature(sigToSet);
                            accountRepository.save(a);
                        }
                    });
                    accountRepository.findById(1L).ifPresent(a -> {
                        if (a.getSignature() == null || a.getSignature().isEmpty()) {
                            a.setSignature(sigToSet);
                            accountRepository.save(a);
                        }
                    });
                    accountRepository.findByUsername("samkimchheng").ifPresent(a -> {
                        if (a.getSignature() == null || a.getSignature().isEmpty()) {
                            a.setSignature(sigToSet);
                            accountRepository.save(a);
                        }
                    });

                    try {
                        List<CertificateRecord> cList = certificateRecordRepository.findAll();
                        for (CertificateRecord cr : cList) {
                            if (cr.getSignature() == null || cr.getSignature().isEmpty()) {
                                cr.setSignature(sigToSet);
                                certificateRecordRepository.save(cr);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // 8c. Ensure official passwords and emails requested by user are permanently synchronized
            try {
                // 1. Admin (admincl@mitpukae.com / admin@manulife%27)
                accountRepository.findByUsername("admin").ifPresent(a -> {
                    a.setPassword("admin@manulife%27");
                    a.setEmail("admincl@mitpukae.com");
                    accountRepository.save(a);
                });
                accountRepository.findById(1L).ifPresent(a -> {
                    a.setPassword("admin@manulife%27");
                    a.setEmail("admincl@mitpukae.com");
                    accountRepository.save(a);
                });

                // 2. Sam Kimchheng (chhengcl@mitpukae.com / techercl@%27)
                accountRepository.findByUsername("samkimchheng").ifPresent(a -> {
                    a.setPassword("techercl@%27");
                    a.setEmail("chhengcl@mitpukae.com");
                    accountRepository.save(a);
                });
                accountRepository.findById(97L).ifPresent(a -> {
                    a.setPassword("techercl@%27");
                    a.setEmail("chhengcl@mitpukae.com");
                    accountRepository.save(a);
                });

                // 3. Seung Somuny (teachercl21@mitpukae.com / techercl@%21)
                accountRepository.findByUsername("Teacher1001").ifPresent(a -> {
                    a.setPassword("techercl@%21");
                    a.setEmail("teachercl21@mitpukae.com");
                    accountRepository.save(a);
                });
                accountRepository.findById(33L).ifPresent(a -> {
                    a.setPassword("techercl@%21");
                    a.setEmail("teachercl21@mitpukae.com");
                    accountRepository.save(a);
                });
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice syncing official credentials: " + e.getMessage());
            }

            // 9. Ensure default Score Rules exist for subjects (Max 60 / 20 / 20 = 100)
            String[] standardSubjects = {"គណិតវិទ្យា", "ភាសាខ្មែរ", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា", "ប្រវត្តិវិទ្យា", "ភូមិវិទ្យា", "ភាសាអង់គ្លេស"};
            for (String subj : standardSubjects) {
                if (scoreRuleRepository.findByClassNameAndSubjectName("ថ្នាក់រៀនទាំងអស់", subj).isEmpty()) {
                    ScoreRule rule = new ScoreRule();
                    rule.setAccountId(97L);
                    rule.setClassName("ថ្នាក់រៀនទាំងអស់");
                    rule.setSubjectName(subj);
                    rule.setMaxMonthlyScore(60);
                    rule.setMaxAttendanceScore(20);
                    rule.setMaxAssignmentScore(20);
                    scoreRuleRepository.save(rule);
                }
            }

            // 10. Ensure existing absences have a default absence type
            try {
                List<Attendance> allAtt = attendanceRepository.findAll();
                for (Attendance att : allAtt) {
                    if (att.getAbsenceType() == null || att.getAbsenceType().isEmpty()) {
                        int day = att.getDate().getDayOfMonth();
                        att.setAbsenceType((day % 2 == 0) ? "PERMISSION" : "UNEXCUSED");
                        attendanceRepository.save(att);
                    }
                }
            } catch (Exception ignored) {}

            // 11. Ensure official students for 10E and 10F are seeded if missing
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                boolean needsStudents = false;
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM students WHERE class_name IN ('១០E', '១០F')")) {
                    if (rs.next() && rs.getInt(1) < 10) {
                        needsStudents = true;
                    }
                } catch (Exception ignored) {
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM \"STUDENTS\" WHERE \"CLASS_NAME\" IN ('១០E', '១០F')")) {
                        if (rs.next() && rs.getInt(1) < 10) {
                            needsStudents = true;
                        }
                    } catch (Exception ignored2) {
                        needsStudents = true;
                    }
                }

                if (needsStudents) {
                    System.out.println(">>> [DataInitializer] Seeding official students for 10E and 10F from students_insert.sql...");
                    ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
                    pop.addScript(new ClassPathResource("students_insert.sql"));
                    pop.setContinueOnError(true);
                    pop.execute(dataSource);
                    System.out.println(">>> [DataInitializer] Official students for 10E and 10F successfully seeded!");

                    if (isPostgres) {
                        try (Connection connPost = dataSource.getConnection();
                             Statement stmtPost = connPost.createStatement()) {
                            stmtPost.execute("SELECT setval(pg_get_serial_sequence('students', 'id'), (SELECT COALESCE(MAX(id), 1) FROM students))");
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice seeding students: " + e.getMessage());
            }

            // 12. Ensure Demo Teacher Account (ID: 98) and sample data exist
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Ensure account 98 exists
                stmt.executeUpdate("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, teaching_classes, principal_name, current_academic_year) " +
                        "VALUES (98, 'demo', 'demo@1234', 'demo@mitpukae.com', 'TEACHER', 'គ្រូបង្រៀន (គណនីសាកល្បង)', true, 'ប្រុស', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'គ្រូបង្រៀន (សាកល្បង)', 'ថ្នាក់សាកល្បង', 'លី ប៊ុនថេង', '២០២៦-២០២៧') " +
                        "ON CONFLICT (id) DO NOTHING");

                // Ensure classroom exists
                stmt.execute("INSERT INTO classroom (account_id, class_name, academic_year) " +
                        "SELECT 98, 'ថ្នាក់សាកល្បង', '២០២៦-២០២៧' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM classroom WHERE account_id = 98 AND class_name = 'ថ្នាក់សាកល្បង')");

                // Ensure demo students exist
                String[][] demoStudents = {
                    {"DM-01", "ចាន់ ធីតា", "ស្រី", "10មករា"},
                    {"DM-02", "សុខ សាន្ត", "ប្រុស", "ពុទ្ទ្រាម"},
                    {"DM-03", "មាស សុវណ្ណ", "ប្រុស", "ថ្គោលអណ្តែត"},
                    {"DM-04", "កែវ និមល", "ស្រី", "ឈើទាល"},
                    {"DM-05", "រិទ្ធី ពិសិដ្ឋ", "ប្រុស", "10មករា"}
                };
                for (String[] st : demoStudents) {
                    stmt.execute("INSERT INTO students (account_id, student_code, name, gender, class_name, academic_year, village, province, status) " +
                            "SELECT 98, '" + st[0] + "', '" + st[1] + "', '" + st[2] + "', 'ថ្នាក់សាកល្បង', '២០២៦-២០២៧', '" + st[3] + "', 'ខេត្តពោធិ៍សាត់', 'កំពុងសិក្សា' " +
                            "WHERE NOT EXISTS (SELECT 1 FROM students WHERE account_id = 98 AND student_code = '" + st[0] + "')");
                }

                // Ensure demo score rules exist
                String[] demoSubjects = {"អក្សរសាស្ត្រខ្មែរ", "គណិតវិទ្យា", "រូបវិទ្យា", "គីមីវិទ្យា", "ជីវវិទ្យា"};
                for (String subj : demoSubjects) {
                    stmt.execute("INSERT INTO score_rule (account_id, class_name, subject_name, max_monthly_score, max_attendance_score, max_assignment_score) " +
                            "SELECT 98, 'ថ្នាក់សាកល្បង', '" + subj + "', 60, 20, 20 " +
                            "WHERE NOT EXISTS (SELECT 1 FROM score_rule WHERE account_id = 98 AND class_name = 'ថ្នាក់សាកល្បង' AND subject_name = '" + subj + "')");
                }
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice initializing demo account: " + e.getMessage());
            }

            // 13. Ensure Official Grade 11 Semester 2 English Exam exists
            seedEnglishExamIfMissing(examRepository, questionRepository);
        };
    }

    private void seedEnglishExamIfMissing(ExamRepository examRepository, QuestionRepository questionRepository) {
        try {
            Long[] accountIds = {97L, 1L, 98L};
            String[] months = {"ឆមាសទី២", "ខែវិច្ឆិកា ២០២៦", "តេស្តដើមឆ្នាំ"};
            String subject = "ភាសាអង់គ្លេស";
            String passage = "I hope you're 1..................and that your wedding ceremony in Phnom Penh went well. I'm sorry I haven't written to you since May, but I just haven't 2.................. find the time, because of my end of year exams. They're our final exams and I did an 3.................. amount of work for them, but now they're finished and you'll be 4.................. to know that I passed them all. As well as exams, I've been very busy playing football and other sports for school over the past months. I'm sure you understand.\n" +
                    "Thanks for your letter, which took only ten days to arrive here. I really enjoyed 5.................. it. It was lovely to read all the news from Cambodia, 6.................. the bits about Dany's wedding in Samrong and your own plans to get married. I also received a letter from Kim Seng in which he 7.................. Dany's and your wedding. I'm sending a 8.................. to him with my letter to you. I hope you don't mind sending it on to him. In addition, could you please give a message to Pisey? I feel really 9.................., because I haven't been able to 10.................. her letter yet.";
            String wordBank = "enormous, reading, been able to, acknowledge, mentioned, delighted, particularly, keeping well, ashamed, postcard";

            for (Long accId : accountIds) {
                for (String month : months) {
                    List<Exam> existingList = examRepository.findByMonthAndSubjectNameAndAccountId(month, subject, accId);
                    Exam exam;
                    if (existingList.isEmpty() || existingList.get(0).getReadingPassage() == null || existingList.get(0).getReadingPassage().trim().isEmpty() || existingList.get(0).getWritingPrompt() == null) {
                        if (existingList.isEmpty()) {
                            exam = new Exam();
                            exam.setAccountId(accId);
                            exam.setMonth(month);
                            exam.setSubjectName(subject);
                            exam.setClassName("ថ្នាក់រៀនទាំងអស់, ថ្នាក់ទី ១១, ១១F, ១០A, ១០B, ១០C, ១០D, ១០E, ១០F, ថ្នាក់សាកល្បង");
                        } else {
                            exam = existingList.get(0);
                        }
                        exam.setReadingPassage(passage);
                        exam.setWordBank(wordBank);
                        exam.setSection1Title("I. GAPS FILLING:");
                        exam.setSection1Instruction("Read the text and fill in each gap with the word in the box given: (20pts)");
                        exam.setSection2Title("II. GRAMMAR SECTION:");
                        exam.setSection2Instruction("Choose the best and correct only one answer A, B, C or D as the following: (15pts)");
                        exam.setSection3Title("III. VOCABULARY SECTION:");
                        exam.setSection3Instruction("Choose the best and correct only one answer A, B, C or D as the following: (15pts)");
                        exam.setSection4Title("IV. WRITING SECTION:");
                        exam.setSection4Instruction("Write a short composition (80-120 words) on the topic given below: (15pts)");
                        exam.setWritingPrompt("Write a letter to your friend telling them about your school life, your favorite subjects, and your plans after completing high school.");
                        exam.setWritingMaxPoints(15);
                        exam = examRepository.save(exam);

                        // Clear and populate questions
                        List<Question> existingQ = questionRepository.findByExamIdAndAccountId(exam.getId(), accId);
                        if (existingQ.isEmpty() || existingQ.stream().noneMatch(q -> "gaps_filling".equalsIgnoreCase(q.getSkillType()))) {
                            questionRepository.deleteAll(existingQ);

                            // 10 Gaps filling questions
                            String[] gapAnswers = {
                                "keeping well", "been able to", "enormous", "delighted", "reading",
                                "particularly", "mentioned", "postcard", "ashamed", "acknowledge"
                            };
                            String[] gapPrompts = {
                                "Gap 1: I hope you're [1]...",
                                "Gap 2: but I just haven't [2] find the time...",
                                "Gap 3: I did an [3] amount of work for them...",
                                "Gap 4: you'll be [4] to know that I passed them all...",
                                "Gap 5: I really enjoyed [5] it...",
                                "Gap 6: lovely to read all the news from Cambodia, [6] the bits about Dany's wedding...",
                                "Gap 7: a letter from Kim Seng in which he [7] Dany's and your wedding...",
                                "Gap 8: I'm sending a [8] to him with my letter to you...",
                                "Gap 9: I feel really [9]...",
                                "Gap 10: because I haven't been able to [10] her letter yet."
                            };
                            for (int i = 0; i < 10; i++) {
                                Question q = new Question();
                                q.setExamId(exam.getId());
                                q.setAccountId(accId);
                                q.setSkillType("gaps_filling");
                                q.setQuestionText(gapPrompts[i]);
                                q.setCorrectOption(gapAnswers[i]);
                                questionRepository.save(q);
                            }

                            // 5 Grammar questions
                            String[][] grammarQuestions = {
                                {"1. Kim Seng... Monica yet.", "hasn't phoned", "didn't", "have phoned", "phoned", "A"},
                                {"2. Thy .... a letter to his sister.", "wrote", "has just written", "had written", "writes", "B"},
                                {"3. Thy and Kim Seng.... football very well for school.", "played", "play", "have just played", "has just played", "B"},
                                {"4. Monica......the strange scandal to Thy yet.", "didn't tell", "told", "has told", "hasn't told", "D"},
                                {"5. Thy.... the news from Monica about her wedding day.", "has already received", "hasn't already received", "have already received", "has received", "A"}
                            };
                            for (String[] g : grammarQuestions) {
                                Question q = new Question();
                                q.setExamId(exam.getId());
                                q.setAccountId(accId);
                                q.setSkillType("grammar");
                                q.setQuestionText(g[0]);
                                q.setOptionA(g[1]);
                                q.setOptionB(g[2]);
                                q.setOptionC(g[3]);
                                q.setOptionD(g[4]);
                                q.setCorrectOption(g[5]);
                                questionRepository.save(q);
                            }

                            // 5 Vocabulary questions
                            String[][] vocabQuestions = {
                                {"1. Thy hasn't written to his sister before, because of the...", "time", "end of year", "exams", "past months", "C"},
                                {"2. Monica will be delighted, because Thy has...his exams.", "Worked hard for", "just finished", "been busy with", "passed", "D"},
                                {"3. Thy has also been busy at school with...", "Final exams", "playing football and sports", "wedding in Phnom Penh", "end of year", "B"},
                                {"4. Monica's letter took... to arrive to Thy.", "ten days", "since May", "final exams", "months", "A"},
                                {"5. Thy feels ashamed because he hasn't...", "written to Monica", "sent Kim Seng a postcard", "acknowledged Pisey's letter yet", "played sports", "C"}
                            };
                            for (String[] v : vocabQuestions) {
                                Question q = new Question();
                                q.setExamId(exam.getId());
                                q.setAccountId(accId);
                                q.setSkillType("vocabulary");
                                q.setQuestionText(v[0]);
                                q.setOptionA(v[1]);
                                q.setOptionB(v[2]);
                                q.setOptionC(v[3]);
                                q.setOptionD(v[4]);
                                q.setCorrectOption(v[5]);
                                questionRepository.save(q);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding English exam: " + e.getMessage());
        }
    }
}
