@all @database @api
Feature: API with Database Validation
  As a QA engineer
  I want to validate API responses against the database
  So that I can ensure data consistency between API and DB layers

  # =====================================================================
  # NOTE: These scenarios demonstrate the DB interaction pattern.
  # If PostgreSQL is not available, DB steps are gracefully skipped
  # and tests still pass with warnings logged.
  #
  # In a real project, you would:
  #   1. Set up PostgreSQL with test data
  #   2. Update config-qa.properties with your DB credentials
  #   3. Run these tests against your actual API + DB
  # =====================================================================

  @regression @db
  Scenario: Validate API user data against database
    # Step 1: Get user from API
    Given I prepare a GET request to "/users/1"
    When I send the request
    Then the response status code should be 200
    And I extract and store the field "name" as "api_user_name"
    And I extract and store the field "email" as "api_user_email"

    # Step 2: Query the same user from DB and compare
    # (Gracefully skipped if DB is not available)
    Then if database is available, validate field "name" for user id 1 matches API value

  @regression @db
  Scenario: Create test data in DB, call API, validate, then cleanup
    # Step 1: Insert test data into DB (skipped if no DB)
    Given I create test data in database for user "test_api_user"

    # Step 2: Call API to retrieve data
    Given I prepare a GET request to "/users/1"
    When I send the request
    Then the response status code should be 200

    # Step 3: Cleanup test data from DB
    Then I delete test data from database for user "test_api_user"
