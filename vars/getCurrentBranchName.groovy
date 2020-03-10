#!/usr/bin/groovy

def call (String gitUserName, String gitUserEmail, String sshAgentId)
{

sh "git config user.name ${gitUserName}"
sh "git config user.email ${gitUserEmail}"
  

TARGET_BRANCH = "${env.GIT_BRANCH}"

return TARGET_BRANCH
}
