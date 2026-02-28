// ============================================================================
// PIPELINE: REST Assured + Cucumber API Tests
// Repo: https://github.com/lakshman-a/framework-2-api-restassured.git
// Jenkinsfile at repo root, code also at repo root (no subfolder needed)
// ============================================================================

pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH',
            defaultValue: 'master',
            description: 'Git branch to build (type any branch name: master, develop, feature/xyz)'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['qa', 'dev'],
            description: 'Target environment (loads config-{env}.properties)'
        )
        choice(
            name: 'TEST_TAGS',
            choices: ['@smoke', '@regression', '@all', '@qa', '@users', '@posts', 'not @db'],
            description: 'Cucumber tags to filter which scenarios run'
        )
        string(
            name: 'CUSTOM_TAGS',
            defaultValue: '',
            description: 'Custom tag expression (overrides TEST_TAGS if filled). Example: @smoke and not @db'
        )
    }

    environment {
        EFFECTIVE_TAGS = "${params.CUSTOM_TAGS ? params.CUSTOM_TAGS : (params.TEST_TAGS ?: '@smoke')}"
        REPO_URL       = 'https://github.com/lakshman-a/framework-2-api-restassured.git'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // ── FIX: Windows long filename issue ────────────────────────────
        stage('Fix Git Long Paths') {
            steps {
                // This fixes "Filename too long" on Windows
                bat 'git config --global core.longpaths true'
            }
        }

        stage('Checkout') {
            steps {
                // Clean workspace before checkout to avoid stale files
                cleanWs()
                git branch: "${params.BRANCH ?: 'master'}",
                    url: "${REPO_URL}",
                    credentialsId: 'github-pat'
            }
        }

        stage('Verify Tools') {
            steps {
                echo '=== Verifying Java and Maven ==='
                bat 'java -version'
                bat 'mvn -version'
                echo "=== Build Configuration ==="
                echo "Branch:      ${params.BRANCH ?: 'master'}"
                echo "Environment: ${params.ENVIRONMENT ?: 'qa'}"
                echo "Tags:        ${EFFECTIVE_TAGS}"
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile -DskipTests'
            }
        }

        stage('Run API Tests') {
            steps {
                bat "mvn test -Denv=${params.ENVIRONMENT ?: 'qa'} \"-Dcucumber.filter.tags=${EFFECTIVE_TAGS}\" -Dmaven.test.failure.ignore=true"
            }
        }

        stage('Publish Cucumber Report') {
            steps {
                cucumber(
                    buildStatus: 'UNSTABLE',
                    jsonReportDirectory: 'target/cucumber-reports',
                    fileIncludePattern: '**/*.json',
                    trendsLimit: 10,
                    classifications: [
                        [key: 'Branch', value: "${params.BRANCH ?: 'master'}"],
                        [key: 'Environment', value: "${params.ENVIRONMENT ?: 'qa'}"],
                        [key: 'Tags', value: "${EFFECTIVE_TAGS}"]
                    ]
                )
            }
        }
    }

    // ── Post Actions ────────────────────────────────────────────────────
    // These run INSIDE the node context so email always works,
    // even when checkout or tests fail.
    post {
        always {
            echo "=== Pipeline finished: ${currentBuild.currentResult} ==="

            // Archive whatever is available (won't fail if nothing exists)
            archiveArtifacts(
                artifacts: 'target/cucumber-reports/**,target/allure-results/**,target/logs/**',
                allowEmptyArchive: true
            )

            // ── EMAIL: Always sent, on success AND failure ──
            emailext(
                subject: "${currentBuild.currentResult}: API Tests - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>REST Assured API Test Results</h2>
                    <table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>
                        <tr style='background:#f0f0f0;'><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
                        <tr><td><b>Build</b></td><td><a href='${env.BUILD_URL}'>#${env.BUILD_NUMBER}</a></td></tr>
                        <tr style='background:${currentBuild.currentResult == "SUCCESS" ? "#d4edda" : "#f8d7da"};'>
                            <td><b>Status</b></td><td><b>${currentBuild.currentResult}</b></td></tr>
                        <tr><td><b>Branch</b></td><td>${params.BRANCH ?: 'master'}</td></tr>
                        <tr><td><b>Environment</b></td><td>${params.ENVIRONMENT ?: 'qa'}</td></tr>
                        <tr><td><b>Tags</b></td><td>${EFFECTIVE_TAGS}</td></tr>
                        <tr><td><b>Duration</b></td><td>${currentBuild.durationString}</td></tr>
                    </table>
                    <br/>
                    <p><a href='${env.BUILD_URL}console'>View Console Output</a></p>
                    <p><a href='${env.BUILD_URL}cucumber-html-reports/overview-features.html'>View Cucumber Report</a></p>
                """,
                to: 'a.lakshman1991@gmail.com',
                mimeType: 'text/html',
                attachLog: true
            )
        }

        success {
            echo 'All API tests passed!'
        }

        unstable {
            echo 'Some tests failed. Check the Cucumber report.'
        }

        failure {
            echo 'Pipeline failed. Check console output.'
        }
    }
}