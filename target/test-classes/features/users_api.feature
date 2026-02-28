@all @users @api
Feature: User API - Complete CRUD Operations
  As a QA engineer testing the User API
  I want to verify all CRUD operations work correctly
  So that I can ensure data integrity across the system

  # =====================================================================
  # This feature demonstrates a COMPLETE end-to-end API test workflow:
  #   1. GET - Retrieve and validate response
  #   2. POST - Create new resource and validate
  #   3. PUT - Full update of resource
  #   4. PATCH - Partial update of resource
  #   5. DELETE - Remove resource
  #   6. Field extraction and cross-validation
  # =====================================================================

  @smoke @qa @regression
  Scenario: GET all users and validate response structure
    Given I prepare a GET request to "/users"
    When I send the request
    Then the response status code should be 200
    And the response should contain a list of users
    And each user should have fields "id", "name", "email", "username"
    And the response time should be less than 5000 milliseconds

  @smoke @qa @regression
  Scenario: GET a single user by ID and validate fields
    Given I prepare a GET request to "/users/1"
    When I send the request
    Then the response status code should be 200
    And the response field "id" should be 1
    And the response field "name" should be "Leanne Graham"
    And the response field "email" should be "Sincere@april.biz"
    And I extract and store the field "username" as "fetched_username"

  @smoke @qa
  Scenario: GET user with query parameters
    Given I prepare a GET request to "/users" with query params
      | key      | value                |
      | username | Bret                 |
    When I send the request
    Then the response status code should be 200
    And the response should contain a list of users
    And the first user's "username" should be "Bret"

  @regression @qa
  Scenario: POST - Create a new user
    Given I prepare a POST request to "/users" with body
      """
      {
        "name": "John Automation",
        "username": "johnauto",
        "email": "john.auto@test.com",
        "phone": "1-555-123-4567"
      }
      """
    When I send the request
    Then the response status code should be 201
    And the response field "name" should be "John Automation"
    And the response field "id" should not be null
    And I extract and store the field "id" as "created_user_id"

  @regression @qa
  Scenario: PUT - Full update of user
    Given I prepare a PUT request to "/users/1" with body
      """
      {
        "id": 1,
        "name": "Updated User Name",
        "username": "updateduser",
        "email": "updated@test.com"
      }
      """
    When I send the request
    Then the response status code should be 200
    And the response field "name" should be "Updated User Name"
    And the response field "email" should be "updated@test.com"

  @regression @qa
  Scenario: PATCH - Partial update of user email
    Given I prepare a PATCH request to "/users/1" with body
      """
      {
        "email": "patched.email@newdomain.com"
      }
      """
    When I send the request
    Then the response status code should be 200
    And the response field "email" should be "patched.email@newdomain.com"

  @regression @qa
  Scenario: DELETE - Remove a user
    Given I prepare a DELETE request to "/users/1"
    When I send the request
    Then the response status code should be 200

  @regression
  Scenario: GET non-existent user returns 404
    Given I prepare a GET request to "/users/99999"
    When I send the request
    Then the response status code should be 404
