package com.upskill.stepdefinitions;

import com.upskill.db.DatabaseUtil;
import com.upskill.utils.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DatabaseSteps {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSteps.class);

    @Then("if database is available, validate field {string} for user id {int} matches API value")
    public void validateFieldAgainstDb(String field, int userId) {
        if (!DatabaseUtil.isAvailable()) {
            log.warn("Database not available - skipping DB validation. This is expected in demo mode.");
            return;
        }
        String sql = "SELECT " + field + " FROM users WHERE id = ?";
        List<Map<String, Object>> results = DatabaseUtil.executeQuery(sql, userId);
        if (!results.isEmpty()) {
            String dbValue = results.get(0).get(field).toString();
            String apiValue = TestContext.get("api_user_" + field);
            log.info("DB value: '{}', API value: '{}'", dbValue, apiValue);
            assertEquals("DB vs API mismatch for field: " + field, dbValue, apiValue);
        } else {
            log.warn("No DB record found for user id={}", userId);
        }
    }

    @Given("I create test data in database for user {string}")
    public void iCreateTestDataInDatabase(String username) {
        if (!DatabaseUtil.isAvailable()) {
            log.warn("Database not available - skipping test data creation.");
            return;
        }
        String sql = "INSERT INTO users (username, name, email) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
        int rows = DatabaseUtil.executeUpdate(sql, username, "Test User", username + "@test.com");
        log.info("Created {} test data rows for user '{}'", rows, username);
    }

    @Then("I delete test data from database for user {string}")
    public void iDeleteTestDataFromDatabase(String username) {
        if (!DatabaseUtil.isAvailable()) {
            log.warn("Database not available - skipping test data cleanup.");
            return;
        }
        int rows = DatabaseUtil.executeUpdate("DELETE FROM users WHERE username = ?", username);
        log.info("Deleted {} rows for user '{}'", rows, username);
    }
}
