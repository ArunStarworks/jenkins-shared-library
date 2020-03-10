#!/usr/bin/groovy


def call (String TARGET_BRANCH)
{

CURRENT_TAG = bat(

script: "git tag --merged ${TARGET_BRANCH}",
returnStdout: true
).trim()

return CURRENT_TAG



}