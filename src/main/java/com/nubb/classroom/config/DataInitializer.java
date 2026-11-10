package com.nubb.classroom.config;

import com.nubb.classroom.model.Account;
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
import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.model.DailyTeachingLog;
import com.nubb.classroom.repository.ClassroomRepository;
import com.nubb.classroom.repository.DailyTeachingLogRepository;
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
import java.util.Map;
import java.util.LinkedHashMap;

@Configuration
public class DataInitializer {

    private void applySchemaMigrations(Statement stmt) {
        try { stmt.execute("ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS \"ACTIVE\" BOOLEAN DEFAULT TRUE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE account SET active = TRUE WHERE active IS NULL"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS account ADD COLUMN IF NOT EXISTS vocabulary_permission BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"ACCOUNT\" ADD COLUMN IF NOT EXISTS \"VOCABULARY_PERMISSION\" BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE account SET vocabulary_permission = TRUE WHERE username IN ('samkimchheng', 'admin') OR id IN (1, 97)"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"ACCOUNT\" SET \"VOCABULARY_PERMISSION\" = TRUE WHERE \"USERNAME\" IN ('samkimchheng', 'admin') OR \"ID\" IN (1, 97)"); } catch (Exception ignored) {}
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
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section1max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section2max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section3max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section1_max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section2_max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS section3_max_points INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION4TITLE\" VARCHAR(255)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION4INSTRUCTION\" VARCHAR(500)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"WRITING_PROMPT\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"WRITING_MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION1MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION2MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION3MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION1_MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION2_MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"SECTION3_MAX_POINTS\" INTEGER"); } catch (Exception ignored) {}
        try { stmt.execute("DELETE FROM question WHERE exam_id IN (SELECT id FROM exam WHERE account_id = 1)"); } catch (Exception ignored) {}
        try { stmt.execute("DELETE FROM exam WHERE account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS student_writing TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"STUDENT_WRITING\" TEXT"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS writing_score DOUBLE PRECISION"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"WRITING_SCORE\" DOUBLE PRECISION"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS open_classes VARCHAR(2000)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"OPEN_CLASSES\" VARCHAR(2000)"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS exam_submitted BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"EXAM_SUBMITTED\" BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS retake_allowed BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        try { stmt.execute("ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"RETAKE_ALLOWED\" BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        // 1. Ensure account_id exists on ALL multi-tenant tables
        String[] accountIdCols = {
            "ALTER TABLE IF EXISTS students ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"STUDENTS\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS classroom ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"CLASSROOM\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS classroom ADD COLUMN IF NOT EXISTS track VARCHAR(50)",
            "ALTER TABLE IF EXISTS \"CLASSROOM\" ADD COLUMN IF NOT EXISTS \"TRACK\" VARCHAR(50)",
            "ALTER TABLE IF EXISTS attendance ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"ATTENDANCE\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS score ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"SCORE\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS score_rule ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"SCORE_RULE\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS score_rules ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"SCORE_RULES\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS lessons ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"LESSONS\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS exam ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"EXAM\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS question ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"QUESTION\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS certificate_records ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"CERTIFICATE_RECORDS\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"LEAVE_REQUESTS\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT"
        };
        for (String q : accountIdCols) {
            try { stmt.execute(q); } catch (Exception ignored) {}
        }

        // 2. Default legacy rows to account_id = 97 (samkimchheng)
        try { stmt.execute("UPDATE certificate_records SET account_id = 97 WHERE account_id IS NULL"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"CERTIFICATE_RECORDS\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE students SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"STUDENTS\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE classroom SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"CLASSROOM\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE score SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"SCORE\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE score_rule SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"SCORE_RULE\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE attendance SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"ATTENDANCE\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE lessons SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"LESSONS\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE exam SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"EXAM\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE question SET account_id = 97 WHERE account_id IS NULL OR account_id = 1"); } catch (Exception ignored) {}
        try { stmt.execute("UPDATE \"QUESTION\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1"); } catch (Exception ignored) {}

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
            "CREATE TABLE IF NOT EXISTS leave_requests (id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, account_id BIGINT, student_id BIGINT NOT NULL, class_name VARCHAR(100), academic_year VARCHAR(50), start_date DATE NOT NULL, end_date DATE NOT NULL, leave_type VARCHAR(100), leave_session VARCHAR(100), reason VARCHAR(1000), parent_phone VARCHAR(100), status VARCHAR(50), teacher_note VARCHAR(500), created_at TIMESTAMP, reviewed_at TIMESTAMP)",
            "CREATE TABLE IF NOT EXISTS \"PUBLIC\".\"LEAVE_REQUESTS\" (\"ID\" BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY, \"ACCOUNT_ID\" BIGINT, \"STUDENT_ID\" BIGINT NOT NULL, \"CLASS_NAME\" VARCHAR(100), \"ACADEMIC_YEAR\" VARCHAR(50), \"START_DATE\" DATE NOT NULL, \"END_DATE\" DATE NOT NULL, \"LEAVE_TYPE\" VARCHAR(100), \"LEAVE_SESSION\" VARCHAR(100), \"REASON\" VARCHAR(1000), \"PARENT_PHONE\" VARCHAR(100), \"STATUS\" VARCHAR(50), \"TEACHER_NOTE\" VARCHAR(500), \"CREATED_AT\" TIMESTAMP, \"REVIEWED_AT\" TIMESTAMP)",
            "ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS account_id BIGINT",
            "ALTER TABLE IF EXISTS \"LEAVE_REQUESTS\" ADD COLUMN IF NOT EXISTS \"ACCOUNT_ID\" BIGINT",
            "ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS academic_year VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS \"LEAVE_REQUESTS\" ADD COLUMN IF NOT EXISTS \"ACADEMIC_YEAR\" VARCHAR(50) DEFAULT '២០២៦-២០២៧'",
            "ALTER TABLE IF EXISTS leave_requests ADD COLUMN IF NOT EXISTS class_name VARCHAR(100)",
            "ALTER TABLE IF EXISTS \"LEAVE_REQUESTS\" ADD COLUMN IF NOT EXISTS \"CLASS_NAME\" VARCHAR(100)",
            "UPDATE leave_requests SET account_id = 97 WHERE account_id IS NULL OR account_id = 1",
            "UPDATE \"LEAVE_REQUESTS\" SET \"ACCOUNT_ID\" = 97 WHERE \"ACCOUNT_ID\" IS NULL OR \"ACCOUNT_ID\" = 1",
            "UPDATE leave_requests SET academic_year = '២០២៦-២០២៧' WHERE academic_year IS NULL OR academic_year = ''",
            "UPDATE \"LEAVE_REQUESTS\" SET \"ACADEMIC_YEAR\" = '២០២៦-២០២៧' WHERE \"ACADEMIC_YEAR\" IS NULL OR \"ACADEMIC_YEAR\" = ''",
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

        // 3. Schema migration for daily_teaching_log (សេវាប្រចាំថ្ងៃ និងកំណត់ត្រាខ្លឹមសារមេរៀន)
        try {
            stmt.execute("CREATE TABLE IF NOT EXISTS daily_teaching_log (" +
                    "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                    "account_id BIGINT, " +
                    "academic_year VARCHAR(50), " +
                    "log_date DATE, " +
                    "day_of_week VARCHAR(50), " +
                    "slot_index INT, " +
                    "period_number INT, " +
                    "time_range VARCHAR(100), " +
                    "class_name VARCHAR(100), " +
                    "subject_name VARCHAR(255), " +
                    "lesson_title VARCHAR(500), " +
                    "content_summary TEXT, " +
                    "objectives TEXT, " +
                    "homework TEXT, " +
                    "notes TEXT, " +
                    "created_at TIMESTAMP, " +
                    "updated_at TIMESTAMP)");
        } catch (Exception ignored) {}

        // 4. Schema migration for vocabulary_words & student_vocabulary_progress (វាក្យសព្ទថ្នាក់ទី១០)
        try {
            stmt.execute("CREATE TABLE IF NOT EXISTS vocabulary_words (" +
                    "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                    "word_number INT, " +
                    "english_word VARCHAR(255) NOT NULL, " +
                    "clean_word VARCHAR(255), " +
                    "part_of_speech VARCHAR(50), " +
                    "khmer_translation TEXT NOT NULL, " +
                    "unit_name VARCHAR(100) NOT NULL, " +
                    "grade_level INT DEFAULT 10, " +
                    "account_id BIGINT, " +
                    "active BOOLEAN DEFAULT TRUE, " +
                    "example_sentence TEXT, " +
                    "created_at TIMESTAMP)");
        } catch (Exception ignored) {}

        try {
            stmt.execute("CREATE TABLE IF NOT EXISTS student_vocabulary_progress (" +
                    "id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                    "student_id BIGINT NOT NULL, " +
                    "word_id BIGINT NOT NULL, " +
                    "unit_name VARCHAR(100), " +
                    "mastered BOOLEAN DEFAULT FALSE, " +
                    "practice_count INT DEFAULT 0, " +
                    "correct_count INT DEFAULT 0, " +
                    "last_practiced TIMESTAMP)");
        } catch (Exception ignored) {}
    }

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository,
                                     StudentRepository studentRepository,
                                     ClassroomRepository classroomRepository,
                                     DailyTeachingLogRepository dailyTeachingLogRepository,
                                     ScoreRepository scoreRepository,
                                     ScoreRuleRepository scoreRuleRepository,
                                     CertificateRecordRepository certificateRecordRepository,
                                     AttendanceRepository attendanceRepository,
                                     LessonRepository lessonRepository,
                                     ExamRepository examRepository,
                                     QuestionRepository questionRepository,
                                     StandardLessonSeeder standardLessonSeeder,
                                     StandardVocabularySeeder standardVocabularySeeder,
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
                        populator.setSqlScriptEncoding("UTF-8");
                        populator.addScript(new ClassPathResource("data_dump.sql"));
                        populator.setContinueOnError(true);
                        populator.setIgnoreFailedDrops(true);
                        populator.execute(dataSource);
                        System.out.println(">>> [DataInitializer] Full database successfully restored!");

                        // Crucial: Re-apply schema migrations immediately after restore!
                        try (Connection conn = dataSource.getConnection();
                             Statement stmt = conn.createStatement()) {
                            applySchemaMigrations(stmt);
                            alignAllSequences(conn);
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

            // 2c. Ensure standard English audio lessons (Unit 1 to Unit 5) and transcripts exist for all teachers
            try {
                standardLessonSeeder.seedLessonsForAllTeachers();
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice seeding standard lessons: " + e.getMessage());
            }

            // 2c-bis. Ensure Grade 10 Standard Vocabulary words (460 words) are seeded
            try {
                standardVocabularySeeder.seedGrade10Vocabulary();
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice seeding grade 10 vocabulary: " + e.getMessage());
            }

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

            // 7c. Clean up orphan scores, leftover test score, test classroom and test score rules for legacy test classes
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] cleanupQueries = {
                    "DELETE FROM score WHERE student_id NOT IN (SELECT id FROM students)",
                    "DELETE FROM \"SCORE\" WHERE student_id NOT IN (SELECT id FROM \"STUDENTS\")",
                    "DELETE FROM attendance WHERE student_id IN (SELECT id FROM students WHERE class_name = '7A' OR class_name LIKE '%7%')",
                    "DELETE FROM \"ATTENDANCE\" WHERE student_id IN (SELECT id FROM \"STUDENTS\" WHERE \"CLASS_NAME\" = '7A' OR \"CLASS_NAME\" LIKE '%7%')",
                    "DELETE FROM score WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM \"SCORE\" WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM classroom WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM \"CLASSROOM\" WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM score_rule WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM \"SCORE_RULE\" WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM score_rules WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM \"SCORE_RULES\" WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM students WHERE class_name = '7A' OR class_name LIKE '%7%'",
                    "DELETE FROM \"STUDENTS\" WHERE \"CLASS_NAME\" = '7A' OR \"CLASS_NAME\" LIKE '%7%'"
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
                    a.setVocabularyPermission(true);
                    accountRepository.save(a);
                });
                accountRepository.findById(1L).ifPresent(a -> {
                    a.setPassword("admin@manulife%27");
                    a.setEmail("admincl@mitpukae.com");
                    a.setVocabularyPermission(true);
                    accountRepository.save(a);
                });

                // 2. Sam Kimchheng (chhengcl@mitpukae.com / techercl@%27)
                Account acc97 = accountRepository.findById(97L).orElse(accountRepository.findByUsername("samkimchheng").orElse(null));
                if (acc97 != null) {
                    acc97.setPassword("techercl@%27");
                    acc97.setEmail("chhengcl@mitpukae.com");
                    acc97.setFullName("សំ គឹមឆេង");
                    acc97.setTeachingClasses("១០E, ១០F, ១០G, ១១F, ១១G");
                    acc97.setSubject("ភាសាអង់គ្លេស");
                    acc97.setVocabularyPermission(true);
                    acc97.setSchoolName("វិទ្យាល័យ ហ៊ុន សែន ១០មករា បឹងកន្ទួត");
                    acc97.setPrincipalName("លី ប៊ុនថេង");
                    acc97.setProvince("ខេត្តពោធិ៍សាត់");
                    acc97.setCurrentAcademicYear("២០២៦-២០២៧");
                    accountRepository.save(acc97);
                }

                // 3. Seung Somuny (teachercl21@mitpukae.com / techercl@%21)
                Account acc33 = accountRepository.findById(33L).orElse(accountRepository.findByUsername("Teacher1001").orElse(null));
                if (acc33 != null) {
                    acc33.setPassword("techercl@%21");
                    acc33.setEmail("teachercl21@mitpukae.com");
                    acc33.setFullName("ស៊ឹង សុមុនី");
                    acc33.setSubject("គណិតវិទ្យា");
                    acc33.setTeachingClasses("៨D, ៨E");
                    acc33.setVocabularyPermission(false);
                    acc33.setSchoolName("វិទ្យាល័យ ហ៊ុន សែន ១០មករា បឹងកន្ទួត");
                    acc33.setPrincipalName("លី ប៊ុនថេង");
                    acc33.setProvince("ខេត្តពោធិ៍សាត់");
                    acc33.setCurrentAcademicYear("២០២៦-២០២៧");
                    accountRepository.save(acc33);
                }
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice syncing official credentials: " + e.getMessage());
            }

            // Ensure classrooms for Sam Kimchheng (97) and Seung Somuny (33)
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                String[] kimchhengClasses = {"១០E", "១០F", "១០G", "១១F", "១១G"};
                String[] years = {"២០២៦-២០២៧", "២០២៧-២០២៨"};
                for (String yr : years) {
                    for (String cls : kimchhengClasses) {
                        stmt.execute("INSERT INTO classroom (account_id, class_name, academic_year) " +
                                "SELECT 97, '" + cls + "', '" + yr + "' " +
                                "WHERE NOT EXISTS (SELECT 1 FROM classroom WHERE account_id = 97 AND class_name = '" + cls + "' AND academic_year = '" + yr + "')");
                    }
                }

                // Ensure official classrooms for Seung Somuny (33): 8D and 8E
                String[] somunyClasses = {"៨D", "៨E"};
                for (String yr : years) {
                    for (String cls : somunyClasses) {
                        stmt.execute("INSERT INTO classroom (account_id, class_name, academic_year) " +
                                "SELECT 33, '" + cls + "', '" + yr + "' " +
                                "WHERE NOT EXISTS (SELECT 1 FROM classroom WHERE account_id = 33 AND class_name = '" + cls + "' AND academic_year = '" + yr + "')");
                    }
                }

                // Ensure score rules for Seung Somuny (33)
                stmt.execute("INSERT INTO score_rule (account_id, class_name, subject_name, max_monthly_score, max_attendance_score, max_assignment_score) " +
                        "SELECT 33, 'ថ្នាក់រៀនទាំងអស់', 'គណិតវិទ្យា', 60, 20, 20 " +
                        "WHERE NOT EXISTS (SELECT 1 FROM score_rule WHERE account_id = 33 AND class_name = 'ថ្នាក់រៀនទាំងអស់' AND subject_name = 'គណិតវិទ្យា')");
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice syncing teacher classrooms: " + e.getMessage());
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

            // 11. Ensure official students for 10E, 10F, 10G, 11F, 11G are seeded if missing
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                boolean needsStudents = false;
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM students WHERE class_name IN ('១០G', '១១F', '១១G')")) {
                    if (rs.next() && rs.getInt(1) < 10) {
                        needsStudents = true;
                    }
                } catch (Exception ignored) {
                    try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM \"STUDENTS\" WHERE \"CLASS_NAME\" IN ('១០G', '១១F', '១១G')")) {
                        if (rs.next() && rs.getInt(1) < 10) {
                            needsStudents = true;
                        }
                    } catch (Exception ignored2) {
                        needsStudents = true;
                    }
                }

                if (needsStudents) {
                    System.out.println(">>> [DataInitializer] Seeding official students for 10E, 10F, 10G, 11F, 11G from students_insert.sql...");
                    try {
                        stmt.executeUpdate("DELETE FROM students WHERE id >= 101 AND id <= 500");
                    } catch (Exception ignored) {}
                    try {
                        stmt.executeUpdate("DELETE FROM \"STUDENTS\" WHERE \"ID\" >= 101 AND \"ID\" <= 500");
                    } catch (Exception ignored) {}
                    ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
                    pop.setSqlScriptEncoding("UTF-8");
                    pop.addScript(new ClassPathResource("students_insert.sql"));
                    pop.setContinueOnError(true);
                    pop.execute(dataSource);
                    System.out.println(">>> [DataInitializer] Official students successfully seeded!");
                    try {
                        stmt.executeUpdate("DELETE FROM students WHERE class_name = '7A' OR class_name LIKE '%7%'");
                        stmt.executeUpdate("DELETE FROM \"STUDENTS\" WHERE \"CLASS_NAME\" = '7A' OR \"CLASS_NAME\" LIKE '%7%'");
                    } catch (Exception ignored) {}

                    try (Connection connAlign = dataSource.getConnection()) {
                        alignTableSequence(connAlign, "students", "id");
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice seeding students: " + e.getMessage());
            }

            // 12. Ensure Demo Teacher Account (ID: 98) and sample data exist
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Ensure account 98 exists
                stmt.execute("INSERT INTO account (id, username, password, email, role, full_name, active, gender, school_name, province, position, teaching_classes, principal_name, current_academic_year) " +
                        "SELECT 98, 'demo', 'demo@1234', 'demo@mitpukae.com', 'TEACHER', 'គ្រូបង្រៀន (គណនីសាកល្បង)', true, 'ប្រុស', 'វិទ្យាល័យ ហ៊ុនសែន១០មករាបឹងកន្ទួត', 'ខេត្តពោធិ៍សាត់', 'គ្រូបង្រៀន (សាកល្បង)', 'ថ្នាក់សាកល្បង', 'លី ប៊ុនថេង', '២០២៦-២០២៧' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM account WHERE id = 98 OR username = 'demo')");

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

            // 14. Ensure Seung Somuny's (33) official 48 students for Class 8D and classrooms exist
            seedSeungSomunyDataIfMissing(studentRepository, classroomRepository);

            // 15. Ensure Sam Kimchheng's (97) daily teaching logs are preserved and seeded
            seedSamKimchhengDailyTeachingLogsIfMissing(dailyTeachingLogRepository);

            // Align all table sequences for H2 and PostgreSQL so new auto-increment rows never collide with pre-seeded data
            try {
                alignAllSequences(dataSource);
                System.out.println(">>> [DataInitializer] All database table sequences aligned successfully!");
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice aligning sequences: " + e.getMessage());
            }
        };
    }

    private void seedEnglishExamIfMissing(ExamRepository examRepository, QuestionRepository questionRepository) {
        try {
            Long[] accountIds = {97L, 98L};
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
                        exam.setSection1MaxPoints(20);
                        exam.setSection2Title("II. GRAMMAR SECTION:");
                        exam.setSection2Instruction("Choose the best and correct only one answer A, B, C or D as the following: (15pts)");
                        exam.setSection2MaxPoints(15);
                        exam.setSection3Title("III. VOCABULARY SECTION:");
                        exam.setSection3Instruction("Choose the best and correct only one answer A, B, C or D as the following: (15pts)");
                        exam.setSection3MaxPoints(15);
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

    public static void alignAllSequences(DataSource dataSource) {
        if (dataSource == null) return;
        try (Connection conn = dataSource.getConnection()) {
            alignAllSequences(conn);
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice aligning all sequences: " + e.getMessage());
        }
    }

    public static void alignAllSequences(Connection conn) {
        if (conn == null) return;
        String[] tables = {
            "students", "classroom", "account", "lessons", "lesson_progress",
            "lesson_transcript", "score", "score_rule", "score_rules", "exam",
            "question", "attendance", "certificate_records", "leave_requests", "schedule",
            "daily_teaching_log", "vocabulary_words", "student_vocabulary_progress"
        };
        for (String table : tables) {
            alignTableSequence(conn, table, "id");
        }
    }

    public static void alignTableSequence(Connection conn, String tableName, String idCol) {
        if (conn == null || tableName == null || idCol == null) return;
        try {
            String dbProduct = conn.getMetaData().getDatabaseProductName().toLowerCase();
            boolean isPg = dbProduct.contains("postgres");

            long maxId = 0L;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(" + idCol + "), 0) FROM " + tableName)) {
                if (rs.next()) {
                    maxId = rs.getLong(1);
                }
            } catch (Exception ex1) {
                try (Statement s = conn.createStatement();
                     ResultSet rs = s.executeQuery("SELECT COALESCE(MAX(\"" + idCol.toUpperCase() + "\"), 0) FROM \"" + tableName.toUpperCase() + "\"")) {
                    if (rs.next()) {
                        maxId = rs.getLong(1);
                    }
                } catch (Exception ignored) {
                    return;
                }
            }

            long nextId = Math.max(1000L, maxId + 1);

            try (Statement s = conn.createStatement()) {
                if (isPg) {
                    try {
                        s.execute("SELECT setval(pg_get_serial_sequence('" + tableName.toLowerCase() + "', '" + idCol.toLowerCase() + "'), " + Math.max(maxId, 1L) + ", true)");
                    } catch (Exception ignored) {}
                } else {
                    try {
                        s.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + idCol + " RESTART WITH " + nextId);
                    } catch (Exception ex1) {
                        try {
                            s.execute("ALTER TABLE \"" + tableName.toUpperCase() + "\" ALTER COLUMN \"" + idCol.toUpperCase() + "\" RESTART WITH " + nextId);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void seedSeungSomunyDataIfMissing(StudentRepository studentRepository, ClassroomRepository classroomRepository) {
        try {
            // Ensure classrooms 8D and 8E exist
            String[] years = {"២០២៦-២០២៧", "២០២៧-២០២៨"};
            String[] somunyClasses = {"៨D", "៨E"};
            for (String yr : years) {
                for (String cls : somunyClasses) {
                    if (!classroomRepository.existsByClassNameAndAccountIdAndAcademicYear(cls, 33L, yr)) {
                        Classroom c = new Classroom();
                        c.setAccountId(33L);
                        c.setClassName(cls);
                        c.setAcademicYear(yr);
                        classroomRepository.save(c);
                    }
                }
            }

            // Ensure 48 official students of 8D exist
            long somunyStudentsCount = studentRepository.findByClassNameAndAccountId("៨D", 33L).size();
            if (somunyStudentsCount < 40) {
                System.out.println(">>> [DataInitializer] Seeding official 48 students for Seung Somuny (33) - Class 8D...");
                String[][] somuny8DStudents = {
                    {"តាត សុភេង", "ប្រុស", "៨D-01", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"តឿង សិល័ដ្ឋ", "ប្រុស", "៨D-02", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ហាង ស៊ីវិន", "ស្រី", "៨D-03", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឃុន សុខា", "ប្រុស", "៨D-04", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ថយ ចន្ថា", "ស្រី", "៨D-05", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ចាន់ តុលា", "ប្រុស", "៨D-06", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ជិ សៅលៀប", "ប្រុស", "៨D-07", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឈុយ ចាន់វី", "ស្រី", "៨D-08", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឈឿន វីរៈ", "ប្រុស", "៨D-09", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ណារ៉ា ដានិត", "ស្រី", "៨D-10", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ទ្រង់ ឈិនណារ៉ា", "ស្រី", "៨D-11", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ធឿម ដាវ៉ាន", "ស្រី", "៨D-12", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ប៊ិន ចាន់ធឿន", "ប្រុស", "៨D-13", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ប៉ុង កក្កដា", "ស្រី", "៨D-14", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"បូរ កាន់និកា", "ស្រី", "៨D-15", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"មុន តុលា", "ប្រុស", "៨D-16", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"មោ រិទ្ធិ", "ប្រុស", "៨D-17", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ម៉ី ចន្ថា", "ស្រី", "៨D-18", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"មឿន ឡានីត", "ស្រី", "៨D-19", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"រស សំណាង", "ប្រុស", "៨D-20", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"រិន សុណារ៉ា", "ប្រុស", "៨D-21", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"រុន ផែនលី", "ប្រុស", "៨D-22", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"រុន សុខចិន្តា", "ប្រុស", "៨D-23", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"លឹម វី", "ប្រុស", "៨D-24", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"វឿន សុខឃីម", "ស្រី", "៨D-25", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សុវណ្ណ ណារ៉ាកា", "ស្រី", "៨D-26", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សួន សារ៉ាត់", "ស្រី", "៨D-27", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សៀម ធីតា", "ស្រី", "៨D-28", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សៀង គុកា", "ស្រី", "៨D-29", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សេង សំបូរ", "ប្រុស", "៨D-30", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សោម សុខឃឿន", "ប្រុស", "៨D-31", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សៃ ចិន្តា", "ប្រុស", "៨D-32", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សំ កឹម សាន", "ប្រុស", "៨D-33", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សំ ឡៀងហេង", "ស្រី", "៨D-34", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ហ៊ាន ស៊ុំណារិន", "ស្រី", "៨D-35", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ហៀង កឹមលៀង", "ប្រុស", "៨D-36", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ហាង សុវណ្ណារ៉ា", "ប្រុស", "៨D-37", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ហៃ សុខា", "ប្រុស", "៨D-38", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឡូត ឡៀង", "ប្រុស", "៨D-39", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឡេង ចាន់ណារ៉ា", "ស្រី", "៨D-40", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"អុយ ធីរ៉ិទ្ធ", "ប្រុស", "៨D-41", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"អាន សុភនីតា", "ស្រី", "៨D-42", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"អែម សុភ័ស្ស", "ស្រី", "៨D-43", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សុខ វីសា", "ស្រី", "៨D-44", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សាំឌី ថៃម៉េង", "ប្រុស", "៨D-45", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ឈឿន ដាវិត", "ប្រុស", "៨D-46", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"ស៊ុន រ៉ាវី", "ប្រុស", "៨D-47", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"},
                    {"សេង កឹមឌី", "ប្រុស", "៨D-48", "៨D", "២០២៦-២០២៧", "កំពុងសិក្សា"}
                };
                for (String[] st : somuny8DStudents) {
                    Student s = new Student();
                    s.setAccountId(33L);
                    s.setName(st[0]);
                    s.setGender(st[1]);
                    s.setStudentCode(st[2]);
                    s.setClassName(st[3]);
                    s.setAcademicYear(st[4]);
                    s.setStatus(st[5]);
                    studentRepository.save(s);
                }
                System.out.println(">>> [DataInitializer] 48 students for Seung Somuny (33) seeded successfully!");
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding Seung Somuny data: " + e.getMessage());
        }
    }

    private void seedSamKimchhengDailyTeachingLogsIfMissing(DailyTeachingLogRepository dailyTeachingLogRepository) {
        try {
            long existingLogsCount = dailyTeachingLogRepository.count();
            if (existingLogsCount >= 10) {
                return;
            }

            System.out.println(">>> [DataInitializer] Seeding official daily teaching logs for Sam Kimchheng (97)...");

            String[] timeRanges = {
                "07:00 - 08:00 ព្រឹក", // 0
                "08:00 - 09:00 ព្រឹក", // 1
                "09:00 - 10:00 ព្រឹក", // 2
                "10:00 - 11:00 ព្រឹក", // 3
                "01:00 - 02:00 រសៀល", // 4
                "02:00 - 03:00 រសៀល", // 5
                "03:00 - 04:00 រសៀល", // 6
                "04:00 - 05:00 រសៀល"  // 7
            };

            Object[][] mondaySlots = {
                {0, "10F", "Unit 3: Technology in Education", "Vocabulary & Reading: Smartphones in the Classroom", "សិស្សចេះប្រើប្រាស់ពាក្យគន្លឹះ និងយល់ន័យអត្ថបទ", "រៀនពាក្យបន្ថែម និងឆ្លើយសំណួរទំព័រ ៤២", "បង្រៀនទាន់ពេលវេលា សិស្សចូលរួមយ៉ាងសកម្ម"},
                {1, "10F", "Unit 3: Technology in Education", "Grammar: Present Perfect vs Past Simple", "សិស្សអាចបង្កើតប្រយោគជាមួយ since/for បានត្រឹមត្រូវ", "លំហាត់ Grammar ទំព័រ ៤៤", "សិស្សអនុវត្តជាដៃគូបានល្អ"},
                {2, "10G", "Unit 3: Technology in Education", "Reading Comprehension & Key Vocabulary", "សិស្សអានអត្ថបទ និងបកប្រែពាក្យគន្លឹះ", "អានអត្ថបទឡើងវិញនៅផ្ទះ", "បង្រៀនតាមផែនការ"},
                {3, "10G", "Unit 3: Technology in Education", "Speaking & Pair Discussions: Digital Tools", "សិស្សអាចសន្ទនាជាក្រុមតូចៗ", "សរសេរកថាខណ្ឌខ្លី ៥ បន្ទាត់", "សិស្សចូលរួមពេញលេញ"},
                {5, "10E", "Unit 3: Technology in Education", "Vocabulary Review & Listening Practice", "សិស្សស្តាប់ការសន្ទនា និងឆ្លើយសំណួរ True/False", "រៀនពាក្យក្នុង Unit 3", "សិស្សចាប់អារម្មណ៍ខ្លាំង"},
                {6, "10E", "Unit 3: Technology in Education", "Writing: Short Paragraph on Technology", "សិស្សសរសេរប្រយោគ និងកថាខណ្ឌខ្លី", "បំពេញសេចក្តីព្រាងកថាខណ្ឌ", "កែលម្អការសរសេររបស់សិស្ស"}
            };

            Object[][] tuesdaySlots = {
                {0, "11F", "Unit 4: The Natural World", "Reading: Biodiversity and Ecosystems in Cambodia", "សិស្សយល់ដឹងពីការការពារបរិស្ថាន និងពាក្យបច្ចេកទេស", "កត់ត្រាគំនិតការពារព្រៃឈើ", "សិស្សឆ្លើយសំណួរបានល្អ"},
                {1, "11F", "Unit 4: The Natural World", "Grammar: Passive Voice in Scientific Descriptions", "សិស្សចេះបំប្លែង Active ទៅ Passive Voice", "លំហាត់ទំព័រ ៥៦", "ពន្យល់ក្បួនវេយ្យាករណ៍យ៉ាងក្បោះក្បាយ"},
                {2, "11G", "Unit 4: The Natural World", "Vocabulary & Reading: Wildlife Conservation", "សិស្សចេះវិភាគអត្ថបទ និងរកគំនិតសំខាន់", "រៀនពាក្យថ្មីៗ ៨ ពាក្យ", "ដំណើរការបង្រៀនរលូនល្អ"},
                {3, "11G", "Unit 4: The Natural World", "Discussion & Presentation: Protecting Forests", "សិស្សជជែកពិភាក្សា និងបញ្ចេញមតិយោបល់", "រៀបចំប្រធានបទសប្តាហ៍ក្រោយ", "សិស្សមានភាពក្លាហានក្នុងការនិយាយ"}
            };

            Object[][] wednesdaySlots = {
                {0, "10E", "Unit 4: Future Careers & Skills", "Vocabulary: Emerging 21st-Century Careers", "សិស្សស្គាល់មុខរបរ និងជំនាញអនាគត", "សរសេរពីមុខរបរដែលខ្លួនស្រឡាញ់", "បង្រៀនទាន់ពេល"},
                {1, "10E", "Unit 4: Future Careers & Skills", "Grammar: Modals of Obligation & Advice (must, should)", "សិស្សប្រើប្រាស់ should, must, have to បានត្រឹមត្រូវ", "លំហាត់វេយ្យាករណ៍ទំព័រ ៥០", "សិស្សយល់ច្បាស់"},
                {2, "10F", "Unit 4: Future Careers & Skills", "Listening & Role-play: Job Interviews", "សិស្សចេះឆ្លើយសំណួរសម្ភាសន៍ជាភាសាអង់គ្លេស", "អនុវត្តសន្ទនាជាមួយដៃគូ", "ថ្នាក់រៀនមានភាពរស់រវើក"},
                {3, "10F", "Unit 4: Future Careers & Skills", "Writing: Professional Cover Email", "សិស្សចេះសរសេរ Email ស្នើសុំការងារ", "សរសេរ Email ផ្ញើជូនគ្រូ", "ផ្ទៀងផ្ទាត់ទម្រង់លិខិត"}
            };

            Object[][] thursdaySlots = {
                {0, "11G", "Unit 5: Science and Innovation", "Reading: Great Innovations that Changed Society", "សិស្សស្គាល់ការច្នៃប្រឌិត និងការស្រាវជ្រាវ", "រកព័ត៌មានពីការបង្កើត Internet", "ខ្លឹមសារទាក់ទាញ"},
                {1, "11G", "Unit 5: Science and Innovation", "Grammar: Relative Clauses (who, which, that)", "សិស្សចេះភ្ជាប់ប្រយោគជាមួយ Relative Pronouns", "លំហាត់ទំព័រ ៦៨", "សិស្សធ្វើលំហាត់បានច្រើន"},
                {2, "11F", "Unit 5: Science and Innovation", "Reading: Artificial Intelligence in Healthcare", "សិស្សយល់ពីអត្ថប្រយោជន៍នៃ AI", "រៀនពាក្យគន្លឹះ AI", "សិស្សជជែកច្រើន"},
                {3, "11F", "Unit 5: Science and Innovation", "Writing: Argumentative Essay Outline", "សិស្សចេះរៀបចំ Outline អត្ថបទ", "បំពេញ Outline", "សិស្សផ្តោតអារម្មណ៍ល្អ"}
            };

            Object[][] fridaySlots = {
                {5, "10G", "Unit 4: Future Careers & Skills", "Review & Vocabulary Quiz", "វាស់ស្ទង់ចំណេះដឹងពាក្យសព្ទរបស់សិស្ស", "មើលមេរៀនឡើងវិញ", "លទ្ធផល Quiz ល្អ"},
                {6, "10G", "Unit 4: Future Careers & Skills", "Speaking: My Dream Job Short Presentations", "សិស្សឡើងធ្វើបទបង្ហាញខ្លីមុខថ្នាក់", "កត់ត្រាចំណុចខ្វះខាត", "បរិយាកាសរីករាយ"}
            };

            LocalDate now = LocalDate.now();
            LocalDate mondayThisWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate[] targetMondays = { mondayThisWeek.minusWeeks(1), mondayThisWeek };

            for (LocalDate baseMon : targetMondays) {
                Map<java.time.DayOfWeek, Object[][]> map = new LinkedHashMap<>();
                map.put(java.time.DayOfWeek.MONDAY, mondaySlots);
                map.put(java.time.DayOfWeek.TUESDAY, tuesdaySlots);
                map.put(java.time.DayOfWeek.WEDNESDAY, wednesdaySlots);
                map.put(java.time.DayOfWeek.THURSDAY, thursdaySlots);
                map.put(java.time.DayOfWeek.FRIDAY, fridaySlots);

                for (Map.Entry<java.time.DayOfWeek, Object[][]> entry : map.entrySet()) {
                    int dayOffset = entry.getKey().getValue() - java.time.DayOfWeek.MONDAY.getValue();
                    LocalDate logDate = baseMon.plusDays(dayOffset);
                    String dowKhmer = com.nubb.classroom.controller.DailyPlanController.getKhmerDayName(entry.getKey());

                    for (Object[] row : entry.getValue()) {
                        int slotIdx = (Integer) row[0];
                        String cls = (String) row[1];
                        String title = (String) row[2];
                        String content = (String) row[3];
                        String obj = (String) row[4];
                        String hw = (String) row[5];
                        String notes = (String) row[6];

                        if (dailyTeachingLogRepository.findByAccountIdAndLogDateAndSlotIndex(97L, logDate, slotIdx).isEmpty()) {
                            DailyTeachingLog l = new DailyTeachingLog();
                            l.setAccountId(97L);
                            l.setAcademicYear("២០២៦-២០២៧");
                            l.setLogDate(logDate);
                            l.setDayOfWeek(dowKhmer);
                            l.setSlotIndex(slotIdx);
                            l.setPeriodNumber(slotIdx + 1);
                            l.setTimeRange(timeRanges[slotIdx]);
                            l.setClassName(cls);
                            l.setSubjectName("ភាសាអង់គ្លេស");
                            l.setLessonTitle(title);
                            l.setContentSummary(content);
                            l.setObjectives(obj);
                            l.setHomework(hw);
                            l.setNotes(notes);
                            dailyTeachingLogRepository.save(l);
                        }
                    }
                }
            }
            System.out.println(">>> [DataInitializer] Official daily teaching logs for Sam Kimchheng (97) seeded successfully!");
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding daily teaching logs: " + e.getMessage());
        }
    }
}
