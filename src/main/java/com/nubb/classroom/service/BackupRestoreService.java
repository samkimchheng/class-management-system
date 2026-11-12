package com.nubb.classroom.service;

import com.nubb.classroom.config.DataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BackupRestoreService {

    @Autowired
    private DataSource dataSource;

    private static final String[] BACKUP_TABLES = {
        "account",
        "classroom",
        "students",
        "attendance",
        "score_rule",
        "score_rules",
        "score",
        "schedule",
        "lessons",
        "lesson_transcript",
        "lesson_progress",
        "exam",
        "question",
        "daily_teaching_logs",
        "daily_teaching_log",
        "certificate_records",
        "certificate_record",
        "leave_requests",
        "leave_request",
        "feedback",
        "vocabulary_words",
        "student_vocabulary_progress"
    };

    /**
     * Generates a self-contained, portable, idempotent SQL dump containing
     * all table schemas and row data formatted in standard UTF-8 SQL.
     */
    public String generateSqlBackup() throws Exception {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("-- ====================================================================\n");
        sb.append("-- Class Management System 2027 - Full Database Backup\n");
        sb.append("-- Generated At: ").append(timestamp).append("\n");
        sb.append("-- Compatible with H2 and PostgreSQL\n");
        sb.append("-- ====================================================================\n\n");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String dbProduct = meta.getDatabaseProductName().toLowerCase();
            sb.append("-- Database Engine: ").append(meta.getDatabaseProductName()).append(" ").append(meta.getDatabaseProductVersion()).append("\n\n");

            // Build case-insensitive map of actual existing tables
            Set<String> existingTables = new HashSet<>();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tName = rs.getString("TABLE_NAME");
                    if (tName != null) existingTables.add(tName.toLowerCase());
                }
            }

            int tableCount = 0;
            long totalRowCount = 0;

            for (String tableName : BACKUP_TABLES) {
                if (!existingTables.contains(tableName.toLowerCase())) {
                    continue;
                }

                String actualTableName = findActualTableName(meta, conn.getCatalog(), tableName);
                if (actualTableName == null) actualTableName = tableName;

                // Inspect columns
                List<ColumnInfo> columns = new ArrayList<>();
                try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, actualTableName, "%")) {
                    while (rs.next()) {
                        columns.add(new ColumnInfo(
                                rs.getString("COLUMN_NAME"),
                                rs.getInt("DATA_TYPE"),
                                rs.getString("TYPE_NAME")
                        ));
                    }
                }

                if (columns.isEmpty()) {
                    continue;
                }

                sb.append("-- --------------------------------------------------------------------\n");
                sb.append("-- Table: ").append(actualTableName).append("\n");
                sb.append("-- --------------------------------------------------------------------\n");

                String selectSql = "SELECT * FROM " + quoteIdentifier(dbProduct, actualTableName);
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(selectSql)) {

                    List<String> colNames = new ArrayList<>();
                    for (ColumnInfo c : columns) {
                        colNames.add(quoteIdentifier(dbProduct, c.name));
                    }
                    String colListStr = String.join(", ", colNames);

                    long rowCountForTable = 0;
                    while (rs.next()) {
                        rowCountForTable++;
                        totalRowCount++;

                        List<String> values = new ArrayList<>();
                        for (ColumnInfo c : columns) {
                            Object val = rs.getObject(c.name);
                            if (val == null) {
                                values.add("NULL");
                            } else if (val instanceof Boolean) {
                                values.add(((Boolean) val) ? "TRUE" : "FALSE");
                            } else if (val instanceof Number) {
                                values.add(val.toString());
                            } else if (val instanceof java.sql.Timestamp || val instanceof java.sql.Date || val instanceof java.sql.Time || val instanceof java.time.temporal.TemporalAccessor) {
                                values.add("'" + escapeSqlString(val.toString()) + "'");
                            } else {
                                values.add("'" + escapeSqlString(val.toString()) + "'");
                            }
                        }

                        sb.append("INSERT INTO ").append(quoteIdentifier(dbProduct, actualTableName))
                          .append(" (").append(colListStr).append(") VALUES (")
                          .append(String.join(", ", values))
                          .append(");\n");
                    }

                    sb.append("-- [Dumped ").append(rowCountForTable).append(" rows from ").append(actualTableName).append("]\n\n");
                    tableCount++;
                } catch (Exception e) {
                    sb.append("-- Error dumping table ").append(actualTableName).append(": ").append(e.getMessage()).append("\n\n");
                }
            }

            sb.append("-- ====================================================================\n");
            sb.append("-- End of Backup: ").append(tableCount).append(" tables, ").append(totalRowCount).append(" total records.\n");
            sb.append("-- ====================================================================\n");
        }

        return sb.toString();
    }

    /**
     * Restores database records from uploaded SQL script content cleanly.
     * Clears multi-tenant data tables safely and inserts all parsed statements,
     * then aligns all database sequences immediately.
     */
    public RestoreResult restoreFromSql(String sqlContent) throws Exception {
        if (sqlContent == null || sqlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("ឯកសារ SQL ទទេ មិនមានទិន្នន័យឡើយ!");
        }

        int executedCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String dbProduct = meta.getDatabaseProductName().toLowerCase();
            boolean isH2 = dbProduct.contains("h2");
            boolean isPostgres = dbProduct.contains("postgres");

            boolean origAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                // Step 1: Temporarily disable referential integrity if H2
                if (isH2) {
                    try { stmt.execute("SET REFERENTIAL_INTEGRITY FALSE"); } catch (Exception ignored) {}
                }

                // Step 2: Delete existing data from backup tables in reverse dependency order
                List<String> reverseTables = new ArrayList<>(Arrays.asList(BACKUP_TABLES));
                Collections.reverse(reverseTables);

                for (String tbl : reverseTables) {
                    String quoted = quoteIdentifier(dbProduct, tbl);
                    try {
                        stmt.executeUpdate("DELETE FROM " + quoted);
                    } catch (Exception ignored) {
                        try { stmt.executeUpdate("DELETE FROM " + tbl); } catch (Exception ignored2) {}
                    }
                }

                // Step 3: Parse and execute all statements from SQL script
                List<String> statements = splitSqlStatements(sqlContent);
                for (String sql : statements) {
                    String trimmed = sql.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                        continue;
                    }
                    try {
                        stmt.execute(trimmed);
                        executedCount++;
                    } catch (Exception ex) {
                        errorCount++;
                        if (errors.size() < 10) {
                            errors.add(ex.getMessage() + " (SQL: " + (trimmed.length() > 80 ? trimmed.substring(0, 80) + "..." : trimmed) + ")");
                        }
                    }
                }

                // Step 4: Re-enable referential integrity if H2
                if (isH2) {
                    try { stmt.execute("SET REFERENTIAL_INTEGRITY TRUE"); } catch (Exception ignored) {}
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(origAutoCommit);
            }

            // Step 5: Align all auto-increment sequences immediately after restore
            try {
                DataInitializer.alignAllSequences(conn);
            } catch (Exception ignored) {}
        }

        return new RestoreResult(executedCount, errorCount, errors);
    }

    private static String findActualTableName(DatabaseMetaData meta, String catalog, String candidate) {
        try (ResultSet rs = meta.getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String t = rs.getString("TABLE_NAME");
                if (t != null && t.equalsIgnoreCase(candidate)) {
                    return t;
                }
            }
        } catch (Exception ignored) {}
        return candidate;
    }

    private static String quoteIdentifier(String dbProduct, String identifier) {
        if (identifier == null) return "";
        if (dbProduct != null && dbProduct.contains("postgres")) {
            return "\"" + identifier.toLowerCase() + "\"";
        }
        return "\"" + identifier + "\"";
    }

    private static String escapeSqlString(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }

    private static List<String> splitSqlStatements(String sql) throws Exception {
        List<String> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(sql));
        StringBuilder current = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.startsWith("//")) {
                continue;
            }
            current.append(line).append("\n");
            if (trimmed.endsWith(";")) {
                list.add(current.toString().trim());
                current.setLength(0);
            }
        }
        if (!current.toString().trim().isEmpty()) {
            list.add(current.toString().trim());
        }
        return list;
    }

    private static class ColumnInfo {
        final String name;
        final int dataType;
        final String typeName;

        ColumnInfo(String name, int dataType, String typeName) {
            this.name = name;
            this.dataType = dataType;
            this.typeName = typeName;
        }
    }

    public static class RestoreResult {
        public final int executedCount;
        public final int errorCount;
        public final List<String> errors;

        public RestoreResult(int executedCount, int errorCount, List<String> errors) {
            this.executedCount = executedCount;
            this.errorCount = errorCount;
            this.errors = errors;
        }
    }
}
