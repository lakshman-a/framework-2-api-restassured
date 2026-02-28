// ============================================================================
// PIPELINE: REST Assured + Cucumber API Tests (ENHANCED)
// ============================================================================
// Features:
//   - Allure Report integrated and visible in Jenkins sidebar
//   - Cucumber Report integrated and visible in Jenkins sidebar
//   - Both reports zipped and attached to email
//   - Pass/Fail summary extracted and shown in email body
//   - Pipeline stages shown visually in Jenkins (Stage View)
//   - Email sent on EVERY build (success, failure, unstable)
// ============================================================================

pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH',
            defaultValue: 'master',
            description: 'Git branch to build (type any branch: master, develop, feature/xyz)'
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
            description: 'Custom tag expression (overrides TEST_TAGS if filled)'
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

        // ── Stage 1: Environment Setup ──────────────────────────────────
        stage('Environment Setup') {
            steps {
                bat 'git config --global core.longpaths true'
                cleanWs()
                echo """
                ========================================
                   REST Assured API Test Pipeline
                ========================================
                  Branch:      ${params.BRANCH ?: 'master'}
                  Environment: ${params.ENVIRONMENT ?: 'qa'}
                  Tags:        ${EFFECTIVE_TAGS}
                  Build:       #${env.BUILD_NUMBER}
                ========================================
                """
            }
        }

        // ── Stage 2: Checkout Code ──────────────────────────────────────
        stage('Checkout') {
            steps {
                git branch: "${params.BRANCH ?: 'master'}",
                    url: "${REPO_URL}",
                    credentialsId: 'github-pat'
            }
        }

        // ── Stage 3: Verify Tools ───────────────────────────────────────
        stage('Verify Tools') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
            }
        }

        // ── Stage 4: Compile ────────────────────────────────────────────
        stage('Compile') {
            steps {
                bat 'mvn clean compile -DskipTests'
            }
        }

        // ── Stage 5: Run Tests ──────────────────────────────────────────
        stage('Run API Tests') {
            steps {
                bat "mvn test -Denv=${params.ENVIRONMENT ?: 'qa'} \"-Dcucumber.filter.tags=${EFFECTIVE_TAGS}\" -Dmaven.test.failure.ignore=true"
            }
        }

        // ── Stage 6: Collect Test Results ───────────────────────────────
        stage('Collect Results') {
            steps {
                // JUnit results - gives pass/fail count in Jenkins UI
                junit(
                    testResults: 'target/surefire-reports/*.xml',
                    allowEmptyResults: true
                )
            }
        }

        // ── Stage 7: Publish Cucumber Report ────────────────────────────
        stage('Cucumber Report') {
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

        // ── Stage 8: Publish Allure Report ──────────────────────────────
        stage('Allure Report') {
            steps {
                allure(
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'target/allure-results']]
                )
            }
        }

        // ── Stage 9: Package Reports for Email ─────────────────────────
        stage('Package Reports') {
            steps {
                script {
                    // Create directory for zipped reports
                    bat 'if not exist "target\\email-reports" mkdir target\\email-reports'

                    // Zip Cucumber HTML report using jar (available with JDK)
                    bat '''
                        if exist "target\\cucumber-reports" (
                            cd target\\cucumber-reports
                            jar -cfM ..\\email-reports\\Cucumber-Report.zip .
                            cd ..\\..
                        )
                    '''

                    // Zip Allure results
                    bat '''
                        if exist "target\\allure-results" (
                            cd target\\allure-results
                            jar -cfM ..\\email-reports\\Allure-Results.zip .
                            cd ..\\..
                        )
                    '''

                    // Extract test summary from Cucumber JSON
                    env.TEST_SUMMARY_HTML = extractTestSummary()
                }
            }
        }
    }

    // ── Post Actions ────────────────────────────────────────────────────
    post {
        always {
            echo "=== Pipeline finished: ${currentBuild.currentResult} ==="

            archiveArtifacts(
                artifacts: 'target/cucumber-reports/**,target/allure-results/**,target/email-reports/**,target/logs/**',
                allowEmptyArchive: true
            )

            emailext(
                subject: "${currentBuild.currentResult == 'SUCCESS' ? 'PASSED' : 'FAILED'}: API Tests - Build #${env.BUILD_NUMBER} [${EFFECTIVE_TAGS}]",
                body: """
                    <div style='font-family: Arial, sans-serif; max-width: 700px;'>

                    <h2 style='color: ${currentBuild.currentResult == "SUCCESS" ? "#28a745" : "#dc3545"};'>
                        ${currentBuild.currentResult == "SUCCESS" ? "PASSED" : "FAILED"} - API Test Results
                    </h2>

                    <table border='1' cellpadding='10' cellspacing='0' style='border-collapse:collapse; width:100%;'>
                        <tr style='background:#333; color:white;'>
                            <th colspan='2'>Build Information</th>
                        </tr>
                        <tr><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
                        <tr><td><b>Build</b></td><td><a href='${env.BUILD_URL}'>#${env.BUILD_NUMBER}</a></td></tr>
                        <tr style='background:${currentBuild.currentResult == "SUCCESS" ? "#d4edda" : currentBuild.currentResult == "UNSTABLE" ? "#fff3cd" : "#f8d7da"};'>
                            <td><b>Status</b></td><td><b>${currentBuild.currentResult}</b></td></tr>
                        <tr><td><b>Branch</b></td><td>${params.BRANCH ?: 'master'}</td></tr>
                        <tr><td><b>Environment</b></td><td>${params.ENVIRONMENT ?: 'qa'}</td></tr>
                        <tr><td><b>Tags</b></td><td><code>${EFFECTIVE_TAGS}</code></td></tr>
                        <tr><td><b>Duration</b></td><td>${currentBuild.durationString}</td></tr>
                    </table>

                    <br/>

                    ${env.TEST_SUMMARY_HTML ?: '<p><i>Test summary not available (tests may not have run).</i></p>'}

                    <br/>
                    <h3>View Reports in Jenkins</h3>
                    <table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse;'>
                        <tr>
                            <td><a href='${env.BUILD_URL}cucumber-html-reports/overview-features.html'>Cucumber Report</a></td>
                            <td><a href='${env.BUILD_URL}allure/'>Allure Report</a></td>
                            <td><a href='${env.BUILD_URL}console'>Console Log</a></td>
                            <td><a href='${env.BUILD_URL}artifact/'>Build Artifacts</a></td>
                        </tr>
                    </table>

                    <br/>
                    <p style='color:#666; font-size:12px;'>
                        Cucumber and Allure reports are attached as ZIP files.<br/>
                        Triggered by: ${currentBuild.getBuildCauses()[0]?.shortDescription ?: 'Manual'}
                    </p>

                    </div>
                """,
                to: 'a.lakshman1991@gmail.com',
                mimeType: 'text/html',
                attachLog: true,
                attachmentsPattern: 'target/email-reports/*.zip'
            )
        }

        success {
            echo 'All API tests passed!'
        }

        unstable {
            echo 'Some tests failed. Check the reports.'
        }

        failure {
            echo 'Pipeline failed. Check console output.'
        }
    }
}

// ============================================================================
// HELPER: Extract pass/fail summary from Cucumber JSON for email
// ============================================================================
def extractTestSummary() {
    try {
        def jsonFiles = findFiles(glob: 'target/cucumber-reports/*.json')
        if (jsonFiles.length == 0) {
            return '<p><i>No Cucumber results found.</i></p>'
        }

        def jsonContent = readFile(file: jsonFiles[0].path, encoding: 'UTF-8')
        def features = readJSON(text: jsonContent)

        int totalScenarios = 0
        int passedScenarios = 0
        int failedScenarios = 0
        int totalSteps = 0
        int passedSteps = 0
        int failedSteps = 0
        def featureResults = []

        features.each { feature ->
            int fPass = 0
            int fFail = 0

            feature.elements?.each { scenario ->
                if (scenario.type == 'scenario') {
                    totalScenarios++
                    boolean passed = true

                    scenario.steps?.each { step ->
                        totalSteps++
                        if (step.result?.status == 'passed') {
                            passedSteps++
                        } else if (step.result?.status == 'failed') {
                            failedSteps++
                            passed = false
                        }
                    }

                    if (passed) { passedScenarios++; fPass++ }
                    else { failedScenarios++; fFail++ }
                }
            }

            featureResults.add([name: feature.name ?: 'Unknown', passed: fPass, failed: fFail])
        }

        int passRate = totalScenarios > 0 ? Math.round((passedScenarios * 100.0) / totalScenarios) : 0

        def html = """
            <h3>Test Execution Summary</h3>
            <table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse; width:100%;'>
                <tr style='background:#333; color:white;'><th>Metric</th><th>Count</th></tr>
                <tr><td><b>Total Scenarios</b></td><td><b>${totalScenarios}</b></td></tr>
                <tr style='background:#d4edda;'><td>Passed</td><td><b>${passedScenarios}</b></td></tr>
                <tr style='background:${failedScenarios > 0 ? "#f8d7da" : "#d4edda"};'>
                    <td>Failed</td><td><b>${failedScenarios}</b></td></tr>
                <tr><td>Total Steps</td><td>${totalSteps} (${passedSteps} passed, ${failedSteps} failed)</td></tr>
                <tr style='background:#e2e3e5;'><td><b>Pass Rate</b></td><td><b>${passRate}%</b></td></tr>
            </table>
        """

        if (featureResults.size() > 0) {
            html += """
                <br/>
                <h3>Results by Feature</h3>
                <table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse; width:100%;'>
                    <tr style='background:#333; color:white;'><th>Feature</th><th>Passed</th><th>Failed</th><th>Status</th></tr>
            """
            featureResults.each { f ->
                def bg = f.failed > 0 ? '#f8d7da' : '#d4edda'
                def icon = f.failed > 0 ? 'FAIL' : 'PASS'
                html += "<tr style='background:${bg};'><td>${f.name}</td><td>${f.passed}</td><td>${f.failed}</td><td><b>${icon}</b></td></tr>"
            }
            html += '</table>'
        }

        return html

    } catch (Exception e) {
        echo "Warning: Could not parse test summary: ${e.message}"
        return "<p><i>Test summary could not be generated: ${e.message}</i></p>"
    }
}
