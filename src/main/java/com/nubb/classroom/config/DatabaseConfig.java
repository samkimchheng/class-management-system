package com.nubb.classroom.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        // Check if PostgreSQL URL is provided via environment variables (e.g., on Render or Supabase)
        if (dbUrl != null && !dbUrl.trim().isEmpty() && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://") || dbUrl.startsWith("jdbc:postgresql://"))) {
            HikariDataSource pgDs = tryConnectPostgres(dbUrl);
            if (pgDs != null) {
                return pgDs;
            }
            System.err.println(">>> [DatabaseConfig] WARNING: PostgreSQL connection failed or timed out. Falling back to built-in local H2 database.");
        }

        // Local development or fallback: H2 embedded file database
        System.out.println(">>> [DatabaseConfig] Using local H2 file database (./data/classroomDB)...");
        HikariConfig h2Config = new HikariConfig();
        h2Config.setDriverClassName("org.h2.Driver");
        h2Config.setJdbcUrl("jdbc:h2:file:./data/classroomDB;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE");
        h2Config.setUsername("sa");
        h2Config.setPassword("");
        h2Config.setMaximumPoolSize(10);
        return new HikariDataSource(h2Config);
    }

    private HikariDataSource tryConnectPostgres(String dbUrl) {
        try {
            String jdbcUrl;
            String username = "";
            String password = "";

            if (dbUrl.startsWith("jdbc:postgresql://")) {
                jdbcUrl = dbUrl;
                username = System.getenv("SPRING_DATASOURCE_USERNAME") != null ? System.getenv("SPRING_DATASOURCE_USERNAME") : "postgres";
                password = System.getenv("SPRING_DATASOURCE_PASSWORD") != null ? System.getenv("SPRING_DATASOURCE_PASSWORD") : "";
            } else {
                URI uri = new URI(dbUrl.replace("postgresql://", "postgres://"));
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "classroom";

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = userParts[0];
                    if (userParts.length > 1) {
                        password = userParts[1];
                    }
                }

                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    jdbcUrl += "?" + uri.getQuery();
                }
            }

            // Remote databases (e.g. Render, Supabase, Neon) require SSL
            if (!jdbcUrl.contains("sslmode=") && !jdbcUrl.contains("localhost") && !jdbcUrl.contains("127.0.0.1")) {
                jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.postgresql.Driver");
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000); // 5 seconds max for fast fail
            config.setInitializationFailTimeout(5000);
            config.setMaxLifetime(1800000);

            HikariDataSource ds = new HikariDataSource(config);

            // Validate connection immediately
            try (java.sql.Connection conn = ds.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                System.out.println(">>> [DatabaseConfig] SUCCESS: Connected and validated PostgreSQL database at " + jdbcUrl);
                return ds;
            } catch (Exception ex) {
                System.err.println(">>> [DatabaseConfig] Failed to validate PostgreSQL connection: " + ex.getMessage());
                try { ds.close(); } catch (Exception ignored) {}

                // If failed with sslmode=require, try once with sslmode=prefer
                if (jdbcUrl.contains("sslmode=require")) {
                    try {
                        String fallbackUrl = jdbcUrl.replace("sslmode=require", "sslmode=prefer");
                        config.setJdbcUrl(fallbackUrl);
                        HikariDataSource ds2 = new HikariDataSource(config);
                        try (java.sql.Connection conn2 = ds2.getConnection();
                             java.sql.Statement stmt2 = conn2.createStatement()) {
                            stmt2.execute("SELECT 1");
                            System.out.println(">>> [DatabaseConfig] SUCCESS with fallback sslmode=prefer!");
                            return ds2;
                        } catch (Exception ex2) {
                            try { ds2.close(); } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println(">>> [DatabaseConfig] Error configuring PostgreSQL: " + e.getMessage());
            return null;
        }
    }
}
