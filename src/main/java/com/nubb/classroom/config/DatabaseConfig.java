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
            try {
                String jdbcUrl;
                String username = "";
                String password = "";

                if (dbUrl.startsWith("jdbc:postgresql://")) {
                    jdbcUrl = dbUrl;
                    username = System.getenv("SPRING_DATASOURCE_USERNAME") != null ? System.getenv("SPRING_DATASOURCE_USERNAME") : "postgres";
                    password = System.getenv("SPRING_DATASOURCE_PASSWORD") != null ? System.getenv("SPRING_DATASOURCE_PASSWORD") : "";
                } else {
                    // Standard URI format: postgres://username:password@host:port/database
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

                HikariConfig config = new HikariConfig();
                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(30000);
                config.setConnectionTimeout(30000);
                config.setMaxLifetime(1800000);

                System.out.println(">>> [DatabaseConfig] SUCCESS: Connected to external PostgreSQL database at " + jdbcUrl);
                return new HikariDataSource(config);
            } catch (Exception e) {
                System.err.println(">>> [DatabaseConfig] WARNING: Failed to configure PostgreSQL, falling back to local H2: " + e.getMessage());
            }
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
}
