#!/usr/bin/groovy

def call (String TARGET_BRANCH) {

SHORT_GIT_COMMIT_ID = sh (
script: "git rev-parse --short ${TARGET_BRANCH}",
returnStdout: true
).trim()

return SHORT_GIT_COMMIT_ID
}
