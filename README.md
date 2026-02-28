# Framework 2: API Automation — REST Assured + Cucumber + Allure

## What Is This?

A **complete API test automation framework** with end-to-end testing, database validation, and all HTTP methods. Uses real public APIs ([JSONPlaceholder](https://jsonplaceholder.typicode.com) and [Reqres](https://reqres.in)).

| Layer | Technology | Purpose |
|-------|-----------|---------|
| BDD | Cucumber (Gherkin) | Readable `.feature` files |
| HTTP Client | REST Assured | API requests & response validation |
| DB | PostgreSQL JDBC | Database interaction & validation |
| Runner | JUnit 4 | Test execution |
| Reporting | Allure + REST Assured filter | Request/response captured in reports |
| Config | Properties files | Environment switching |
| Logging | SLF4J + Logback | Console + file logging |

---

## Project Structure

```
restassured-cucumber-api-framework/
├── pom.xml
├── src/main/java/com/upskill/
│   ├── config/ConfigManager.java        # Environment config loader
│   ├── api/ApiClient.java              # REST Assured wrapper (all HTTP methods)
│   ├── db/DatabaseUtil.java            # PostgreSQL interaction
│   ├── models/
│   │   ├── User.java                   # POJO for User API
│   │   └── Post.java                   # POJO for Post API
│   └── utils/
│       ├── TestContext.java             # Shares state between steps
│       └── JsonUtils.java              # JSON helpers
├── src/test/java/com/upskill/
│   ├── hooks/Hooks.java
│   ├── stepdefinitions/
│   │   ├── ApiSteps.java               # All API step definitions
│   │   └── DatabaseSteps.java          # DB step definitions
│   └── runners/
│       ├── TestRunner.java             # Main runner
│       └── TestRunnerSmoke.java        # Smoke runner
└── src/test/resources/
    ├── features/
    │   ├── users_api.feature           # CRUD on /users (8 scenarios)
    │   ├── posts_api.feature           # E2E + cross-validation (5 scenarios)
    │   └── database_api.feature        # DB interaction (2 scenarios)
    ├── config-qa.properties
    ├── config-dev.properties
    └── allure.properties
```

---

## What the Tests Cover

### users_api.feature
- **GET all users** — validate list, field presence, response time
- **GET single user** — validate specific fields, extract & store values
- **GET with query params** — filter by username
- **POST** — create user, validate response, extract created ID
- **PUT** — full update
- **PATCH** — partial update
- **DELETE** — remove resource
- **404** — non-existent resource

### posts_api.feature (End-to-End)
- **Create → Retrieve → Cross-validate → Cleanup** workflow
- **API-to-API cross validation** — user's ID matches post's userId
- **Nested data** — post comments validation
- **Response time assertion**

### database_api.feature
- **API vs DB validation** — compare API response fields with DB records
- **Test data lifecycle** — INSERT test data → API call → DELETE cleanup
- Gracefully skips if PostgreSQL is unavailable

---

## How to Run

### Prerequisites
- Java 17+, Maven 3.8+
- PostgreSQL (optional — DB tests skip gracefully without it)

### Run All Tests
```bash
mvn test
```

### Run by Tags
```bash
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@qa"
mvn test -Dcucumber.filter.tags="@regression and not @db"
mvn test -Dcucumber.filter.tags="@users"
mvn test -Dcucumber.filter.tags="@posts"

# Maven profiles
mvn test -P smoke
mvn test -P qa
```

### Switch Environments
```bash
mvn test -Denv=dev
mvn test -Denv=qa     # default
```

### Generate Allure Report
```bash
mvn test
mvn allure:serve       # Opens in browser
```

### Jenkins Pipeline
```groovy
pipeline {
    agent any
    parameters {
        choice(name: 'ENV', choices: ['qa', 'dev'], description: 'Environment')
        choice(name: 'TAGS', choices: ['@smoke', '@regression', '@all'], description: 'Tags')
    }
    stages {
        stage('Test') {
            steps {
                sh "mvn test -Denv=${params.ENV} -Dcucumber.filter.tags=\"${params.TAGS}\""
            }
        }
        stage('Report') {
            steps {
                allure includeProperties: false, results: [[path: 'target/allure-results']]
            }
        }
    }
}
```

---

## Database Setup (Optional)

If you want to run the DB scenarios with a real PostgreSQL:

```sql
CREATE DATABASE testdb;
CREATE USER testuser WITH PASSWORD 'testpass';
GRANT ALL ON DATABASE testdb TO testuser;

-- Create test table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    name VARCHAR(200),
    email VARCHAR(200)
);
```

Then update `config-qa.properties` with your DB credentials.

---

## How a Real API Test Works (Mental Model)

```
1. PREPARE   → Set up test data (DB insert or API POST)
2. BUILD     → Construct URL + headers + auth + params + body
3. EXECUTE   → Send HTTP request
4. EXTRACT   → Pull fields from JSON response
5. VALIDATE  → Assert status code, field values, response time
6. CROSS-CHECK → Compare API response vs DB or vs another API call
7. CLEANUP   → Delete test data (DB delete or API DELETE)
```

---

## Practice Exercises

### Beginner
1. Add a scenario to GET `/users/2` and validate the name field
2. Add a POST scenario creating a post with userId=2
3. Run only your scenario using a custom tag

### Intermediate
4. Add a new feature `comments_api.feature` for `/comments` endpoint
5. Implement header-based authentication scenarios
6. Add JSON Schema validation (schema files in `src/test/resources/schemas/`)

### Advanced
7. Implement retry logic for flaky API responses
8. Add data-driven tests using Scenario Outline with Examples table
9. Chain 3+ API calls: create user → create post for user → get comments → validate
10. Set up PostgreSQL in Docker and run the full DB validation scenarios
11. Add custom Allure annotations for API method categorization
