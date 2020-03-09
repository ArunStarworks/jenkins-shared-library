#!/usr/bin/groovy
 
@Library([ 'allocations-shared-library@master', 'netcore-shared-library@master', 'azure-shared-library@master']) _;
 
import com.tjx.netcore.PlatformType;
import com.tjx.netcore.BuildConfiguration;
import com.tjx.utilities.IncrementType;
import com.tjx.selenium.Browser;
import com.tjx.selenium.ExecutionEnvironment;
 
def repoTag
def gitCommitId
def currentTag
 
def call(Map templateParams) {
    
    def NEUTRAL_COLOUR = "background-color:#99CCFF"
 
    // SonarQube Parameters for a .NET Core Project
    def sonarQubeParameters = [:]
        sonarQubeParameters.sonarQubeEnvironment = "SonarQube"
        sonarQubeParameters.projectKey = "sonar:allocations-${templateParams.application}"
        sonarQubeParameters.projectName = "Allocations-${templateParams.application}"
        sonarQubeParameters.sonarHostUrl = "https://sonarqube.tjx.com"
        sonarQubeParameters.sonarCredsId = "ALLOCATIONS_SONAR_TOKEN"
        sonarQubeParameters.sonarExclusions = "**/*.js,**/*.dll,**/*.cshtml,**/*.svg,**/*.png,**/*.xml,**/*.md,**/*.ico" + "," + "${templateParams.sonarExclusions}"
        sonarQubeParameters.coverageReportPath = "${templateParams.unitTestsProject}/coverage.opencover.xml"
 
    def targetPlatform = "${templateParams.targetPlatform}"
 
    // Environment & Application information
 
    def sandbox_rg = "innovate-nprod-sandbox-eastus2-allocation01-rg"
    def iacdev_rg  = "innovate-nprod-iac-devtest-eastus2-allocation01-rg"
    def dev_rg = "planalloc-nprod-dev01-eastus2-allocations-rg"
    def qa_rg  = "planalloc-nprod-qa01-eastus2-allocations-rg"
    def test_rg = "planalloc-nprod-test01-eastus2-allocations-rg"
    def sandbox_sp = 'TJX-Azure-Sandbox-SP'
    def sandbox_subscription_id = 'TJX-INNOVATE-SANDBOX'
    def iacnonprod_subscription_id = 'TJX-INNOVATE-IAC-DEVTEST'
    def nonprod_subscription_id = 'TJX-PLANALLOC-NON-PROD'
 
    def productLine = 'alloc'
    def region = 'eus2'
    
    def environments = ["dev", "qa", "test", "stage", "prod"]
    def nonprod_spn = "TJX-Azure-Dev-SP"
    def test_spn = "TJX-Azure-Test-SP"
    def appservice_productline = "${templateParams.appServiceProductLine}" // E.g. allocations
    def appservice_applicationGroup = "${templateParams.appServiceAppGroup}" // E.g. ADA, ItemPools, OTS
    def appservice_application = "${templateParams.appServiceApp}" // E.g. auditdb, refdb, blobstorage
    def appservice_type = "${templateParams.appServiceType}" // Can take values ui, service, processing or ingestion
  
    //  Environment Dashboard
    def _appgroup = "${templateParams.appgroup}" // E.g. ip
    def _component = "${templateParams.component}" // E.g. frontend
    def _azureservice = "${templateParams.azureservice}" // webapp
    def enviro = 'abc'
    def component = "${_appgroup}-${_component}-${_azureservice}"
    def package_name = 'package_name'
    def passed_build ='job/AllocationsModernisation/'
    def status = 'pass'
    def dash_update_job = '/AllocationsModernisation/NorthStarPipelines/dashboardupdate'
 
    // Selenium Grid
    def network='jenkins-selenium-test-${JOB_NAME}-${BUILD_NUMBER}'
    def seleniumHub='selenium-hub-${BUILD_NUMBER}'
    def chrome="chrome-${BUILD_NUMBER}"
    def firefox="firefox-${BUILD_NUMBER}"
    def containertest="conatinertest-${BUILD_NUMBER}"
 
    pipeline {
        agent 'none'
 
        triggers {
            bitbucketPush()
        }
 
        options {
            ansiColor('xterm')
            buildDiscarder(logRotator(numToKeepStr: '20', daysToKeepStr: '14'))
            timestamps()
        }
 
        environment {
 
            // Agent Details
            AGENT = "allocations"
            REGISTRY = "itnpsharedhubacr01.azurecr.io"
            REGISTRYORG = "buildtools"
            ONE_SHOT_IMAGE = "appmod-dotnetcore-slave:latest"
            CREDENTIALS_ID = "TJX-Azure-Dev-ID"
            ONE_SHOT_DOCKER_IMAGE = "appmod-dockerci-slave:latest"
            ONE_SHOT_FRAMEWORK_IMAGE = "appmod-python-slave:latest"
 
          	FAILED_STAGE="Initialisation"
            LOGGING_FILE = "${env.JOB_NAME}-log.log"
            LOGGING_LEVEL = "normal"
            STASH_PATTERN = "*.zip"
            UNIT_TEST_FILTER = "*"
            
            ARTIFACTORY_SERVER_URL = "https://jfrog.tjx.com/artifactory"
            ALLOCATIONS_SERVICE_ACCOUNT_ID = "78df5b40-de6c-4c90-b94c-e1c5855b2f64"
 
            BITBUCKET_URL = "https://bitbucket.tjx.com"
            TENANT_ID = "2242945a-4ab9-4132-840e-cce1c66e31bb"
            
            // Git Tagging Variables
            GIT_CONFIG_USER_NAME  = 'jenkins'
            GIT_CONFIG_USER_EMAIL = 'jenkins@bitbucket.tjx.com'
            SSH_AGENT_ID = 'allocationsmodernisation-privatekey'
 
 
            SYSTEMS_TEAM_EMAIL = '6ca73d7b.tjxinc.onmicrosoft.com@amer.teams.ms,cc:ML-Allocations-SystemTeam@tjx.com'
            APPROVER_EMAILS = 'ana_salort@tjx.com,coreen_tremblay@tjx.com'
            PIPELINE_STATUS_EMAIL = 'ef5f6c17.tjxinc.onmicrosoft.com@amer.teams.ms,cc:ML-Allocations-SystemTeam@tjx.com'
            COLOUR = "background-color:#f04b2d"
 
            //Selenium Grid Variables
            GRID_PORT= "${templateParams.port}"
            SELENIUM_VERSION= "${templateParams.seleniumVersion}"
 
            ALM_CREDENTIALS_ID = "ALLOCATIONS_ALM"
            ALM_TEST_FRAMEWORK = ".NET Core NUnit"
            PROJECT_NAME = "Allocations"
            BASE_TEST_FOLDER = "Allocations\\ItemPools\\${templateParams.appName}"
            BASE_TEST_SET_FOLDER = "Allocations\\ItemPools\\${templateParams.appName}"
 
            // Created 2 variables for this in-case we want to change it in the future.
            SMOKE_TEST_FILTER = "${templateParams.smokeTestFilter}"
            SMOKE_TEST_STORY  = "${templateParams.smokeTestFilter}"
            SMOKETEST_SERVICE_USER = "QAAL010"
 
            IACDEV_EVIDENCE_STORAGE_ACCOUNT = "${productLine}np${region}iacdevtestevd"
            IACDEV_EVIDENCE_CONTAINER_NAME = "smoketests"
 
            // Test Automation Framework Link
            TEST_AUTO_FRAMEWORK = "ssh://git@bitbucket.tjx.com/al/${templateParams.testAutoFrameworkRepo}.git"
        }
 
        stages {
            stage ('.NET Core Service CI Process'){
                agent {
                    docker {
                        label "${env.AGENT}"
                        image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_IMAGE}"
                        registryUrl "https://${env.REGISTRY}"
                        registryCredentialsId "${env.CREDENTIALS_ID}"
                        args "-v /etc/passwd:/etc/passwd"
                    }
                }
                stages {
 
                    stage('Application Build & Unit Testing'){
                      steps {
                        script {
                            gitCommitId = "${env.GIT_COMMIT}"
                        }
                        netCoreTest("TestResults","${templateParams.unitTestsProject}","opencover", "./","${env.UNIT_TEST_FILTER}")
                        dotnetSonarScanBegin(sonarQubeParameters)
                        netCoreBuild("${env.LOGGING_LEVEL}","${env.LOGGING_FILE}")
                        script {
                          if ( env.GIT_BRANCH =~ ".*origin/master.*" || env.GIT_BRANCH =~ "master" || env.GIT_BRANCH =~ ".*origin/release.*" || env.GIT_BRANCH =~ "release/.*" ){
                            echo "Uploading Unit Test Results to ALM as we are on the master branch"
                            hpAlmReportUpload("${env.PROJECT_NAME}", "${env.BASE_TEST_FOLDER}\\UnitTestResults", "${env.BASE_TEST_SET_FOLDER}\\UnitTestResults", "${env.ALM_CREDENTIALS_ID}", "${env.ALM_TEST_FRAMEWORK}", "**/TestResults.xml")
                          }
                        }
                      }
                    }
				
 
                    stage('Static Code Analysis') {
                      steps{
                        script {
                          FAILED_STAGE="${env.STAGE_NAME}"
                        }
                        dotnetSonarScanEnd(sonarQubeParameters)
                      }
                    }
 
                    stage ('Increment Version'){
                        steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                                if ( env.GIT_BRANCH =~ ".*origin/master.*" || env.GIT_BRANCH =~ "master" ){
                                    echo "On master"
                                    currentTag = semanticVersioningGit( IncrementType.MINOR, "${env.GIT_CONFIG_USER_NAME}", "${env.GIT_CONFIG_USER_EMAIL}", "${env.SSH_AGENT_ID}")     
                                }
                                else if(  env.GIT_BRANCH =~ ".*origin/feature.*" || env.GIT_BRANCH =~ "feature/.*" || env.GIT_BRANCH =~ "PR-.*" ){
                                    echo "On a feature branch"
                                    currentTag = semanticVersioningGit( IncrementType.BUILD_NUMBER, "${env.GIT_CONFIG_USER_NAME}", "${env.GIT_CONFIG_USER_EMAIL}", "${env.SSH_AGENT_ID}")
                                }
                                else if( env.GIT_BRANCH =~ ".*origin/release.*" || env.GIT_BRANCH =~ "release/.*" ){
                                    echo "On a release branch"
                                    currentTag = semanticVersioningGit( IncrementType.PATCH, "${env.GIT_CONFIG_USER_NAME}", "${env.GIT_CONFIG_USER_EMAIL}", "${env.SSH_AGENT_ID}")
                                }
                                env.LATEST_TAG = "${currentTag.toString()}"                            
                            }
                        }
                    }
            
                    stage('Push Versioned Artifact'){
                        steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                                TARGET_BRANCH = getBranchNameMultiBranch("${env.GIT_CONFIG_USER_NAME}", "${env.GIT_CONFIG_USER_EMAIL}", "${env.SSH_AGENT_ID}")
                            }
                            netCorePublish("./app", "normal", "${templateParams.application}-log.log",  "${templateParams.targetPlatform}", BuildConfiguration.Release.value())
                            script {
                                if ( env.GIT_BRANCH =~ ".*origin/master.*" || env.GIT_BRANCH =~ "master" || env.GIT_BRANCH =~ ".*origin/release.*" || env.GIT_BRANCH =~ "release/.*" ){
                                    echo "on a master branch"
                                    artifactoryPush("${env.ARTIFACTORY_SERVER_URL}","${env.ALLOCATIONS_SERVICE_ACCOUNT_ID}", "${templateParams.application}", "./app.zip", "AllocationsModernisation-Services/${templateParams.application}-releases/", "zip", TARGET_BRANCH)
                                }
                                else if(  env.GIT_BRANCH =~ ".*origin/feature.*" || env.GIT_BRANCH =~ "feature/.*" || env.GIT_BRANCH =~ "PR-.*" ){
                                    echo "on a feature branch"
                                    artifactoryPush("${env.ARTIFACTORY_SERVER_URL}","${env.ALLOCATIONS_SERVICE_ACCOUNT_ID}", "${templateParams.application}", "./app.zip", "AllocationsModernisation-Services/${templateParams.application}-development/", "zip", TARGET_BRANCH)
                                }
                            }
                            // Stash the relevant zip file that is needed for deployment.
                            stash allowEmpty: false, includes: "${env.STASH_PATTERN}", name: "serviceartifact"
                        }
                    }
                }
            }
            
            stage('Sandbox Deployment'){
                when {
                    expression {
                        env.GIT_BRANCH =~ ".*origin/master.*|master|origin/feature.*|feature/.*|PR-.*"
                    }
                }
                agent {
                    docker {
                        label "${env.AGENT}"
                        image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_DOCKER_IMAGE}"
                        registryUrl "https://${env.REGISTRY}"
                        registryCredentialsId "${env.CREDENTIALS_ID}"
                        args "-v /etc/passwd:/etc/passwd"
                    }
                }
                stages {
                    stage('Deploy to Sandbox CI'){
                       steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                                enviro = "Sandbox CI"
                            }
                            unstash "serviceartifact"
                            echo "Deploy to Sandbox CI"
                            deployZipToAppService("${sandbox_sp}", "${productLine}${region}sbx01${templateParams.appName}", "${sandbox_subscription_id}", "${sandbox_rg}", "${templateParams.application}", "${env.LATEST_TAG}")
                            dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
                        }
                    }
 
                    stage ('Smoke Test Sandbox CI') {
                        steps {
                          script {
                              FAILED_STAGE="${env.STAGE_NAME}"
                          }
                          echo "PLACEHOLDER - Smoke Test the CI Deployment"
                        }
                    }
                }
            }
 
            stage('Deploy to IAC Dev and run Smoke Tests'){
 
                agent 'none'
                options{
                    lock(quantity: 1, resource: "${templateParams.application}")
                }
                
                stages {
                    stage('IAC Dev Deployment') {
                
                        agent {
                            docker {
                                label "${env.AGENT}"
                                image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_DOCKER_IMAGE}"
                                registryUrl "https://${env.REGISTRY}"
                                registryCredentialsId "${env.CREDENTIALS_ID}"
                                args "-v /etc/passwd:/etc/passwd"
                            }
                        }
                
                        stages {
                            stage('Deploy to IACDev Environment'){
                                when {
                                    expression {
                                        env.GIT_BRANCH =~ ".*origin/master.*|master|origin/feature.*|feature/.*|PR-.*"                                        
                                    }
                                }
                                steps {
                                        script {
                                            FAILED_STAGE="${env.STAGE_NAME}"
                                            enviro = "IAC Dev"
                                        }
                                        echo "Deploying to IAC Dev Environment"
                                        emailNotifications("${env.SYSTEMS_TEAM_EMAIL}", "APPROVAL NEEDED: Deploy to IAC Dev Environment: Job ${env.JOB_NAME}:${env.BUILD_NUMBER}", "${env.COLOUR}")
                                        timeout(time: 1, unit: 'HOURS'){     
                                            input 'Deploy to IAC Dev Environment?'
                                        }
                                        unstash "serviceartifact"
                                        deployZipToAppService("${sandbox_sp}", "${templateParams.iacDevAppServiceGroup}-${templateParams.iacDevAppServiceApp}-${templateParams.iacDevAppServiceType}", "${iacnonprod_subscription_id}", "${iacdev_rg}", "${templateParams.application}", "${env.LATEST_TAG}")
                                        dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
                                }
                            }
                        }
                    }
 
                    stage ('Smoke Testing'){
                        when {
                            expression {
                                env.GIT_BRANCH =~ ".*origin/master.*|master|origin/feature.*|feature/.*|PR-.*" && templateParams.testAutoFrameworkRepo != null
                            }
                        }
                        agent {
                            docker {
                                label "${env.AGENT}"
                                image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_FRAMEWORK_IMAGE}"
                                registryUrl "https://${env.REGISTRY}"
                                registryCredentialsId "${env.CREDENTIALS_ID}"
                                args "-v /etc/passwd:/etc/passwd"
                            }
                        }
                        stages {
                            stage('API Smoke Test'){
                                steps {
                                    script {
                                        gitCommitId = "${env.GIT_COMMIT}"
                                    }
                                    sshagent(['allocationsmodernisation-privatekey']) {
                                        sh """#!/bin/bash
                                            GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" git clone ${env.TEST_AUTO_FRAMEWORK} ${templateParams.testAutoFrameworkRepo}
                                        """
                                    }
                                    azureLogin("${env.SMOKETEST_SERVICE_USER}", "${iacnonprod_subscription_id}","User")
                                    seleniumRun("selenium-hub-allocations", "${env.SMOKETEST_SERVICE_USER}", "4444", "${env.SMOKE_TEST_FILTER}", "${templateParams.testAutoFrameworkRepo}/${templateParams.testAutoFrameworkRepo}", "iacd01" , Browser.None, ExecutionEnvironment.Cloud)
                                    stash allowEmpty: false, includes: "FunctionalTestResults.xml", name: "smoke_test_xml"
                                    stash allowEmpty: false, includes: "${templateParams.testAutoFrameworkRepo}/${templateParams.testAutoFrameworkRepo}/Report/", name: "api_smoke_test_report"                                                            
                                }
                            }
                        }
                    }
                }
            }
 
            stage ('Upload Smoke Test Results'){
                when {
                    expression {
                        env.GIT_BRANCH =~ ".*origin/master.*|master|origin/feature.*|feature/.*|PR-.*" && templateParams.testAutoFrameworkRepo != null
                    }
                }
                agent {
                    docker {
                        label "${env.AGENT}"
                        image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_DOCKER_IMAGE}"
                        registryUrl "https://${env.REGISTRY}"
                        registryCredentialsId "${env.CREDENTIALS_ID}"
                        args "-v /etc/passwd:/etc/passwd"
                    }
                }
                stages {
                    stage('Upload Smoke Test Results to ALM & Blob Storage'){
                        steps {
                            script {
                                gitCommitId = "${env.GIT_COMMIT}"
                            }
                            unstash "api_smoke_test_report"
                            unstash "smoke_test_xml"
                            sh "zip -r report.zip ./${templateParams.testAutoFrameworkRepo}/${templateParams.testAutoFrameworkRepo}/Report/"
                            archiveArtifacts artifacts: "report.zip", fingerprint: true
                            script {
                                if ( env.GIT_BRANCH =~ ".*origin/master.*" || env.GIT_BRANCH =~ "master" || env.GIT_BRANCH =~ ".*origin/release.*" || env.GIT_BRANCH =~ "release/.*" || env.GIT_BRANCH =~ ".*origin/feature.*" || env.GIT_BRANCH =~ "feature/.*" || env.GIT_BRANCH =~ "PR-.*" ){
                                    echo "Uploading Functional Test Results to ALM"
                                    hpAlmReportUpload("${env.PROJECT_NAME}", "${env.BASE_TEST_FOLDER}\\SmokeTestResults\\${env.SMOKE_TEST_STORY}", "${env.BASE_TEST_SET_FOLDER}\\SmokeTestResults\\${env.SMOKE_TEST_STORY}", "${env.ALM_CREDENTIALS_ID}", "${env.ALM_TEST_FRAMEWORK}", "**/FunctionalTestResults.xml")
                                }
                            }
                            uploadToBlob("${sandbox_sp}", "${iacnonprod_subscription_id}", "${env.IACDEV_EVIDENCE_CONTAINER_NAME}", "${templateParams.iacDevAppServiceGroup}-${templateParams.iacDevAppServiceApp}-${templateParams.iacDevAppServiceType}", "${templateParams.iacDevAppServiceGroup}-${templateParams.iacDevAppServiceApp}-${templateParams.iacDevAppServiceType}-${SMOKE_TEST_STORY}", "report.zip", "${env.IACDEV_EVIDENCE_STORAGE_ACCOUNT}", true, true)
                        }
                    }
                }
            }
 
            stage('Non-Prod Environment Deploys'){
                when {
                    expression {
                        env.GIT_BRANCH =~ ".*origin/master.*|master"
                    }
                }
                agent {
                    docker {
                        label "${env.AGENT}"
                        image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_DOCKER_IMAGE}"
                        registryUrl "https://${env.REGISTRY}"
                        registryCredentialsId "${env.CREDENTIALS_ID}"
                        args "-v /etc/passwd:/etc/passwd"
                    }
                }
                stages {
                    stage('Deploy to Dev Environment'){
                       steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                                enviro = "DEV"
                            }
                            unstash "serviceartifact"
                            echo "Deploying to Dev Environment"
                            deployZipToAppService("${nonprod_spn}", "${productLine}${region}d01${templateParams.appName}", "${nonprod_subscription_id}", "${dev_rg}", "${templateParams.application}", "${env.LATEST_TAG}")
                            dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
                        }
                    }
 
                    stage ('Smoke Test Dev') {
                        steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                            }
                            echo "PLACEHOLDER - Smoke Test the CI Deployment"
                        }
                    }
 
                    stage('Deploy to Test Environment'){
                       steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                                enviro = "TEST"
                            }
                            timeout(time: 1, unit: 'HOURS'){     
                                input 'Deploy to Test Environment?'
                            }
                            echo "Deploying to Test Environment"
                            deployZipToAppService("${test_spn}", "${productLine}${region}t01${templateParams.appName}", "${nonprod_subscription_id}", "${test_rg}", "${templateParams.application}", "${env.LATEST_TAG}")
                            dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
                        }
                    }
 
                    stage ('Smoke Test Test') {
                        steps {
                            script {
                                FAILED_STAGE="${env.STAGE_NAME}"
                            }
                            echo "PLACEHOLDER - Smoke Test the CI Deployment"
                        }
                    }
                }
            }
 
            // stage('Higher Environment (QA) Deploys'){
            //     when {
            //         expression {
            //             env.GIT_BRANCH =~ ".*origin/master.*|master|origin/release.*|release/.*"
            //         }
            //     }
            //     agent {
            //         docker {
            //             label "${env.AGENT}"
            //             image "${env.REGISTRY}/${env.REGISTRYORG}/${env.ONE_SHOT_DOCKER_IMAGE}"
            //             registryUrl "https://${env.REGISTRY}"
            //             registryCredentialsId "${env.CREDENTIALS_ID}"
            //             args "-v /etc/passwd:/etc/passwd"
            //         }
            //     }
            //     stages {
 
            //         stage('Deploy to QA Environment'){
            //             steps {
            //                 script {
            //                     FAILED_STAGE="${env.STAGE_NAME}"
            //                     enviro = "QA"
            //                 }
            //                 echo "Deploying to QA Environment"
            //                 emailNotifications("${env.APPROVER_EMAILS}", "APPROVAL NEEDED: Deploy to QA Environment: Job ${env.JOB_NAME}:${env.BUILD_NUMBER}", "${env.COLOUR}")
            //                 timeout(time: 1, unit: 'HOURS'){
            //                     input message: "Deploy to QA?", ok: "Yes, we should.", submitter: "cor00068,ana00357"
            //                 }
            //                 unstash "serviceartifact"
            //                 deployZipToAppService("${nonprod_spn}", "${productLine}${region}q01${templateParams.appName}", "${nonprod_subscription_id}", "${qa_rg}", "${templateParams.application}", "${env.LATEST_TAG}")
            //               	//dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
            //                 emailVersions("${env.SYSTEMS_TEAM_EMAIL};sandeep_sodha@tjx.com;ana_salort@tjx.com;coreen_tremblay@tjx.com;jagannath_atreya@tjx.com", "Please update the confluence page for Job: ${env.JOB_NAME}:${env.BUILD_NUMBER}", "${NEUTRAL_COLOUR}", "${templateParams.application}", "QA")
            //             }
            //         }
 
            //         stage ('Smoke Test QA') {
            //             steps {
            //                 script {
            //     			        FAILED_STAGE="${env.STAGE_NAME}"
            //   			        }
            //                 echo "PLACEHOLDER - Smoke Test the QA Deployment"
            //             }
            //         }
 
            //     }
            // }
 
 
        }
        post {
            always {
                node("${env.AGENT}") {
                    echo 'Any Post build actions would always happen in this stage'
                }
            }
            success {
                node("${env.AGENT}") {
                    echo "Pipeline Successful"
                    updateBitbucketBuildStatus("${env.ALLOCATIONS_SERVICE_ACCOUNT_ID}", "SUCCESSFUL", "${env.BITBUCKET_URL}", "${gitCommitId}")
                    echo "Updated Bitbucket Build Status"
                    emailNotifications("${env.PIPELINE_STATUS_EMAIL}", "Allocations Modernisation - Jenkins Build Success ${currentBuild.currentResult}: Job ${env.JOB_NAME}:${env.BUILD_NUMBER}", "background-color:#008000")
                }
            }
            failure {
                node("${env.AGENT}") {
                    echo "Pipeline has failed. The failed stage name: ${FAILED_STAGE}"
                    script {
                        status = "fail"
                    }
                    updateBitbucketBuildStatus("${env.ALLOCATIONS_SERVICE_ACCOUNT_ID}", "FAILED", "${env.BITBUCKET_URL}", "${gitCommitId}")
                    echo "Updated Bitbucket Build Status"
                    emailNotifications("${env.PIPELINE_STATUS_EMAIL}", "Allocations Modernisation - Jenkins Build Failure ${currentBuild.currentResult}: Job ${env.JOB_NAME}:${env.BUILD_NUMBER}", "${env.COLOUR}")
                    dashboardUpdate("${dash_update_job}", "${enviro}", "${_appgroup}-${_component}-${_azureservice}", "${passed_build}", "${status}")
                }
            }
            cleanup {
                node("${env.AGENT}") {
                    cleanWs()
                }
            }
        }
    }
}
 
 
 
