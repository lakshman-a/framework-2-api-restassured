package com.upskill.db;

import com.upskill.config.ConfigManager;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DatabaseUtil - Handles PostgreSQL database interactions.
 *
 * Used for:
 *   - Creating/deleting test data before tests
 *   - Validating API responses against DB records
 *   - Field-level comparisons between API and DB
 *
 * NOTE: In a real project, update the DB connection details in config-{env}.properties.
 *       If no DB is available, the framework gracefully skips DB steps.
 */
public class DatabaseUtil {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);
    private static Connection connection;

    /**
     * Get a database connection (creates one if needed).
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = ConfigManager.get("db.url");
                String user = ConfigManager.get("db.username");
                String pass = ConfigManager.get("db.password");

                log.info("Connecting to database: {}", url);
                connection = DriverManager.getConnection(url, user, pass);
                log.info("Database connection established");
            }
        } catch (SQLException e) {
            log.warn("Could not connect to database: {}. DB steps will be skipped.", e.getMessage());
            return null;
        }
        return connection;
    }

    /**
     * Execute a SELECT query and return results as a list of maps.
     */
    @Step("Execute SELECT query: {sql}")
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        log.info("Executing query: {}", sql);
        List<Map<String, Object>> results = new ArrayList<>();

        Connection conn = getConnection();
        if (conn == null) {
            log.warn("No DB connection. Returning empty results.");
            return results;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }

            log.info("Query returned {} rows", results.size());
        } catch (SQLException e) {
            log.error("Query execution failed: {}", e.getMessage());
        }
        return results;
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE statement.
     */
    @Step("Execute update: {sql}")
    public static int executeUpdate(String sql, Object... params) {
        log.info("Executing update: {}", sql);

        Connection conn = getConnection();
        if (conn == null) {
            log.warn("No DB connection. Skipping update.");
            return 0;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            int affected = stmt.executeUpdate();
            log.info("Rows affected: {}", affected);
            return affected;
        } catch (SQLException e) {
            log.error("Update execution failed: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get a single value from a query result.
     */
    @Step("Get single value: {sql}")
    public static Object getSingleValue(String sql, Object... params) {
        List<Map<String, Object>> results = executeQuery(sql, params);
        if (!results.isEmpty()) {
            return results.get(0).values().iterator().next();
        }
        return null;
    }

    /**
     * Close the database connection.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed");
            }
        } catch (SQLException e) {
            log.error("Error closing DB connection: {}", e.getMessage());
        }
    }

    /**
     * Check if DB is available (for graceful degradation).
     */
    public static boolean isAvailable() {
        Connection conn = getConnection();
        return conn != null;
    }
}
