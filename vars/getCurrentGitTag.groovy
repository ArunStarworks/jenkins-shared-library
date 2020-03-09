#!/usr/bin/groovy


def call (String TARGET_BRANCH)
{

CURRENT_TAG = sh(

script: "git tag --merged ${TARGET_BRANCH} | sort -r --version-sort | head -n1,
returnStdout: true
).trim()

return CURRENT_TAG



}