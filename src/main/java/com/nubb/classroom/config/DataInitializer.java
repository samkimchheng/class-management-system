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

            // 2c-bis. Ensure Grade 10 (460 words), Grade 11 (303 words), & Grade 12 (294 words) Standard Vocabulary words are seeded
            try {
                standardVocabularySeeder.seedGrade10Vocabulary();
                standardVocabularySeeder.seedGrade11Vocabulary();
                standardVocabularySeeder.seedGrade12Vocabulary();
            } catch (Exception e) {
                System.err.println(">>> [DataInitializer] Notice seeding vocabulary: " + e.getMessage());
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

            // 16. Safely seed a starter Standard MoEYS Khmer Exam ONLY for Khmer teachers who have zero Khmer exams yet
            seedKhmerExamIfMissing(accountRepository, classroomRepository, examRepository, questionRepository);

            // 17. Safely seed a starter Standard MoEYS Mathematics Exam ONLY for Math teachers who have zero Math exams yet
            seedMathExamIfMissing(accountRepository, classroomRepository, examRepository, questionRepository);

            // 18. Safely seed a starter Standard MoEYS Physics Exam ONLY for Physics teachers who have zero Physics exams yet
            seedPhysicsExamIfMissing(accountRepository, classroomRepository, examRepository, questionRepository);

            // 19. Safely seed starter Standard MoEYS Exams for Chemistry, Biology, History, Geography, Civic/Morality, Earth Science & Computer Science
            seedRemainingStandardSubjectExamsIfMissing(accountRepository, classroomRepository, examRepository, questionRepository);

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

    private void seedKhmerExamIfMissing(
            AccountRepository accountRepository,
            ClassroomRepository classroomRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        try {
            List<Account> allAccounts = accountRepository.findAll();
            for (Account acc : allAccounts) {
                String subj = acc.getSubject();
                boolean isDemo = Long.valueOf(98L).equals(acc.getId());
                if (!isDemo && (subj == null || !subj.contains("ខ្មែរ"))) {
                    continue;
                }

                // STRICT ZERO DATA LOSS: Never touch or overwrite if this teacher already has any Khmer exam
                boolean hasAnyKhmerExam = examRepository.findByAccountId(acc.getId()).stream()
                        .anyMatch(e -> "ភាសាខ្មែរ".equals(e.getSubjectName()));
                if (hasAnyKhmerExam) {
                    continue;
                }

                List<Classroom> teacherClasses = classroomRepository.findByAccountId(acc.getId());
                String classNames = teacherClasses.stream()
                        .map(Classroom::getClassName)
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining(", "));
                if (classNames.isEmpty()) {
                    classNames = (acc.getTeachingClasses() != null && !acc.getTeachingClasses().trim().isEmpty())
                            ? acc.getTeachingClasses().trim()
                            : "៨D";
                }

                Exam exam = new Exam();
                exam.setAccountId(acc.getId());
                exam.setMonth("ខែវិច្ឆិកា ២០២៦");
                exam.setSubjectName("ភាសាខ្មែរ");
                exam.setClassName(classNames);
                exam.setOpenClasses(""); // Closed by default until teacher opens for class
                exam.setReadingPassage(
                        "ការអានសៀវភៅគឺជាប្រភពនៃចំណេះដឹងដ៏ធំធេង ដែលមិនអាចកាត់ថ្លៃបានសម្រាប់មនុស្សគ្រប់រូប ជាពិសេសយុវជនជំនាន់ក្រោយ។ " +
                        "នៅក្នុងយុគសម័យបច្ចេកវិទ្យាដ៏ទំនើបនាពេលបច្ចុប្បន្ន ទោះបីជាមានឧបករណ៍អេឡិចត្រូនិកជាច្រើនផ្តល់ព័ត៌មានរហ័សក៏ដោយ " +
                        "ការអានសៀវភៅនៅតែមានតម្លៃខ្ពង់ខ្ពស់ ព្រោះវាជួយបង្កើនការផ្តោតអារម្មណ៍ ពង្រីកវាក្យស័ព្ទ និងហ្វឹកហាត់គំនិតត្រិះរិះពិចារណាឱ្យបានស៊ីជម្រៅ។\n\n" +
                        "លើសពីនេះទៅទៀត ទម្លាប់អានជារៀងរាល់ថ្ងៃជួយឱ្យសិស្សានុសិស្សចេះសរសេរអក្សរខ្មែរបានត្រឹមត្រូវតាមក្បួនអក្ខរាវិរុទ្ធ " +
                        "យល់ដឹងពីវប្បធម៌ អរិយធម៌ដ៏រុងរឿងរបស់ដូនតា និងមានសមត្ថភាពដោះស្រាយបញ្ហាក្នុងជីវភាពប្រចាំថ្ងៃប្រកបដោយភាពឈ្លាសវៃ។"
                );
                exam.setSection1Title("I. អំណាន និងការយល់ដឹងអត្ថបទ៖");
                exam.setSection1Instruction("ចូរអានអត្ថបទខាងលើដោយយកចិត្តទុកដាក់ រួចជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection1MaxPoints(15);
                exam.setSection2Title("II. វេយ្យាករណ៍ និងអក្ខរាវិរុទ្ធ៖");
                exam.setSection2Instruction("ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection2MaxPoints(15);
                exam.setSection3Title("III. អក្សរសិល្ប៍ និងវាក្យស័ព្ទ៖");
                exam.setSection3Instruction("ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection3MaxPoints(15);
                exam.setSection4Title("IV. សំណេរ / តែងសេចក្តី៖");
                exam.setSection4Instruction("ចូរសរសេរតែងសេចក្តីតាមក្បួនខ្នាត (សេចក្តីផ្តើម តួសេចក្តី សេចក្តីបញ្ចប់) លើប្រធានបទខាងក្រោម៖ (២៥ ពិន្ទុ)");
                exam.setWritingPrompt("ប្រធាន៖ «ចំណេះវិជ្ជាជាទ្រព្យដ៏ប្រសើរដែលចោរលួចមិនបាន»។ ចូរពន្យល់ពាក្យស្លោកនេះ ដោយលើកឧទាហរណ៍ពីសង្គម និងជីវភាពសិក្សាជាក់ស្តែងមកបញ្ជាក់។");
                exam.setWritingMaxPoints(25);
                exam = examRepository.save(exam);

                String[][] sampleQuestions = {
                    // Section I: Reading comprehension (kh_reading)
                    {"kh_reading", "១. តាមរយៈអត្ថបទខាងលើ តើការអានសៀវភៅជាអ្វី?", "ជាការចំណាយពេលឥតប្រយោជន៍", "ជាប្រភពនៃចំណេះដឹងដ៏ធំធេង", "សម្រាប់តែមនុស្សចាស់ប៉ុណ្ណោះ", "មិនសំខាន់ក្នុងសម័យទំនើបទេ", "B"},
                    {"kh_reading", "២. ហេតុអ្វីបានជាការអានសៀវភៅនៅតែមានតម្លៃក្នុងសម័យបច្ចេកវិទ្យាទំនើប?", "ព្រោះវាជួយបង្កើនការផ្តោតអារម្មណ៍ និងគំនិតត្រិះរិះពិចារណា", "ព្រោះសៀវភៅមានរូបភាពស្អាត", "ព្រោះមិនមានទូរស័ព្ទប្រើប្រាស់", "ព្រោះគ្រូបង្ខំឱ្យអាន", "A"},
                    {"kh_reading", "៣. តើទម្លាប់អានជារៀងរាល់ថ្ងៃជួយសិស្សយ៉ាងដូចម្តេចលើការសរសេរ?", "សរសេរបានលឿនតែខុសច្រើន", "មិនចាំបាច់រៀនវេយ្យាករណ៍", "ចេះសរសេរត្រឹមត្រូវតាមក្បួនអក្ខរាវិរុទ្ធ", "ភ្លេចវាក្យស័ព្ទបុរាណ", "C"},
                    {"kh_reading", "៤. ក្រៅពីភាសា តើការអានសៀវភៅជួយឱ្យសិស្សយល់ដឹងពីអ្វីទៀត?", "របៀបលេងហ្គេម", "វប្បធម៌ និងអរិយធម៌ដ៏រុងរឿងរបស់ដូនតា", "ការទិញទំនិញតាមអនឡាញ", "ព័ត៌មានកីឡាតែម្យ៉ាង", "B"},
                    {"kh_reading", "៥. តើគំនិតសំខាន់បំផុតនៃអត្ថបទនេះចង់អប់រំដូចម្តេច?", "ឱ្យបោះបង់បច្ចេកវិទ្យាទាំងស្រុង", "ឱ្យបណ្តុះទម្លាប់អានសៀវភៅដើម្បីកសាងសមត្ថភាព", "ឱ្យទិញសៀវភៅទុកតាំងលម្អ", "មិនបាច់ទៅសាលារៀន", "B"},
                    // Section II: Grammar & Spelling (kh_grammar)
                    {"kh_grammar", "១. ពាក្យណាមួយខាងក្រោមជា «កិរិយាសព្ទ»?", "សៀវភៅ", "ស្រស់ស្អាត", "សិក្សា", "ដោយសារ", "C"},
                    {"kh_grammar", "២. ពាក្យណាមួយដែលសរសេរត្រឹមត្រូវតាមអក្ខរាវិរុទ្ធខ្មែរ?", "វិទ្យាល័យ", "វិទ្យាលយ័", "វិទ្យាលយ", "វិទ្យាល័យ្", "A"},
                    {"kh_grammar", "៣. តើពាក្យ «កតញ្ញូ» មានន័យផ្ទុយនឹងពាក្យអ្វី?", "សប្បុរស", "អកតញ្ញូ", "សុចរិត", "មេត្តា", "B"},
                    {"kh_grammar", "៤. ក្នុងល្បះ «សិស្សល្អតែងគោរពវិន័យសាលា» តើពាក្យ «ល្អ» ជាថ្នាក់ពាក្យអ្វី?", "នាម", "កិរិយាសព្ទ", "គុណនាម", "ឈ្នាប់", "C"},
                    {"kh_grammar", "៥. ពាក្យណាមួយជា «ពាក្យសមាស» (ពាក្យផ្សំបង្កើតន័យថ្មី)?", "ទឹក", "ក្តារខៀន", "ដី", "មេឃ", "B"},
                    // Section III: Literature & Vocabulary (kh_literature)
                    {"kh_literature", "១. សុភាសិតខ្មែរថា «ចេះពីរៀន មានពី...» តើពាក្យពេញគឺអ្វី?", "ដើរ", "រក", "ដេក", "លេង", "B"},
                    {"kh_literature", "២. រឿងអក្សរសិល្ប៍បុរាណខ្មែរណាមួយដែលបង្ហាញពីការតស៊ូ និងសច្ចធម៌របស់ស្ត្រីខ្មែរ?", "រឿងទុំទាវ", "រឿងផ្កាស្រពោន", "រឿងកុលាបប៉ៃលិន", "រឿងព្រះវេស្សន្តរ", "A"},
                    {"kh_literature", "៣. ពាក្យ «វឌ្ឍនភាព» មានន័យដូចម្តេច?", "ការធ្លាក់ចុះ", "ភាពរីកចម្រើន", "ភាពស្ងប់ស្ងាត់", "ការបែកបាក់", "B"},
                    {"kh_literature", "៤. តាមក្បួនតែងសេចក្តីខ្មែរ តើផ្នែកធំៗនៃតែងសេចក្តីមានប៉ុន្មានផ្នែក?", "២ ផ្នែក", "៣ ផ្នែក (សេចក្តីផ្តើម តួសេចក្តី សេចក្តីបញ្ចប់)", "៤ ផ្នែក", "៥ ផ្នែក", "B"},
                    {"kh_literature", "៥. សុភាសិត «ទុំមុនស្រគាល» មានន័យអប់រំដូចម្តេច?", "លើកទឹកចិត្តឱ្យរហ័សរហួន", "ហាមមិនឱ្យធ្វើអ្វីហួសវ័យ ឬមុនពេលកំណត់សមស្រប", "ឱ្យចេះដាំផ្លែឈើ", "ឱ្យស្រឡាញ់មិត្តភក្តិ", "B"}
                };
                for (String[] row : sampleQuestions) {
                    Question q = new Question();
                    q.setExamId(exam.getId());
                    q.setAccountId(acc.getId());
                    q.setSkillType(row[0]);
                    q.setQuestionText(row[1]);
                    q.setOptionA(row[2]);
                    q.setOptionB(row[3]);
                    q.setOptionC(row[4]);
                    q.setOptionD(row[5]);
                    q.setCorrectOption(row[6]);
                    questionRepository.save(q);
                }
                System.out.println(">>> [DataInitializer] Seeded starter Standard MoEYS Khmer Exam for Khmer teacher: " + acc.getFullName() + " (ID " + acc.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding Khmer exam: " + e.getMessage());
        }
    }

    private void seedMathExamIfMissing(
            AccountRepository accountRepository,
            ClassroomRepository classroomRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        try {
            List<Account> allAccounts = accountRepository.findAll();
            for (Account acc : allAccounts) {
                String subj = acc.getSubject();
                boolean isDemo = Long.valueOf(98L).equals(acc.getId());
                if (!isDemo && (subj == null || (!subj.contains("គណិត") && !subj.toLowerCase().contains("math")))) {
                    continue;
                }

                // STRICT ZERO DATA LOSS: Never touch or overwrite if this teacher already has any Math exam
                boolean hasAnyMathExam = examRepository.findByAccountId(acc.getId()).stream()
                        .anyMatch(e -> "គណិតវិទ្យា".equals(e.getSubjectName()));
                if (hasAnyMathExam) {
                    continue;
                }

                List<Classroom> teacherClasses = classroomRepository.findByAccountId(acc.getId());
                String classNames = teacherClasses.stream()
                        .map(Classroom::getClassName)
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining(", "));
                if (classNames.isEmpty()) {
                    classNames = (acc.getTeachingClasses() != null && !acc.getTeachingClasses().trim().isEmpty())
                            ? acc.getTeachingClasses().trim()
                            : "៨D";
                }

                Exam exam = new Exam();
                exam.setAccountId(acc.getId());
                exam.setMonth("ខែវិច្ឆិកា ២០២៦");
                exam.setSubjectName("គណិតវិទ្យា");
                exam.setClassName(classNames);
                exam.setOpenClasses(""); // Closed by default until teacher opens for class
                exam.setReadingPassage(
                        "រូបមន្តជំនួយសំខាន់ៗ៖\n" +
                        "• (a + b)² = a² + 2ab + b²  |  (a - b)² = a² - 2ab + b²  |  a² - b² = (a - b)(a + b)\n" +
                        "• ទ្រឹស្តីបទពីតាគ័រក្នុងត្រីកោណកែង៖ c² = a² + b²\n" +
                        "• ផ្ទៃក្រឡាត្រីកោណ៖ S = (បាត × កម្ពស់) / 2  |  បរិមាត្ររង្វង់៖ P = 2πR"
                );
                exam.setSection1Title("I. ពីជគណិត និងការគណនា៖");
                exam.setSection1Instruction("ចូរគណនា រួចជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection1MaxPoints(15);
                exam.setSection2Title("II. ធរណីមាត្រ និងរង្វាស់រង្វាល់៖");
                exam.setSection2Instruction("ចូរដោះស្រាយ រួចជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection2MaxPoints(15);
                exam.setSection3Title("III. សមាមាត្រ ស្ថិតិ និងប្រូបាប៊ីលីតេ៖");
                exam.setSection3Instruction("ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection3MaxPoints(15);
                exam.setSection4Title("IV. លំហាត់ដោះស្រាយលម្អិត និងចំណោទ៖");
                exam.setSection4Instruction("ចូរសរសេររបៀបដោះស្រាយលម្អិតជាជំហានៗសម្រាប់ចំណោទខាងក្រោម៖ (២៥ ពិន្ទុ)");
                exam.setWritingPrompt(
                        "១. (១២ ពិន្ទុ) ដោះស្រាយសមីការ និងវិសមីការខាងក្រោម៖\n" +
                        "   ក) 3(2x - 4) + 5 = 2x + 9\n" +
                        "   ខ) 4x - 7 ≤ 2x + 5\n\n" +
                        "២. (១៣ ពិន្ទុ) ដីស្រែរាងចតុកោណកែងមួយមានបណ្តោយវែងជាងទទឹង 8m និងមានបរិមាត្រសរុប 64m។\n" +
                        "   ក) ចូររករង្វាស់ទទឹង និងបណ្តោយនៃដីស្រែនេះ។\n" +
                        "   ខ) គណនាផ្ទៃក្រឡារបស់ដីស្រែជាម៉ែត្រការ៉េ (m²)។"
                );
                exam.setWritingMaxPoints(25);
                exam = examRepository.save(exam);

                String[][] sampleQuestions = {
                    // Section I: Algebra (math_algebra)
                    {"math_algebra", "១. ចូរដោះស្រាយសមីការ 2x + 6 = 14 ។ តើ x មានតម្លៃស្មើប៉ុន្មាន?", "x = 2", "x = 4", "x = 6", "x = 10", "B"},
                    {"math_algebra", "២. តើកន្សោម (x + 3)² ពន្លាតបានដូចម្តេច?", "x² + 9", "x² + 3x + 9", "x² + 6x + 9", "x² + 6x + 6", "C"},
                    {"math_algebra", "៣. ចូរគណនាតម្លៃ √81 + √16 ៖", "11", "13", "15", "97", "B"},
                    {"math_algebra", "៤. ដាក់កន្សោម x² - 25 ជាផលគុណកត្តា៖", "(x - 5)(x + 5)", "(x - 5)²", "(x + 5)²", "(x - 25)(x + 1)", "A"},
                    {"math_algebra", "៥. បើ a = 3 និង b = -2 តើតម្លៃនៃកន្សោម 2a² - 3b ស្មើប៉ុន្មាន?", "12", "24", "18", "6", "B"},
                    // Section II: Geometry (math_geometry)
                    {"math_geometry", "១. ត្រីកោណកែងមួយមានជ្រុងជាប់មុំកែង 6cm និង 8cm។ តើអ៊ីប៉ូតេនុសមានប្រវែងប៉ុន្មាន?", "10 cm", "12 cm", "14 cm", "48 cm", "A"},
                    {"math_geometry", "២. ផលបូកមុំក្នុងទាំងបីនៃត្រីកោណមួយស្មើនឹងប៉ុន្មានដឺក្រេ?", "90°", "180°", "270°", "360°", "B"},
                    {"math_geometry", "៣. ចតុកោណកែងមួយមានបណ្តោយ 12cm និងទទឹង 5cm។ តើផ្ទៃក្រឡារបស់វាស្មើប៉ុន្មាន?", "17 cm²", "34 cm²", "60 cm²", "30 cm²", "C"},
                    {"math_geometry", "៤. រង្វង់មួយមានកាំ R = 7cm (យក π ≈ 22/7)។ តើបរិមាត្ររង្វង់ស្មើប៉ុន្មាន?", "22 cm", "44 cm", "154 cm", "14 cm", "B"},
                    {"math_geometry", "៥. ប្រសិនបើមុំពីរបំពេញគ្នា (មុំបន្ថែម) ហើយមុំមួយស្មើ 65° តើមុំមួយទៀតស្មើប៉ុន្មាន?", "25°", "35°", "115°", "125°", "A"},
                    // Section III: Functions, Statistics & Applied Math (math_applied)
                    {"math_applied", "១. មធ្យមភាគនព្វន្តនៃទិន្នន័យពិន្ទុ 6, 8, 10, 12, 14 គឺ៖", "8", "10", "12", "50", "B"},
                    {"math_applied", "២. សៀវភៅ ៤ ក្បាលថ្លៃ 12,000 រៀល។ តើសៀវភៅ ៧ ក្បាលថ្លៃប៉ុន្មានរៀល?", "18,000 រៀល", "21,000 រៀល", "24,000 រៀល", "28,000 រៀល", "B"},
                    {"math_applied", "៣. បោះគ្រាប់ឡុកឡាក់សមមួយ ១ ដង។ តើប្រូបាប៊ីលីតេដែលចេញលេខគូស្មើប៉ុន្មាន?", "1/6", "1/3", "1/2", "2/3", "C"},
                    {"math_applied", "៤. អនុគមន៍លីនេអ៊ែរ f(x) = 3x - 2 ។ តើ f(4) មានតម្លៃស្មើប៉ុន្មាន?", "10", "12", "14", "5", "A"},
                    {"math_applied", "៥. ទំនិញមួយតម្លៃ 50,000 រៀល ត្រូវបានបញ្ចុះតម្លៃ 20%។ តើត្រូវបង់ប្រាក់ប៉ុន្មាន?", "10,000 រៀល", "30,000 រៀល", "40,000 រៀល", "45,000 រៀល", "C"}
                };
                for (String[] row : sampleQuestions) {
                    Question q = new Question();
                    q.setExamId(exam.getId());
                    q.setAccountId(acc.getId());
                    q.setSkillType(row[0]);
                    q.setQuestionText(row[1]);
                    q.setOptionA(row[2]);
                    q.setOptionB(row[3]);
                    q.setOptionC(row[4]);
                    q.setOptionD(row[5]);
                    q.setCorrectOption(row[6]);
                    questionRepository.save(q);
                }
                System.out.println(">>> [DataInitializer] Seeded starter Standard MoEYS Math Exam for Math teacher: " + acc.getFullName() + " (ID " + acc.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding Math exam: " + e.getMessage());
        }
    }

    private void seedPhysicsExamIfMissing(
            AccountRepository accountRepository,
            ClassroomRepository classroomRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        try {
            List<Account> allAccounts = accountRepository.findAll();
            for (Account acc : allAccounts) {
                String subj = acc.getSubject();
                boolean isDemo = Long.valueOf(98L).equals(acc.getId());
                if (!isDemo && (subj == null || (!subj.contains("រូប") && !subj.toLowerCase().contains("phys")))) {
                    continue;
                }

                // STRICT ZERO DATA LOSS: Never touch or overwrite if this teacher already has any Physics exam
                boolean hasAnyPhysicsExam = examRepository.findByAccountId(acc.getId()).stream()
                        .anyMatch(e -> "រូបវិទ្យា".equals(e.getSubjectName()));
                if (hasAnyPhysicsExam) {
                    continue;
                }

                List<Classroom> teacherClasses = classroomRepository.findByAccountId(acc.getId());
                String classNames = teacherClasses.stream()
                        .map(Classroom::getClassName)
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(java.util.stream.Collectors.joining(", "));
                if (classNames.isEmpty()) {
                    classNames = (acc.getTeachingClasses() != null && !acc.getTeachingClasses().trim().isEmpty())
                            ? acc.getTeachingClasses().trim()
                            : "៨D";
                }

                Exam exam = new Exam();
                exam.setAccountId(acc.getId());
                exam.setMonth("ខែវិច្ឆិកា ២០២៦");
                exam.setSubjectName("រូបវិទ្យា");
                exam.setClassName(classNames);
                exam.setOpenClasses(""); // Closed by default until teacher opens for class
                exam.setReadingPassage(
                        "រូបមន្ត និងតម្លៃថេររូបវិទ្យាជំនួយ៖\n" +
                        "• សំទុះទំនាញផែនដី៖ g = 10 m/s²  |  ល្បឿនពន្លឺក្នុងសុញ្ញាកាស៖ c = 3 × 10⁸ m/s\n" +
                        "• មេកានិច៖ P = m·g  |  p = F / S  |  v = d / t  |  Ec = ½mv²  |  Ep = mgh\n" +
                        "• អគ្គិសនី៖ ច្បាប់អូម U = R·I  |  អានុភាព P = U·I  |  ថាមពល E = P·t"
                );
                exam.setSection1Title("I. មេកានិច និងថាមពល៖");
                exam.setSection1Instruction("ចូរគណនា និងជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection1MaxPoints(15);
                exam.setSection2Title("II. ទែម៉ូឌីណាមិក រលក និងអុបទិក៖");
                exam.setSection2Instruction("ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection2MaxPoints(15);
                exam.setSection3Title("III. អគ្គិសនី និងអេឡិចត្រូម៉ាញេទិក៖");
                exam.setSection3Instruction("ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)");
                exam.setSection3MaxPoints(15);
                exam.setSection4Title("IV. លំហាត់អនុវត្ត និងដោះស្រាយលម្អិត៖");
                exam.setSection4Instruction("ចូរសរសេររបៀបដោះស្រាយលម្អិត (រូបមន្ត ការជំនួសលេខ និងខ្នាតត្រឹមត្រូវ)៖ (២៥ ពិន្ទុ)");
                exam.setWritingPrompt(
                        "១. (១២ ពិន្ទុ) ម៉ូតូមួយគ្រឿងបើកបរក្នុងល្បឿនថេរ 72 km/h លើផ្លូវត្រង់អស់រយៈពេល 15 នាទី។\n" +
                        "   ក) ចូរបំប្លែងល្បឿនពី km/h ទៅជា m/s។\n" +
                        "   ខ) គណនាចម្ងាយចរសរុបរបស់ម៉ូតូគិតជាម៉ែត្រ (m) និងគីឡូម៉ែត្រ (km)។\n\n" +
                        "២. (១៣ ពិន្ទុ) រេស៊ីស្តង់មួយមានតម្លៃ R = 20 Ω ត្រូវបានតភ្ជាប់ជាមួយប្រភពតង់ស្យុង U = 12 V ។\n" +
                        "   ក) គណនាអាំងតង់ស៊ីតេចរន្តអគ្គិសនី I ដែលឆ្លងកាត់រេស៊ីស្តង់។\n" +
                        "   ខ) គណនាអានុភាពអគ្គិសនី P និងថាមពលអគ្គិសនី E ដែលប្រើប្រាស់ក្នុងរយៈពេល 10 នាទី។"
                );
                exam.setWritingMaxPoints(25);
                exam = examRepository.save(exam);

                String[][] sampleQuestions = {
                    // Section I: Mechanics (phys_mechanics)
                    {"phys_mechanics", "១. ក្នុងប្រព័ន្ធខ្នាតអន្តរជាតិ (SI) តើខ្នាតរបស់ «កម្លាំង» គឺអ្វី?", "ស៊ូល (J)", "ញូតុន (N)", "វ៉ាត់ (W)", "ប៉ាស្កាល់ (Pa)", "B"},
                    {"phys_mechanics", "២. អង្គធាតុមួយមានម៉ាស់ m = 5 kg នៅទីកន្លែងដែល g = 10 N/kg។ តើទម្ងន់របស់វាស្មើប៉ុន្មាន?", "0.5 N", "5 N", "15 N", "50 N", "D"},
                    {"phys_mechanics", "៣. រថយន្តមួយបើកបរបានចម្ងាយ 120 km ក្នុងរយៈពេល 2 ម៉ោង។ តើល្បឿនមធ្យមរបស់វាស្មើប៉ុន្មាន?", "40 km/h", "60 km/h", "120 km/h", "240 km/h", "B"},
                    {"phys_mechanics", "៤. កម្លាំង F = 40 N អំពើលើអង្គធាតុមួយឱ្យផ្លាស់ទីតាមទិសកម្លាំងបានចម្ងាយ d = 5 m។ តើកម្មន្ត W ស្មើប៉ុន្មាន?", "8 J", "45 J", "200 J", "400 J", "C"},
                    {"phys_mechanics", "៥. អង្គធាតុមួយមានម៉ាស់ 2 kg កំពុងផ្លាស់ទីដោយល្បឿន 4 m/s។ តើថាមពលស៊ីនេទិច Ec ស្មើប៉ុន្មាន?", "8 J", "16 J", "32 J", "4 J", "B"},
                    // Section II: Thermodynamics, Waves & Optics (phys_optics)
                    {"phys_optics", "១. សីតុណ្ហភាព 27°C ត្រូវនឹងប៉ុន្មានកែលវិន (K)?", "246 K", "273 K", "300 K", "327 K", "C"},
                    {"phys_optics", "២. ទឹកសុទ្ធពុះនៅសីតុណ្ហភាពប៉ុន្មានក្រោមសម្ពាធបរិយាកាសធម្មតា?", "0°C", "50°C", "100°C", "212°C", "C"},
                    {"phys_optics", "៣. តាមច្បាប់ចំណាំងផ្លាតពន្លឺ ប្រសិនបើមុំប៉ះ i = 35° តើមុំចំណាំងផ្លាត r ស្មើប៉ុន្មាន?", "35°", "55°", "70°", "90°", "A"},
                    {"phys_optics", "៤. រលកសំឡេងមិនអាចដាលឆ្លងកាត់មជ្ឈដ្ឋានមួយណាបានទេ?", "ដែក", "ទឹក", "ខ្យល់", "សុញ្ញាកាស (ទីទំនេរ)", "D"},
                    {"phys_optics", "៥. រលកមួយមានប្រេកង់ f = 50 Hz។ តើខួប T របស់រលកស្មើប៉ុន្មាន?", "0.02 s", "0.2 s", "2 s", "50 s", "A"},
                    // Section III: Electricity & Magnetism (phys_electricity)
                    {"phys_electricity", "១. ខ្នាតរបស់ «រេស៊ីស្តង់អគ្គិសនី» គឺអ្វី?", "អំពែ (A)", "វ៉ុល (V)", "អូម (Ω)", "គូឡុំ (C)", "C"},
                    {"phys_electricity", "២. បើតង់ស្យុង U = 12 V និងរេស៊ីស្តង់ R = 4 Ω តើអាំងតង់ស៊ីតេចរន្ត I ស្មើប៉ុន្មាន?", "3 A", "8 A", "16 A", "48 A", "A"},
                    {"phys_electricity", "៣. អំពូលភ្លើងមួយមានអានុភាព P = 60 W ប្រើតង់ស្យុង 220 V។ តើថាមពលប្រើក្នុង 10 s ស្មើប៉ុន្មាន?", "6 J", "60 J", "600 J", "2200 J", "C"},
                    {"phys_electricity", "៤. មេដែកមួយមានប៉ូលប៉ុន្មាន?", "១ ប៉ូល", "២ ប៉ូល (ជើង និងត្បូង)", "៣ ប៉ូល", "៤ ប៉ូល", "B"},
                    {"phys_electricity", "៥. បង្គុំរេស៊ីស្តង់ R₁ = 6 Ω និង R₂ = 4 Ω តជាស៊េរី មានរេស៊ីស្តង់សមមូល Req ស្មើប៉ុន្មាន?", "2.4 Ω", "10 Ω", "24 Ω", "1.5 Ω", "B"}
                };
                for (String[] row : sampleQuestions) {
                    Question q = new Question();
                    q.setExamId(exam.getId());
                    q.setAccountId(acc.getId());
                    q.setSkillType(row[0]);
                    q.setQuestionText(row[1]);
                    q.setOptionA(row[2]);
                    q.setOptionB(row[3]);
                    q.setOptionC(row[4]);
                    q.setOptionD(row[5]);
                    q.setCorrectOption(row[6]);
                    questionRepository.save(q);
                }
                System.out.println(">>> [DataInitializer] Seeded starter Standard MoEYS Physics Exam for Physics teacher: " + acc.getFullName() + " (ID " + acc.getId() + ")");
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding Physics exam: " + e.getMessage());
        }
    }

    private record StandardExamSpec(
            String subjectName,
            String subjectKeyword,
            String formulaNote,
            String sec1Title,
            String sec1Instruction,
            String sec2Title,
            String sec2Instruction,
            String sec3Title,
            String sec3Instruction,
            String sec4Title,
            String sec4Instruction,
            String writingPrompt,
            String[][] questions) {}

    private void seedRemainingStandardSubjectExamsIfMissing(
            AccountRepository accountRepository,
            ClassroomRepository classroomRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository) {
        List<StandardExamSpec> specs = List.of(
            new StandardExamSpec(
                "គីមីវិទ្យា", "គីមី",
                "ទិន្នន័យម៉ាសអាតូមជំនួយ៖ H = 1, C = 12, N = 14, O = 16, Na = 23, Mg = 24, S = 32, Cl = 35.5, Ca = 40, Fe = 56",
                "I. គីមីទូទៅ និងរូបមន្តម៉ូលេគុល៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. សមីការប្រតិកម្ម និងគណនាម៉ូល៖", "ចូរគណនា រួចជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. អាស៊ីត-បាស អំបិល និងគីមីសរីរាង្គ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. លំហាត់គណនា និងសរសេរសមីការប្រតិកម្ម៖", "ចូរសរសេរសមីការតុល្យការ និងដោះស្រាយលម្អិតជាជំហានៗ៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ចូរថ្លឹងសមីការប្រតិកម្មគីមីខាងក្រោម៖\n   ក) Fe + HCl → FeCl₂ + H₂↑\n   ខ) CaCO₃ + HCl → CaCl₂ + CO₂↑ + H₂O\n\n២. (១៣ ពិន្ទុ) គេដាក់លោហៈម៉ាញេស្យូម (Mg) ចំនួន 4.8 g ឱ្យមានប្រតិកម្មជាមួយសូលុយស្យុងអាស៊ីតក្លរីឌ្រិច (HCl) លើស។\n   ក) គណនាចំនួនម៉ូលរបស់ម៉ាញេស្យូម (n)។\n   ខ) គណនាមាឌឧស្ម័នអ៊ីដ្រូសែន (H₂) ដែលភាយឡើងនៅលក្ខខណ្ឌធម្មតា (Vm = 22.4 L/mol)។",
                new String[][]{
                    {"chem_sec1", "១. តើនិមិត្តសញ្ញាគីមីរបស់ធាតុ «សូដ្យូម» គឺអ្វី?", "S", "So", "Na", "K", "C"},
                    {"chem_sec1", "២. តើរូបមន្តម៉ូលេគុលនៃទឹកសុទ្ធគឺអ្វី?", "H₂O₂", "H₂O", "HO", "H₃O", "B"},
                    {"chem_sec1", "៣. គណនាម៉ាសម៉ូលេគុលនៃកាបូនឌីអុកស៊ីត (CO₂)៖ (C=12, O=16)", "28 g/mol", "32 g/mol", "44 g/mol", "56 g/mol", "C"},
                    {"chem_sec1", "៤. ក្នុងអាតូម តើភាគល្អិតណាមានបន្ទុកអគ្គិសនីវិជ្ជមាន (+)?", "អេឡិចត្រុង", "ណឺត្រុង", "ប្រូតុង", "អ៊ីយ៉ុងអវិជ្ជមាន", "C"},
                    {"chem_sec1", "៥. តើឧស្ម័នណាមួយមានចំណែកច្រើនជាងគេក្នុងខ្យល់ (~78%)?", "អុកស៊ីសែន (O₂)", "អាសូត (N₂)", "កាបូនឌីអុកស៊ីត (CO₂)", "អ៊ីដ្រូសែន (H₂)", "B"},
                    {"chem_sec2", "១. តើចំនួនម៉ូល n នៃកាបូន (C) ចំនួន 24 g ស្មើប៉ុន្មានម៉ូល? (C=12)", "1 mol", "2 mol", "3 mol", "4 mol", "B"},
                    {"chem_sec2", "២. ក្នុងសមីការតុល្យការ 2H₂ + O₂ → 2H₂O តើត្រូវការ O₂ ប៉ុន្មានម៉ូលដើម្បីបង្កើតបានទឹក 4 mol?", "1 mol", "2 mol", "3 mol", "4 mol", "B"},
                    {"chem_sec2", "៣. មាឌម៉ូលនៃឧស្ម័ននៅលក្ខខណ្ឌធម្មតា (0°C, 1 atm) ស្មើប៉ុន្មាន?", "11.2 L/mol", "22.4 L/mol", "24.0 L/mol", "44.8 L/mol", "B"},
                    {"chem_sec2", "៤. កំហាប់ជាម៉ូលនៃសូលុយស្យុងដែលមានធាតុរលាយ 0.5 mol ក្នុងមាឌ 2 L គឺ៖", "0.25 mol/L", "0.5 mol/L", "1.0 mol/L", "2.5 mol/L", "A"},
                    {"chem_sec2", "៥. ម៉ាសម៉ូលនៃ CaCO₃ ស្មើប៉ុន្មាន? (Ca=40, C=12, O=16)", "68 g/mol", "84 g/mol", "100 g/mol", "116 g/mol", "C"},
                    {"chem_sec3", "១. សូលុយស្យុងដែលមានតម្លៃ pH < 7 មានលក្ខណៈជាអ្វី?", "បាស", "ណឺត", "អាស៊ីត", "អំបិល", "C"},
                    {"chem_sec3", "២. ប្រតិកម្មរវាងអាស៊ីត និងបាសបង្កើតបានជាអ្វី?", "អំបិល និងទឹក", "អុកស៊ីត និងឧស្ម័ន", "លោហៈ និងទឹក", "អាស៊ីតថ្មី", "A"},
                    {"chem_sec3", "៣. តើកន្សោមណាមួយជារូបមន្តទូទៅរបស់អាល់កាន (Alkane)?", "CₙH₂ₙ", "CₙH₂ₙ₊₂", "CₙH₂ₙ₋₂", "CₙHₙ", "B"},
                    {"chem_sec3", "៤. ក្រដាសពណ៌លីតមុសប្រែពីក្រហមទៅខៀវ នៅពេលជ្រលក់ក្នុងសូលុយស្យុងអ្វី?", "ទឹកខ្មេះ", "សូលុយស្យុង HCl", "សូលុយស្យុង NaOH (បាស)", "ទឹកសុទ្ធ", "C"},
                    {"chem_sec3", "៥. មេតាន (CH₄) ជាសមាសធាតុសំខាន់ក្នុងឧស្ម័នអ្វី?", "ឧស្ម័នធម្មជាតិ / ជីវឧស្ម័ន", "អុកស៊ីសែន", "ក្លរ", "អាម៉ូញាក់", "A"}
                }
            ),
            new StandardExamSpec(
                "ជីវវិទ្យា", "ជីវ",
                "ទិន្នន័យជំនួយ៖ កោសិកាជាឯកតាគ្រឹះនៃភាវៈរស់ | គោលការណ៍បំពេញបាស ADN: A = T, G = C | សមីការរស្មីសំយោគ៖ 6CO₂ + 6H₂O → C₆H₁₂O₆ + 6O₂",
                "I. កោសិកា និងរចនាសម្ព័ន្ធសារពាង្គកាយ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. សរីរវិទ្យា និងបរិស្ថានវិទ្យា៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. ពន្ធុវិទ្យា (ADN/ARN) និងការបន្តពូជ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. សំណួរវិភាគ និងដោះស្រាយលម្អិត៖", "ចូរពន្យល់ និងគណនាលម្អិតលើសំណួរខាងក្រោម៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ចូរប្រៀបធៀបភាពខុសគ្នាសំខាន់ៗ ៣ ចំណុចរវាង «កោសិការុក្ខជាតិ» និង «កោសិកាសត្វ»។\n\n២. (១៣ ពិន្ទុ) អង្កត់ ADN មួយមានប្រវែង ២ ច្រវាក់សរុប 2,400 នុយក្លេអូទីត ដែលក្នុងនោះមាននុយក្លេអូទីតប្រភេទ A = 500 ។ ចូរគណនាចំនួននុយក្លេអូទីតប្រភេទ T, G និង C នៃ ADN នេះ។",
                new String[][]{
                    {"bio_sec1", "១. តើធាតុកោសិកាណាដែលជា «មជ្ឈមណ្ឌលផលិតថាមពល» (ATP) របស់កោសិកា?", "រីបូសូម", "មីតូកុងឌ្រី", "លីសូសូម", "ណ្វៃយ៉ូ", "B"},
                    {"bio_sec1", "២. តើធាតុកោសិកាណាមួយមានតែក្នុងកោសិការុក្ខជាតិ ប៉ុន្តែគ្មានក្នុងកោសិកាសត្វទេ?", "ភ្នាសកោសិកា", "ណ្វៃយ៉ូ", "ក្លរ៉ូប្លាស និងភ្នាសគ្រោងសែលុយឡូស", "ស៊ីតូប្លាស", "C"},
                    {"bio_sec1", "៣. ជាលិកាអ្វីក្នុងរុក្ខជាតិដែលដឹកនាំទឹក និងអំបិលខនិជពីឫសទៅស្លឹក?", "ផ្លូអែម (Phloem)", "ស៊ីឡែម (Xylem)", "អេពីឌែម", "ប៉ារ៉ង់ស៊ីម", "B"},
                    {"bio_sec1", "៤. មុខងារចម្បងរបស់កោសិកាឈាមក្រហមគឺ៖", "កំចាត់មេរោគ", "ដឹកនាំអុកស៊ីសែន (O₂)", "ធ្វើឱ្យឈាមកក", "ផលិតអង់ទីករ", "B"},
                    {"bio_sec1", "៥. ប្រភេទជាលិកាណាដែលមានសមត្ថភាពកន្ត្រាក់ដើម្បីបង្កើតចលនា?", "ជាលិកាសាច់ដុំ", "ជាលិកាសរសៃប្រសាទ", "ជាលិកាឈាម", "ជាលិកាឆ្អឹង", "A"},
                    {"bio_sec2", "១. រុក្ខជាតិបៃតងផលិតអាហារតាមលំនាំអ្វីដោយប្រើពន្លឺព្រះអាទិត្យ?", "ដង្ហើម", "រស្មីសំយោគ (Photosynthesis)", "ការរំលាយអាហារ", "ការបន្សុទ្ធឈាម", "B"},
                    {"bio_sec2", "២. ក្នុងប្រព័ន្ធដង្ហើមមនុស្ស ការដោះដូរឧស្ម័ន O₂ និង CO₂ កើតឡើងនៅទីណា?", "បំពង់ខ្យល់", "ថង់សួត (Alveoli)", "បំពង់អាហារ", "បេះដូង", "B"},
                    {"bio_sec2", "៣. ក្នុងខ្សែសង្វាក់អាហារ តើរុក្ខជាតិបៃតងមានតួនាទីជាអ្វី?", "អ្នកផលិត (Producers)", "អ្នកប្រើប្រាស់ទី១", "អ្នកប្រើប្រាស់ទី២", "អ្នកបំបែក (Decomposers)", "A"},
                    {"bio_sec2", "៤. អ័រម៉ូន «អាំងស៊ុយលីន (Insulin)» ផលិតដោយលំពែងជួយគ្រប់គ្រងអ្វី?", "សម្ពាធឈាម", "កម្រិតជាតិស្ករក្នុងឈាម", "ការលូតលាស់ឆ្អឹង", "សីតុណ្ហភាពរាងកាយ", "B"},
                    {"bio_sec2", "៥. បេះដូងមនុស្សមានប៉ុន្មានថត?", "២ ថត", "៣ ថត", "៤ ថត (អូរីគុល ២ និងវ៉ង់ទ្រីគុល ២)", "៥ ថត", "C"},
                    {"bio_sec3", "១. តើបាសអាសូត ៤ ប្រភេទក្នុងម៉ូលេគុល ADN គឺអ្វីខ្លះ?", "A, U, G, C", "A, T, G, C", "A, B, C, D", "T, U, G, C", "B"},
                    {"bio_sec3", "២. តើបាសអាសូតប្រភេទណាដែលមានក្នុង ARN ប៉ុន្តែគ្មានក្នុង ADN?", "អាដេនីន (A)", "ទីមីន (T)", "អ៊ុយរ៉ាស៊ីល (U)", "ក្វានីន (G)", "C"},
                    {"bio_sec3", "៣. ចំណែកកោសិកា «មីតូស (Mitosis)» បង្កើតបានកោសិកាកូនប៉ុន្មានដែលមានក្រូម៉ូសូមដូចកោសិកាមេ?", "១ កោសិកា", "២ កោសិកា", "៤ កោសិកា", "៨ កោសិកា", "B"},
                    {"bio_sec3", "៤. មនុស្សធម្មតាមានក្រូម៉ូសូមចំនួនប៉ុន្មានក្នុងកោសិកាលូតលាស់?", "២៣ ក្រូម៉ូសូម", "៤៦ ក្រូម៉ូសូម (២៣ គូ)", "៤៨ ក្រូម៉ូសូម", "២២ គូ", "B"},
                    {"bio_sec3", "៥. លោក ហ្គ្រេហ្គ័រ ម៉ង់ដែល (Gregor Mendel) ត្រូវបានគេស្គាល់ថាជាបិតានៃវិទ្យាសាស្ត្រអ្វី?", "មីក្រូជីវវិទ្យា", "ពន្ធុវិទ្យា (Genetics)", "បរិស្ថានវិទ្យា", "កាយវិភាគវិទ្យា", "B"}
                }
            ),
            new StandardExamSpec(
                "ប្រវត្តិវិទ្យា", "ប្រវត្តិ",
                "ទិន្នន័យសង្ខេបប្រវត្តិសាស្ត្រ៖ សម័យអង្គរ (៨០២-១៤៣១) | ទិវាបុណ្យឯករាជ្យជាតិ (៩ វិច្ឆិកា ១៩៥៣) | ថ្ងៃបង្កើតអង្គការសហប្រជាជាតិ (២៤ តុលា ១៩៤៥)",
                "I. ប្រវត្តិសាស្ត្រកម្ពុជា៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. ប្រវត្តិសាស្ត្រពិភពលោក និងតំបន់៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. កាលបរិច្ឆេទ ព្រឹត្តិការណ៍ និងអង្គការអន្តរជាតិ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. សំណួរពិភាក្សា និងវិភាគប្រវត្តិសាស្ត្រ៖", "ចូរសរសេរចម្លើយពន្យល់ និងវិភាគឱ្យបានក្បោះក្បាយ៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) តើកត្តាសំខាន់ៗអ្វីខ្លះដែលធ្វើឱ្យចក្រភពអង្គរមានភាពរុងរឿងដល់កំពូលក្នុងរជ្ជកាលព្រះបាទជ័យវរ្ម័នទី៧?\n\n២. (១៣ ពិន្ទុ) ចូររៀបរាប់ពីព្រះរាជបូជនីយកិច្ចទាមទារឯករាជ្យជាតិរបស់ព្រះបាទសម្តេចព្រះនរោត្តម សីហនុ រហូតទទួលបានជោគជ័យនៅថ្ងៃទី ៩ វិច្ឆិកា ១៩៥៣។",
                new String[][]{
                    {"hist_sec1", "១. តើព្រះមហាក្សត្រខ្មែរអង្គណាដែលបានស្ថាបនាសម័យអង្គរនៅឆ្នាំ ៨០២ លើភ្នំគូលែន?", "ព្រះបាទយសោវរ្ម័នទី១", "ព្រះបាទជ័យវរ្ម័នទី២", "ព្រះបាទសូរ្យវរ្ម័នទី២", "ព្រះបាទជ័យវរ្ម័នទី៧", "B"},
                    {"hist_sec1", "២. ប្រាសាទអង្គរវត្តត្រូវបានកសាងឡើងក្នុងរជ្ជកាលព្រះមហាក្សត្រអង្គណា?", "ព្រះបាទសូរ្យវរ្ម័នទី២", "ព្រះបាទឥន្ទ្រវរ្ម័នទី១", "ព្រះបាទជ័យវរ្ម័នទី៧", "ព្រះបាទឧទយាទិត្យវរ្ម័នទី២", "A"},
                    {"hist_sec1", "៣. ប្រាសាទបាយ័ន ប្រាសាទតាព្រហ្ម និងមន្ទីរពេទ្យ ១០២ ត្រូវបានស្ថាបនាដោយ៖", "ព្រះបាទសូរ្យវរ្ម័នទី២", "ព្រះបាទជ័យវរ្ម័នទី៧", "ព្រះបាទត្រសក់ផ្អែម", "ព្រះបាទនរោត្តម", "B"},
                    {"hist_sec1", "៤. ប្រទេសកម្ពុជាទទួលបានឯករាជ្យពេញលេញពីអាណានិគមបារាំងនៅថ្ងៃខែឆ្នាំណា?", "២៣ តុលា ១៩៩១", "៩ វិច្ឆិកា ១៩៥៣", "៧ មករា ១៩៧៩", "២៤ កញ្ញា ១៩៩៣", "B"},
                    {"hist_sec1", "៥. រាជធានីដំបូងគេបង្អស់របស់កម្ពុជានៅសម័យនគរភ្នំ (ហ្វូណន) មានឈ្មោះអ្វី?", "អង្គរធំ", "វ្យាធបុរៈ (ឬ បាភ្នំ)", "លង្វែក", "ឧត្តុង្គ", "B"},
                    {"hist_sec2", "១. សង្គ្រាមលោកលើកទី១ បានផ្ទុះឡើង និងបញ្ចប់នៅចន្លោះឆ្នាំណា?", "១៩១៤-១៩១៨", "១៩៣៩-១៩៤៥", "១៩៥០-១៩៥៣", "១៩៤៥-១៩៧៥", "A"},
                    {"hist_sec2", "២. សង្គ្រាមលោកលើកទី២ បានបញ្ចប់ជាផ្លូវការនៅឆ្នាំណា?", "១៩១៨", "១៩៣៩", "១៩៤៥", "១៩៥៤", "C"},
                    {"hist_sec2", "៣. បដិវត្តន៍ឧស្សាហកម្មដំបូងគេបានចាប់ផ្តើមឡើងនៅប្រទេសណា?", "បារាំង", "អាល្លឺម៉ង់", "អង់គ្លេស (ចក្រភពអង់គ្លេស)", "សហរដ្ឋអាមេរិក", "C"},
                    {"hist_sec2", "៤. អរិយធម៌បុរាណមេសូប៉ូតាមី បានកកើតឡើងនៅតាមដងទន្លេណា?", "ទន្លេនីល", "ទន្លេទីគ្រីស និងអឺប្រាត", "ទន្លេគង្គា", "ទន្លេលឿង", "B"},
                    {"hist_sec2", "៥. ជញ្ជាំងប៊ែរឡាំង (Berlin Wall) បានដួលរលំនៅឆ្នាំណា ដែលជាសញ្ញាបញ្ចប់សង្គ្រាមត្រជាក់?", "១៩៧៩", "១៩៨៩", "១៩៩១", "១៩៩៩", "B"},
                    {"hist_sec3", "១. អង្គការសហប្រជាជាតិ (UN) ត្រូវបានបង្កើតឡើងជាផ្លូវការនៅថ្ងៃខែឆ្នាំណា?", "២៤ តុលា ១៩៤៥", "៨ សីហា ១៩៦៧", "១០ ធ្នូ ១៩៤៨", "៩ វិច្ឆិកា ១៩៥៣", "A"},
                    {"hist_sec3", "២. ប្រទេសកម្ពុជាបានចូលជាសមាជិកពេញសិទ្ធិទី ១០ នៃអាស៊ាន (ASEAN) នៅថ្ងៃខែឆ្នាំណា?", "២៣ តុលា ១៩៩១", "៣០ មេសា ១៩៩៩", "១៤ ធ្នូ ១៩៥៥", "៧ កក្កដា ២០០៨", "B"},
                    {"hist_sec3", "៣. កិច្ចព្រមព្រៀងសន្តិភាពទីក្រុងប៉ារីសស្តីពីកម្ពុជា ត្រូវបានចុះហត្ថលេខានៅថ្ងៃណា?", "២៣ តុលា ១៩៩១", "២១ កញ្ញា ១៩៩៣", "៩ វិច្ឆិកា ១៩៥៣", "៣០ មេសា ១៩៩៩", "A"},
                    {"hist_sec3", "៤. តើប្រាសាទព្រះវិហារត្រូវបានចុះក្នុងបញ្ជីបេតិកភណ្ឌពិភពលោករបស់ UNESCO នៅថ្ងៃណា?", "១៤ ធ្នូ ១៩៩២", "៧ កក្កដា ២០០៨", "៨ កក្កដា ២០១៧", "១៧ កញ្ញា ២០២៣", "B"},
                    {"hist_sec3", "៥. ថ្ងៃទី ៧ ខែមករា ឆ្នាំ ១៩៧៩ គឺជាទិវាប្រវត្តិសាស្ត្រអ្វីរបស់កម្ពុជា?", "ទិវាឯករាជ្យជាតិ", "ទិវាជ័យជម្នះលើរបបប្រល័យពូជសាសន៍", "ទិវាប្រកាសរដ្ឋធម្មនុញ្ញ", "ទិវាបង្កើតអង្គរ", "B"}
                }
            ),
            new StandardExamSpec(
                "ភូមិវិទ្យា", "ភូមិ",
                "ទិន្នន័យភូមិសាស្ត្រកម្ពុជា៖ ផ្ទៃដីសរុប 181,035 km² | ចំនួនរាជធានី-ខេត្ត៖ ២៥ (១ រាជធានី + ២៤ ខេត្ត) | ភ្នំខ្ពស់ជាងគេ៖ ភ្នំឱរ៉ាល់ (1,813 m)",
                "I. ភូមិវិទ្យារូប និងអាកាសធាតុ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. ភូមិវិទ្យាកម្ពុជា និងតំបន់អាស៊ី៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. សេដ្ឋកិច្ច ប្រជាសាស្ត្រ និងផែនទី៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. សំណួរវិភាគភូមិសាស្ត្រ និងសេដ្ឋកិច្ច៖", "ចូររៀបរាប់ និងវិភាគសំណួរខាងក្រោមឱ្យបានច្បាស់លាស់៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ចូររៀបរាប់ពីសារៈសំខាន់ ៤ យ៉ាងនៃបឹងទន្លេសាបចំពោះសេដ្ឋកិច្ច សង្គម និងជីវចម្រុះនៅកម្ពុជា។\n\n២. (១៣ ពិន្ទុ) ដង់ស៊ីតេប្រជាជនគណនាដោយរូបមន្ត៖ ដង់ស៊ីតេ = ចំនួនប្រជាជន / ផ្ទៃដី (km²)។\n   បើខេត្តមួយមានផ្ទៃដី 5,000 km² និងមានប្រជាជនរស់នៅ 750,000 នាក់ ចូរគណនាដង់ស៊ីតេប្រជាជនក្នុងខេត្តនេះ និងពន្យល់ពីកត្តាដែលនាំឱ្យតំបន់វាលទំនាបមានប្រជាជនរស់នៅកុះករ។",
                new String[][]{
                    {"geo_sec1", "១. ភ្នំដែលខ្ពស់ជាងគេបំផុតនៅក្នុងប្រទេសកម្ពុជាមានឈ្មោះអ្វី?", "ភ្នំគូលែន", "ភ្នំឱរ៉ាល់ (១,៨១៣ ម៉ែត្រ)", "ភ្នំបូកគោ", "ភ្នំដងរែក", "B"},
                    {"geo_sec1", "២. ប្រទេសកម្ពុជាស្ថិតនៅក្នុងតំបន់អាកាសធាតុប្រភេទណា?", "តំបន់ត្រជាក់", "តំបន់មូសុងត្រូពិច (មាន ២ រដូវ)", "តំបន់វាលខ្សាច់", "តំបន់ប៉ូល", "B"},
                    {"geo_sec1", "៣. ខ្សែស្របកណ្តាលនៃផែនដី (រយៈទទឹង 0°) ត្រូវបានគេហៅថាអ្វី?", "ខ្សែប៉ូល", "អេក្វាទ័រ", "ខ្សែមេរីឌៀនដើម", "តំបន់ត្រូពិច", "B"},
                    {"geo_sec1", "៤. មហាសមុទ្រណាដែលមានទំហំធំជាងគេបំផុតលើពិភពលោក?", "មហាសមុទ្រអាត្លង់ទិក", "មហាសមុទ្រឥណ្ឌា", "មហាសមុទ្រប៉ាស៊ីហ្វិក", "មហាសមុទ្រអាកទិក", "C"},
                    {"geo_sec1", "៥. កម្លាំងធម្មជាតិណាដែលធ្វើឱ្យដីមានជីជាតិនៅតាមដងទន្លេមេគង្គ?", "ល្បាប់ (ដីល្បាប់ទឹកជំនន់)", "ខ្យល់ព្យុះ", "រលកសមុទ្រ", "ភ្នំភ្លើង", "A"},
                    {"geo_sec2", "១. ប្រទេសកម្ពុជាមានផ្ទៃក្រឡាសរុបប្រមាណប៉ុន្មានគីឡូម៉ែត្រការ៉េ?", "181,035 km²", "236,800 km²", "331,210 km²", "513,120 km²", "A"},
                    {"geo_sec2", "២. ព្រំប្រទល់ខាងលិច និងខាងជើងឆៀងខាងលិចរបស់កម្ពុជាជាប់នឹងប្រទេសណា?", "វៀតណាម", "ឡាវ", "ថៃ", "មីយ៉ាន់ម៉ា", "C"},
                    {"geo_sec2", "៣. បឹងទឹកសាបធំជាងគេនៅអាស៊ីអាគ្នេយ៍គឺបឹងណា?", "បឹងកំពីងពួយ", "បឹងទន្លេសាប", "បឹងយក្សឡោម", "បឹងបៃកាល់", "B"},
                    {"geo_sec2", "៤. កំពង់ផែទឹកជ្រៅអន្តរជាតិសំខាន់ជាងគេរបស់កម្ពុជាស្ថិតនៅខេត្ត/ក្រុងណា?", "ខេត្តកំពត", "ខេត្តកោះកុង", "ខេត្តព្រះសីហនុ", "រាជធានីភ្នំពេញ", "C"},
                    {"geo_sec2", "៥. ខេត្តណាមួយរបស់កម្ពុជាដែលល្បីល្បាញខាងទន្លេមេគង្គ និងផ្សោតទឹកសាបកាំពី?", "ខេត្តបាត់ដំបង", "ខេត្តក្រចេះ", "ខេត្តពោធិ៍សាត់", "ខេត្តកែប", "B"},
                    {"geo_sec3", "១. វិស័យសេដ្ឋកិច្ចចម្បងៗ (សសរស្តម្ភ) របស់កម្ពុជាបច្ចុប្បន្នរួមមាន៖", "កសិកម្ម វាយនភណ្ឌ-កាត់ដេរ ទេសចរណ៍ និងសំណង់", "តែនេសាទម្យ៉ាង", "ឧស្សាហកម្មផលិតយន្តហោះ", "ការជីករ៉ែប្រេងកាតតែម្យ៉ាង", "A"},
                    {"geo_sec3", "២. តំបន់ណាដែលមានដង់ស៊ីតេប្រជាជនរស់នៅច្រើនកុះករជាងគេនៅកម្ពុជា?", "តំបន់ខ្ពង់រាប និងភ្នំ", "តំបន់វាលទំនាបកណ្តាល និងរាជធានីភ្នំពេញ", "តំបន់ព្រៃភ្នំក្រវាញ", "កោះដាច់ស្រយាល", "B"},
                    {"geo_sec3", "៣. មាត្រដ្ឋានផែនទី 1 : 100,000 មានន័យថា 1 cm លើផែនទីស្មើនឹងប៉ុន្មាននៅលើដីជាក់ស្តែង?", "100 m", "1 km (1,000 m)", "10 km", "100 km", "B"},
                    {"geo_sec3", "៤. ទ្វីបណាដែលមានចំនួនប្រជាជនច្រើនជាងគេបំផុតលើពិភពលោក?", "ទ្វីបអឺរ៉ុប", "ទ្វីបអាហ្វ្រិក", "ទ្វីបអាស៊ី", "ទ្វីបអាមេរិក", "C"},
                    {"geo_sec3", "៥. ដំណាំកសិឧស្សាហកម្មសំខាន់ៗនៅកម្ពុជារួមមានអ្វីខ្លះ?", "ស្រូវ កៅស៊ូ ស្វាយចន្ទី ដំឡូងមី និងម្រេច", "តែស្រូវសាលី", "ផ្លែប៉ោម និងទំពាំងបាយជូរ", "ដើមអូលីវ", "A"}
                }
            ),
            new StandardExamSpec(
                "សីលធម៌", "សីលធម៌",
                "ចំណេះដឹងគោល៖ បាវចនាជាតិ «ជាតិ សាសនា ព្រះមហាក្សត្រ» | អង្គការនីតិប្បញ្ញត្តិ នីតិប្រតិបត្តិ និងតុលាការ | សិទ្ធិ និងកាតព្វកិច្ចពលរដ្ឋ",
                "I. សីលធម៌ វប្បធម៌ និងសង្គមខ្មែរ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. ច្បាប់ សិទ្ធិមនុស្ស និងពលរដ្ឋល្អ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. ការគ្រប់គ្រងរដ្ឋ ប្រជាធិបតេយ្យ និងសន្តិភាព៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. សំណួរវិភាគស្ថានភាពសង្គម និងពលរដ្ឋវិជ្ជា៖", "ចូរឆ្លើយសំណួរវិភាគខាងក្រោមដោយលើកអំណះអំណាងសមស្រប៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ក្នុងនាមជាយុវជន និងសិស្សល្អ តើប្អូនត្រូវប្រតិបត្តិសីលធម៌រស់នៅក្នុងសាលារៀន និងគ្រួសារដូចម្តេចខ្លះ?\n\n២. (១៣ ពិន្ទុ) ចូរពន្យល់ពីទំនាក់ទំនងរវាង «សិទ្ធិ» និង «កាតព្វកិច្ច» របស់ប្រជាពលរដ្ឋនៅក្នុងសង្គមនីតិរដ្ឋ ដោយលើកឧទាហរណ៍ពីការគោរពច្បាប់ចរាចរណ៍ និងការបង់ពន្ធ។",
                new String[][]{
                    {"civic_sec1", "១. តើការសំពះតាមប្រពៃណីខ្មែរមានប៉ុន្មានរបៀបសំខាន់ៗ?", "៣ របៀប", "៥ របៀប", "៧ របៀប", "១០ របៀប", "B"},
                    {"civic_sec1", "២. គុណធម៌ ៤ យ៉ាងរបស់មាតាបិតាចំពោះកូន (ព្រហ្មវិហារធម៌ ៤) គឺ៖", "សទ្ធា សីល ចាគៈ បញ្ញា", "មេត្តា ករុណា មុទិតា ឧបេក្ខា", "ទាន សីល ភាវនា សច្ចៈ", "លោភៈ ទោសៈ មោហៈ", "B"},
                    {"civic_sec1", "៣. ទង្វើណាមួយដែលបង្ហាញពីសីលធម៌ល្អក្នុងការប្រើប្រាស់បណ្តាញសង្គម?", "ផ្សព្វផ្សាយព័ត៌មានមិនពិត", "ប្រើភាសាគួរសម គោរពសិទ្ធិអ្នកដទៃ និងផ្ទៀងផ្ទាត់ការពិត", "ជេរប្រមាថអ្នកដទៃ", "លួចរូបភាពអ្នកដទៃ", "B"},
                    {"civic_sec1", "៤. ទិវាសិទ្ធិនារីអន្តរជាតិប្រារព្ធឡើងជារៀងរាល់ឆ្នាំនៅថ្ងៃណា?", "១ មិថុនា", "៨ មីនា", "១០ ធ្នូ", "១ ឧសភា", "B"},
                    {"civic_sec1", "៥. តើពាក្យថា «កតញ្ញូតាធម៌» មានន័យដូចម្តេច?", "ការដឹងគុណ និងតបស្នងសងគុណចំពោះអ្នកមានគុណ", "ការខឹងសម្បា", "ការប្រកួតប្រជែង", "ភាពអាត្មានិយម", "A"},
                    {"civic_sec2", "១. ច្បាប់កំពូលនៃព្រះរាជាណាចក្រកម្ពុជាគឺជាអ្វី?", "ច្បាប់ចរាចរណ៍", "រដ្ឋធម្មនុញ្ញ", "ប្រកាសក្រសួង", "សារាចរណែនាំ", "B"},
                    {"civic_sec2", "២. តាមរដ្ឋធម្មនុញ្ញកម្ពុជា ប្រជាពលរដ្ឋខ្មែរគ្រប់រូបមានសិទ្ធិបោះឆ្នោតចាប់ពីអាយុប៉ុន្មានឆ្នាំ?", "១៥ ឆ្នាំ", "១៨ ឆ្នាំឡើងទៅ", "២១ ឆ្នាំ", "២៥ ឆ្នាំ", "B"},
                    {"civic_sec2", "៣. សិទ្ធិកុមារជាមូលដ្ឋានមាន ៤ ក្រុមសំខាន់គឺ៖", "រស់រានមានជីវិត ការអភិវឌ្ឍ ការការពារ និងការចូលរួម", "តែលេងកម្សាន្តម្យ៉ាង", "តែការរកប្រាក់", "គ្មានសិទ្ធិ", "A"},
                    {"civic_sec2", "៤. នៅពេលជិះម៉ូតូលើដងផ្លូវ តើអ្នកបើកបរត្រូវគោរពច្បាប់អ្វីជាចាំបាច់?", "បើកបរយ៉ាងលឿន", "ពាក់មួកសុវត្ថិភាព និងគោរពភ្លើងសញ្ញាចរាចរណ៍", "មិនបាច់មានកញ្ចក់មើលក្រោយ", "ជែងគ្រប់ពេល", "B"},
                    {"civic_sec2", "៥. កាតព្វកិច្ចសំខាន់មួយរបស់ពលរដ្ឋចំពោះបរិស្ថានគឺ៖", "បោះចោលសំរាមតាមទីសាធារណៈ", "ចូលរួមសម្អាតអនាម័យ និងដាំដើមឈើថែរក្សាបរិស្ថាន", "កាប់បំផ្លាញព្រៃឈើ", "ដុតប្លាស្ទិក", "B"},
                    {"civic_sec3", "១. អំណាចរដ្ឋនៅកម្ពុជាបែងចែកជា ៣ អំណាចដាច់ដោយឡែកពីគ្នាគឺ៖", "នីតិប្បញ្ញត្តិ នីតិប្រតិបត្តិ និងតុលាការ", "យោធា ប៉ូលិស និងពន្ធដារ", "ឃុំ ស្រុក និងខេត្ត", "សេដ្ឋកិច្ច សង្គម និងវប្បធម៌", "A"},
                    {"civic_sec3", "២. ស្ថាប័នណាដែលជាអ្នកតាក់តែង និងអនុម័តច្បាប់ (អំណាចនីតិប្បញ្ញត្តិ)?", "រាជរដ្ឋាភិបាល", "រដ្ឋសភា និងព្រឹទ្ធសភា", "តុលាការកំពូល", "គណៈកម្មាធិការជាតិរៀបចំការបោះឆ្នោត", "B"},
                    {"civic_sec3", "៣. ប្រព័ន្ធនយោបាយនៃព្រះរាជាណាចក្រកម្ពុជាគឺជា៖", "របបរាជានិយមអាស្រ័យរដ្ឋធម្មនុញ្ញ និងប្រជាធិបតេយ្យសេរីពហុបក្ស", "របបផ្តាច់ការ", "របបសហព័ន្ធ", "របបឯកបក្ស", "A"},
                    {"civic_sec3", "៤. ទិវាសិទ្ធិមនុស្សអន្តរជាតិប្រារព្ធឡើងនៅថ្ងៃណា?", "៨ មីនា", "៩ វិច្ឆិកា", "១០ ធ្នូ", "២៣ តុលា", "C"},
                    {"civic_sec3", "៥. តើការដោះស្រាយទំនាស់ដោយសន្តិវិធីផ្តល់ផលប្រយោជន៍អ្វីដល់សង្គម?", "បង្កើតសត្រូវ", "រក្សាសាមគ្គីភាព ស្ថិរភាព និងសុខដុមរមនាសង្គម", "ខាតបង់ពេលវេលា", "បំផ្លាញសហគមន៍", "B"}
                }
            ),
            new StandardExamSpec(
                "ផែនដីវិទ្យា", "ផែនដី",
                "ទិន្នន័យភូគព្ភវិទ្យា៖ ស្រទាប់ផែនដី ៣ (សម្បក ម៉ង់តូ ស្នូល) | សមាសភាពខ្យល់៖ N₂ 78%, O₂ 21%, ឧស្ម័នដទៃ 1% | ប្រព័ន្ធព្រះអាទិត្យមានភព ៨",
                "I. ស្រទាប់ផែនដី ថ្ម និងរ៉ែ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. បរិយាកាស អាកាសធាតុ និងមហាសមុទ្រ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. តារាសាស្ត្រ និងបរិស្ថានផែនដី៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. សំណួរពន្យល់បាតុភូមិផែនដី និងវិភាគ៖", "ចូរពន្យល់ពីមូលហេតុ និងដំណោះស្រាយចំពោះសំណួរខាងក្រោម៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ចូររៀបរាប់ពីស្រទាប់ធំៗទាំង ៣ នៃរចនាសម្ព័ន្ធខាងក្នុងរបស់ផែនដី និងលក្ខណៈសំខាន់ៗនៃស្រទាប់នីមួយៗ។\n\n២. (១៣ ពិន្ទុ) តើអ្វីជា «ផលផ្ទះកញ្ចក់ (Greenhouse Effect)» និងការឡើងកម្ដៅផែនដី? ចូរលើកវិធានការ ៣ យ៉ាងដើម្បីកាត់បន្ថយផលប៉ះពាល់បរិស្ថាន និងការប្រែប្រួលអាកាសធាតុ។",
                new String[][]{
                    {"earth_sec1", "១. តើរចនាសម្ព័ន្ធខាងក្នុងផែនដីចែកចេញជាស្រទាប់ធំៗប៉ុន្មាន?", "២ ស្រទាប់", "៣ ស្រទាប់ (សម្បកផែនដី ម៉ង់តូ និងស្នូល)", "៤ ស្រទាប់តែសម្បក", "១ ស្រទាប់", "B"},
                    {"earth_sec1", "២. ថ្មប្រភេទណាដែលកកើតឡើងពីការត្រជាក់ និងកកនៃម៉ាក់ម៉ា ឬកម្អែភ្នំភ្លើង?", "ថ្មកម្ទេចកំណ (Sedimentary)", "ថ្មម៉ាក់ម៉ា / ភ្នំភ្លើង (Igneous)", "ថ្មប្រែរូប (Metamorphic)", "ដីខ្សាច់", "B"},
                    {"earth_sec1", "៣. តើមូលហេតុចម្បងអ្វីដែលបង្កឱ្យមាន «ការរញ្ជួយដី» និង «ភ្នំភ្លើង»?", "ចលនាផ្លាកតិចតូនិច (Tectonic Plates)", "ខ្យល់បក់ខ្លាំង", "ជំនោរសមុទ្រ", "ព្រះច័ន្ទរះ", "A"},
                    {"earth_sec1", "៤. មាត្រដ្ឋានណាដែលប្រើសម្រាប់វាស់កម្រិតភាពធ្ងន់ធ្ងរនៃការរញ្ជួយដី?", "មាត្រដ្ឋានសេលស្យុស", "មាត្រដ្ឋានរិចទ័រ (Richter Scale)", "មាត្រដ្ឋានប៉ាស្កាល់", "មាត្រដ្ឋាន pH", "B"},
                    {"earth_sec1", "៥. តើផូស៊ីល (Fossils) ច្រើនត្រូវបានរកឃើញនៅក្នុងថ្មប្រភេទណា?", "ថ្មក្រានីត", "ថ្មកម្ទេចកំណ (ដូចជាថ្មកំបោរ ថ្មភក់)", "ថ្មបាសាល់", "លោហៈធាតុរាវ", "B"},
                    {"earth_sec2", "១. តើស្រទាប់បរិយាកាសណាដែលនៅជិតផ្ទៃផែនដីជាងគេ និងជាកន្លែងកើតមានភ្លៀង ពពក ខ្យល់ព្យុះ?", "ស្ត្រាតូស្វ៊ែរ", "ត្រូប៉ូស្វ៊ែរ (Troposphere)", "មេសូស្វ៊ែរ", "ទែម៉ូស្វ៊ែរ", "B"},
                    {"earth_sec2", "២. ស្រទាប់អូហ្សូន (Ozone Layer) ដែលការពារផែនដីពីកាំរស្មីស្វាយអ៊ុលត្រាស្ថិតនៅស្រទាប់ណា?", "ត្រូប៉ូស្វ៊ែរ", "ស្ត្រាតូស្វ៊ែរ (Stratosphere)", "ស្នូលផែនដី", "សមុទ្រជ្រៅ", "B"},
                    {"earth_sec2", "៣. ដំណើរវិលវង់ទឹក (Water Cycle) លើផែនដីចាប់ផ្តើមពីរំហួតទឹក រួចកកើតជាអ្វី?", "ថ្មភ្នំភ្លើង", "កំណកអាកាស (ពពក) និងទឹកភ្លៀង", "រ៉ែមាស", "ទឹកសមុទ្រប្រៃ", "B"},
                    {"earth_sec2", "៤. តើឧស្ម័នណាមួយជាមូលហេតុចម្បងបង្កឱ្យមានផលផ្ទះកញ្ចក់ និងឡើងកម្ដៅផែនដី?", "អុកស៊ីសែន (O₂)", "កាបូនឌីអុកស៊ីត (CO₂) និងមេតាន", "អាសូត (N₂)", "អាកុង (Ar)", "B"},
                    {"earth_sec2", "៥. ឧបករណ៍ «បារ៉ូម៉ែត្រ (Barometer)» ប្រើសម្រាប់វាស់អ្វី?", "សីតុណ្ហភាព", "សម្ពាធបរិយាកាស", "ល្បឿនខ្យល់", "កម្រិតទឹកភ្លៀង", "B"},
                    {"earth_sec3", "១. ក្នុងប្រព័ន្ធព្រះអាទិត្យ តើផែនដីជាភពលំដាប់ទីប៉ុន្មានរាប់ពីព្រះអាទិត្យ?", "ទី ២", "ទី ៣", "ទី ៤", "ទី ៥", "B"},
                    {"earth_sec3", "២. ការវិលជុំវិញអ័ក្សខ្លួនឯងរបស់ផែនដីប្រើរយៈពេល ២៤ ម៉ោង បង្កើតឱ្យមានបាតុភូតអ្វី?", "រដូវទាំង ៤", "យប់ និងថ្ងៃ", "ចន្ទគ្រាស", "ការរញ្ជួយដី", "B"},
                    {"earth_sec3", "៣. ផែនដីវិលជុំវិញព្រះអាទិត្យប្រើរយៈពេលប្រហែលប៉ុន្មានថ្ងៃ?", "២៨ ថ្ងៃ", "១៨០ ថ្ងៃ", "៣៦៥.២៥ ថ្ងៃ (១ ឆ្នាំ)", "៥០០ ថ្ងៃ", "C"},
                    {"earth_sec3", "៤. តើភពណាដែលមានទំហំធំជាងគេបំផុតក្នុងប្រព័ន្ធព្រះអាទិត្យ?", "ភពផែនដី", "ភពអង្គារ", "ភពព្រហស្បតិ៍ (Jupiter)", "ភពសុក្រ", "C"},
                    {"earth_sec3", "៥. បាតុភូតជំនោរទឹកសមុទ្រឡើង និងស្រកកើតឡើងដោយសារកម្លាំងទំនាញរបស់អ្វីជាចម្បង?", "ភពពុធ", "ព្រះច័ន្ទ និងព្រះអាទិត្យ", "ភ្លៀងធ្លាក់", "ការរញ្ជួយដី", "B"}
                }
            ),
            new StandardExamSpec(
                "កុំព្យូទ័រ", "កុំព្យូទ័រ",
                "រូបមន្ត & មូលដ្ឋានគ្រឹះ៖ 1 Byte = 8 bits | 1 KB = 1024 Bytes | Excel: =SUM(A1:A10), =AVERAGE(A1:A10), =IF(A1>=50,\"Pass\",\"Fail\") | លេខគោល២ (Binary)",
                "I. ផ្នែករឹង (Hardware) និងប្រព័ន្ធប្រតិបត្តិការ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "II. កម្មវិធីការិយាល័យ (Word, Excel) និងទិន្នន័យ៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "III. បណ្តាញ Internet, HTML/កូដ និងសុវត្ថិភាព៖", "ចូរជ្រើសរើសចម្លើយ A, B, C ឬ D ដែលត្រឹមត្រូវបំផុត៖ (១៥ ពិន្ទុ)",
                "IV. លំហាត់អនុវត្ត រូបមន្ត Excel និងសរសេរកូដ៖", "ចូរឆ្លើយសំណួរអនុវត្ត និងសរសេររូបមន្ត/កូដខាងក្រោម៖ (២៥ ពិន្ទុ)",
                "១. (១២ ពិន្ទុ) ក្នុងកម្មវិធី Microsoft Excel គេមានពិន្ទុសិស្សក្នុងប្រអប់ B2 ដល់ B6៖\n   ក) សរសេររូបមន្តសម្រាប់បូកពិន្ទុសរុប និងរូបមន្តគណនាមធ្យមភាគ។\n   ខ) សរសេររូបមន្ត IF ដើម្បីបង្ហាញពាក្យ «ជាប់» បើមធ្យមភាគក្នុង C2 >= 50 ឬ «ធ្លាក់» បើតិចជាង 50 ។\n\n២. (១៣ ពិន្ទុ) ចូរបំប្លែងលេខគោលដប់ 25 ទៅជាលេខគោលពីរ (Binary) និងរៀបរាប់ពីវិធីការពារសុវត្ថិភាពគណនីអ៊ីនធឺណិតចំនួន ៣ ចំណុច។",
                new String[][]{
                    {"it_sec1", "១. តើឧបករណ៍ណាដែលប្រើជា «ខួរក្បាល» សម្រាប់គណនា និងដំណើរការទិន្នន័យកុំព្យូទ័រ?", "Monitor", "CPU (Central Processing Unit)", "Printer", "Mouse", "B"},
                    {"it_sec1", "២. តើ 1 Byte ស្មើនឹងប៉ុន្មាន bits?", "4 bits", "8 bits", "16 bits", "32 bits", "B"},
                    {"it_sec1", "៣. តើអង្គចងចាំបណ្តោះអាសន្នណាដែលបាត់បង់ទិន្នន័យពេលបិទភ្លើងកុំព្យូទ័រ?", "Hard Disk / SSD", "RAM", "USB Flash", "CD-ROM", "B"},
                    {"it_sec1", "៤. ឧបករណ៍ណាមួយជា Output Device (ឧបករណ៍បញ្ចេញទិន្នន័យ)?", "Keyboard", "Scanner", "Printer និង Monitor", "Microphone", "C"},
                    {"it_sec1", "៥. Windows 11, macOS និង Linux គឺជាប្រភេទកម្មវិធីអ្វី?", "កម្មវិធីរចនារូបភាព", "ប្រព័ន្ធប្រតិបត្តិការ (Operating System)", "កម្មវិធីលេងហ្គេម", "ឧបករណ៍រឹង", "B"},
                    {"it_sec2", "១. ក្នុង Microsoft Excel គ្រប់រូបមន្តទាំងអស់ត្រូវចាប់ផ្តើមដោយសញ្ញាអ្វី?", "សញ្ញា +", "សញ្ញា =", "សញ្ញា $", "សញ្ញា #", "B"},
                    {"it_sec2", "២. ផ្លូវកាត់ (Shortcut) ណាសម្រាប់ «ចម្លង» (Copy) និង «បិទភ្ជាប់» (Paste) លើ Windows?", "Ctrl+X និង Ctrl+Z", "Ctrl+C និង Ctrl+V", "Ctrl+S និង Ctrl+P", "Ctrl+A និង Ctrl+F", "B"},
                    {"it_sec2", "៣. អនុគមន៍ Excel ណាដែលប្រើសម្រាប់រកតម្លៃធំជាងគេក្នុងជួរ A1:A10?", "=MIN(A1:A10)", "=SUM(A1:A10)", "=MAX(A1:A10)", "=COUNT(A1:A10)", "C"},
                    {"it_sec2", "៤. ឯកសារដែលរក្សាទុកពី Microsoft Word ជំនាន់ថ្មីមានកន្ទុយ (Extension) អ្វី?", ".xlsx", ".docx", ".pptx", ".mp3", "B"},
                    {"it_sec2", "៥. លេខគោលដប់ «10» សរសេរជាលេខគោលពីរ (Binary) បានអ្វី?", "1001", "1010", "1100", "0101", "B"},
                    {"it_sec3", "១. អក្សរកាត់ «URL» ក្នុងការប្រើប្រាស់អ៊ីនធឺណិតតំណាងឱ្យអ្វី?", "អាសយដ្ឋានគេហទំព័រ (Uniform Resource Locator)", "ឈ្មោះម៉ាស៊ីនព្រីន", "ប្រភេទខ្សែភ្លើង", "កម្មវិធីកំចាត់មេរោគ", "A"},
                    {"it_sec3", "២. ស្លាក HTML (Tag) ណាដែលប្រើសម្រាប់បង្កើតតំណភ្ជាប់ (Hyperlink)?", "<p>", "<img>", "<a>", "<table>", "C"},
                    {"it_sec3", "៣. ប្រូតូកូលណាដែលផ្តល់សុវត្ថិភាពអ៊ិនគ្រីបទិន្នន័យលើគេហទំព័រ?", "HTTP", "HTTPS", "FTP", "SMTP", "B"},
                    {"it_sec3", "៤. ទង្វើបោកប្រាស់តាមអ៊ីមែល ឬសារដើម្បីលួចពាក្យសម្ងាត់ត្រូវបានហៅថាអ្វី?", "Firewall", "Phishing", "Backup", "Update", "B"},
                    {"it_sec3", "៥. ពាក្យសម្ងាត់ (Password) ដែលមានសុវត្ថិភាពខ្ពស់គួរមានលក្ខណៈដូចម្តេច?", "ប្រើថ្ងៃខែឆ្នាំកំណើត", "ប្រើ 123456", "មានប្រវែងវែង លាយអក្សរធំ-តូច លេខ និងនិមិត្តសញ្ញាពិសេស", "ប្រើឈ្មោះខ្លួនឯង", "C"}
                }
            )
        );

        try {
            List<Account> allAccounts = accountRepository.findAll();
            for (StandardExamSpec spec : specs) {
                for (Account acc : allAccounts) {
                    String subj = acc.getSubject();
                    boolean isDemo = Long.valueOf(98L).equals(acc.getId());
                    if (!isDemo && (subj == null || !subj.contains(spec.subjectKeyword()))) {
                        continue;
                    }

                    // STRICT ZERO DATA LOSS: Never touch or overwrite if this teacher already has any exam for this subject
                    boolean hasAny = examRepository.findByAccountId(acc.getId()).stream()
                            .anyMatch(e -> spec.subjectName().equals(e.getSubjectName()));
                    if (hasAny) {
                        continue;
                    }

                    List<Classroom> teacherClasses = classroomRepository.findByAccountId(acc.getId());
                    String classNames = teacherClasses.stream()
                            .map(Classroom::getClassName)
                            .filter(java.util.Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(java.util.stream.Collectors.joining(", "));
                    if (classNames.isEmpty()) {
                        classNames = (acc.getTeachingClasses() != null && !acc.getTeachingClasses().trim().isEmpty())
                                ? acc.getTeachingClasses().trim()
                                : "៨D";
                    }

                    Exam exam = new Exam();
                    exam.setAccountId(acc.getId());
                    exam.setMonth("ខែវិច្ឆិកា ២០២៦");
                    exam.setSubjectName(spec.subjectName());
                    exam.setClassName(classNames);
                    exam.setOpenClasses(""); // Closed by default until teacher opens for class
                    exam.setReadingPassage(spec.formulaNote());
                    exam.setSection1Title(spec.sec1Title());
                    exam.setSection1Instruction(spec.sec1Instruction());
                    exam.setSection1MaxPoints(15);
                    exam.setSection2Title(spec.sec2Title());
                    exam.setSection2Instruction(spec.sec2Instruction());
                    exam.setSection2MaxPoints(15);
                    exam.setSection3Title(spec.sec3Title());
                    exam.setSection3Instruction(spec.sec3Instruction());
                    exam.setSection3MaxPoints(15);
                    exam.setSection4Title(spec.sec4Title());
                    exam.setSection4Instruction(spec.sec4Instruction());
                    exam.setWritingPrompt(spec.writingPrompt());
                    exam.setWritingMaxPoints(25);
                    exam = examRepository.save(exam);

                    for (String[] row : spec.questions()) {
                        Question q = new Question();
                        q.setExamId(exam.getId());
                        q.setAccountId(acc.getId());
                        q.setSkillType(row[0]);
                        q.setQuestionText(row[1]);
                        q.setOptionA(row[2]);
                        q.setOptionB(row[3]);
                        q.setOptionC(row[4]);
                        q.setOptionD(row[5]);
                        q.setCorrectOption(row[6]);
                        questionRepository.save(q);
                    }
                    System.out.println(">>> [DataInitializer] Seeded starter Standard MoEYS " + spec.subjectName() + " Exam for teacher: " + acc.getFullName() + " (ID " + acc.getId() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [DataInitializer] Notice seeding standard subject exams: " + e.getMessage());
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
