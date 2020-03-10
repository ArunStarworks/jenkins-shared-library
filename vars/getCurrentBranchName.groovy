#!/usr/bin/groovy

def call (String gitUserName, String gitUserEmail, String sshAgentId)
{

bat "git config user.name ${gitUserName}"
bat "git config user.email ${gitUserEmail}"
  

TARGET_BRANCH = "${env.GIT_BRANCH}"

return TARGET_BRANCH
}
