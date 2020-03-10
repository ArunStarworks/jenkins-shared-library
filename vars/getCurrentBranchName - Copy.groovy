#!/usr/bin/groovy

def call (String gitUserName, String gitUserEmail, String sshAgentId)
{
sh "git config user.name ${gitUserName}"
sh "git config user.email ${gitUserEmail}"
  
if ("${env.GIT_BRANCH}" =~ "origin/.*")
{
   echo "Branch contains origin - New branch is ${TARGET_BRANCH}"
}
else if ("${env.GIT_BRANCH}" =~ "PR-*")
{
   echo "Origin does not exist in the branch name - Adding it in"
   TARGET_BRANCH = "origin/" + TARGET_BRANCH
   echo "New branch is ${TARGET_BRANCH}"
}

else
{
TARGET_BRANCH = "${env.GIT_BRANCH}"
}
return TARGET_BRANCH
}
