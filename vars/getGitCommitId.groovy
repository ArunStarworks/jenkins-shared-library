#!/usr/bin/groovy

def call (String TARGET_BRANCH) {

SHORT_GIT_COMMIT_ID = bat (
script: "git rev-parse --short ${TARGET_BRANCH}",
returnStdout: true
).trim()

return SHORT_GIT_COMMIT_ID
}
