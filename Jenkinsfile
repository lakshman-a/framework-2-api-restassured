// ============================================================================
// PIPELINE: REST Assured + Cucumber API Tests
// ============================================================================
// This pipeline:
//   1. Checks out code from GitHub
//   2. Builds with Maven
//   3. Runs API tests with user-selected tags/environment
//   4. Publishes Cucumber + Allure reports
//   5. Emails test results
// ============================================================================

pipeline {
    agent any

    // ── Tool Configuration ──────────────────────────────────────────────
    // These must match the names configured in Jenkins Global Tool Config
    // Manage Jenkins → Tools → Maven installations / JDK installations
    tools {
        maven 'Maven-3'    // Name of your Maven installation in Jenkins
        jdk   'JDK-17'     // Name of your JDK installation in Jenkins
    }

    // ── Parameters (shown as dropdowns when user clicks "Build with Parameters") ─
    parameters {
        choice(
            name: 'BRANCH',
            choices: ['develop', 'main', 'feature/api-tests'],
            description: 'Git branch to checkout'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['qa', 'dev'],
            description: 'Target environment (loads config-{env}.properties)'
        )
        choice(
            name: 'TEST_TAGS',
            choices: ['@smoke', '@regression', '@all', '@qa', '@users', '@posts', '@smoke and @qa', 'not @db'],
            description: 'Cucumber tags to filter which scenarios run'
        )
        string(
            name: 'CUSTOM_TAGS',
            defaultValue: '',
            description: 'Custom tag expression (overrides TEST_TAGS if provided). Example: @smoke and not @db'
        )
    }

    // ── Environment Variables ────────────────────────────────────────────
    environment {
        // Determine which tags to use: custom if provided, otherwise dropdown
        EFFECTIVE_TAGS = "${params.CUSTOM_TAGS ? params.CUSTOM_TAGS : params.TEST_TAGS}"
        // GitHub repo
        GIT_REPO = 'https://github.com/lakshman-a/SHSF.git'
        // Email recipients (comma-separated)
        EMAIL_RECIPIENTS = 'your-email@example.com'
    }

    // ── Stages ──────────────────────────────────────────────────────────
    stages {

        stage('Checkout') {
            steps {
                echo "Checking out branch: ${params.BRANCH}"
                git branch: "${params.BRANCH}",
                    url: "${GIT_REPO}"
            }
        }

        stage('Build & Resolve Dependencies') {
            steps {
                dir('framework-2-api-restassured') {
                    echo "Resolving Maven dependencies..."
                    sh 'mvn clean compile -DskipTests -q'
                }
            }
        }

        stage('Run API Tests') {
            steps {
                dir('framework-2-api-restassured') {
                    echo "Running tests with tags: ${EFFECTIVE_TAGS} | Environment: ${params.ENVIRONMENT}"
                    sh """
                        mvn test \
                            -Denv=${params.ENVIRONMENT} \
                            -Dcucumber.filter.tags="${EFFECTIVE_TAGS}" \
                            -Dmaven.test.failure.ignore=true
                    """
                }
            }
        }

        stage('Publish Reports') {
            steps {
                dir('framework-2-api-restassured') {
                    // ── Cucumber Report ──
                    cucumber(
                        buildStatus: 'UNSTABLE',
                        jsonReportDirectory: 'target/cucumber-reports',
                        fileIncludePattern: '**/*.json',
                        trendsLimit: 10,
                        classifications: [
                            [key: 'Environment', value: "${params.ENVIRONMENT}"],
                            [key: 'Tags', value: "${EFFECTIVE_TAGS}"],
                            [key: 'Branch', value: "${params.BRANCH}"]
                        ]
                    )

                    // ── Allure Report ──
                    allure(
                        includeProperties: false,
                        jdk: '',
                        results: [[path: 'target/allure-results']]
                    )
                }
            }
        }
    }

    // ── Post Actions (always run) ───────────────────────────────────────
    post {
        always {
            echo "Pipeline completed with status: ${currentBuild.currentResult}"

            // ── Archive Artifacts ──
            dir('framework-2-api-restassured') {
                archiveArtifacts(
                    artifacts: 'target/cucumber-reports/**,target/allure-results/**,target/logs/**',
                    allowEmptyArchive: true
                )
            }

            // ── Email Notification ──
            emailext(
                subject: "[Jenkins] API Tests ${currentBuild.currentResult} - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    <h2>API Test Automation Results</h2>
                    <table border="1" cellpadding="8" cellspacing="0">
                        <tr><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
                        <tr><td><b>Build #</b></td><td>${env.BUILD_NUMBER}</td></tr>
                        <tr><td><b>Status</b></td><td>${currentBuild.currentResult}</td></tr>
                        <tr><td><b>Branch</b></td><td>${params.BRANCH}</td></tr>
                        <tr><td><b>Environment</b></td><td>${params.ENVIRONMENT}</td></tr>
                        <tr><td><b>Tags</b></td><td>${EFFECTIVE_TAGS}</td></tr>
                        <tr><td><b>Duration</b></td><td>${currentBuild.durationString}</td></tr>
                    </table>
                    <br/>
                    <p><b>Reports:</b></p>
                    <ul>
                        <li><a href="${env.BUILD_URL}cucumber-html-reports/overview-features.html">Cucumber Report</a></li>
                        <li><a href="${env.BUILD_URL}allure/">Allure Report</a></li>
                        <li><a href="${env.BUILD_URL}console">Console Output</a></li>
                    </ul>
                """,
                to: "${EMAIL_RECIPIENTS}",
                mimeType: 'text/html',
                attachLog: true
            )
        }

        success {
            echo '✅ All API tests passed!'
        }

        unstable {
            echo '⚠️ Some tests failed. Check the reports.'
        }

        failure {
            echo '❌ Pipeline failed. Check the console output.'
        }
    }
}
