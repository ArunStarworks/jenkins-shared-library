#!/usr/bin/groovy
 
/**
* Increments the tag version in Git as well
*
* @param incrementType - The class value of the type of increment i.e. major, minor, patch
* @param gitUserName - The user to log back into Git to increment the tag in Bitbucket
* @param gitUserEmail - The email of the user to log back into Git to increment the tag in Bitbucket
* @param sshAgentId - The SSH key credential associated with the user to log back into Git to increment the tag in Bitbucket
*/
 
import com.company.utilities.IncrementType
 
def call (IncrementType incrementType, String gitUserName, String gitUserEmail, String sshAgentId){
 
    
    TARGET_BRANCH = getCurrentBranchName(gitUserName, gitUserEmail, sshAgentId)
    echo "The target branch is ${TARGET_BRANCH}"
    
    CURRENT_TAG = getCurrentGitTag(TARGET_BRANCH)
    echo "Current Tag on Repository - ${CURRENT_TAG}"
 
    // Obtain the Git commit ID of the current branch
    SHORT_GIT_COMMIT_ID = getGitCommitId(TARGET_BRANCH)
    echo "Current Short Git Commit ID - ${SHORT_GIT_COMMIT_ID}"
 
    if( incrementType == IncrementType.MAJOR ){
        NEW_TAG = bat (
            script: """
                    CURRENT_TAG=${CURRENT_TAG};
                    a=( \${CURRENT_TAG//./ } );
                    ((a[0]++)); export NEW_TAG="\${a[0]}.0.0";
                    echo \${NEW_TAG}""",
                    returnStdout: true
        ).trim()
    }
    else if ( incrementType == IncrementType.MINOR ){
        NEW_TAG =bat (
            script: """
                    CURRENT_TAG=${CURRENT_TAG};
                    a=( \${CURRENT_TAG//./ } );
                    ((a[1]++)); export NEW_TAG="\${a[0]}.\${a[1]}.0";
                    echo \${NEW_TAG}""",
            returnStdout: true
        ).trim()   
    }
    else if ( incrementType == IncrementType.PATCH ){
        NEW_TAG = bat (
            script: """
                    CURRENT_TAG=${CURRENT_TAG};
                    a=( \${CURRENT_TAG//./ } );
                    ((a[2]++)); export NEW_TAG="\${a[0]}.\${a[1]}.\${a[2]}";
                    echo \${NEW_TAG}""",
            returnStdout: true
        ).trim()
    }
    else if ( incrementType == IncrementType.BUILD_NUMBER){
        if("${env.GIT_BRANCH}" =~ "PR-*"){
            NEW_TAG = bat (
                script: """
                CURRENT_TAG=${CURRENT_TAG};
                a=( \${CURRENT_TAG//./ } );
                export JIRA_TICKET_NUMBER=\$( git log \$1 | grep -Eo '([A-Z]{3,}-)([0-9]+)' | uniq | head -n 1 )
                export METATAG=${env.BUILD_NUMBER}-\${JIRA_TICKET_NUMBER}-${env.GIT_BRANCH}-${SHORT_GIT_COMMIT_ID}; export NEW_TAG="\${a[0]}.\${a[1]}.\${a[2]}.\$METATAG"
                echo \${NEW_TAG}""",
                returnStdout: true
            ).trim()
        }
        else {
            NEW_TAG = bat (
                script: """
                CURRENT_TAG=${CURRENT_TAG};
                a=( \${CURRENT_TAG//./ } );
                export JIRA_TICKET_NUMBER=\$( git log \$1 | grep -Eo '([A-Z]{3,}-)([0-9]+)' | uniq | head -n 1 )
                export METATAG=${env.BUILD_NUMBER}-\${JIRA_TICKET_NUMBER}-${SHORT_GIT_COMMIT_ID}; export NEW_TAG="\${a[0]}.\${a[1]}.\${a[2]}.\$METATAG"
                echo \${NEW_TAG}""",
                returnStdout: true
            ).trim()
        }
    }
    
    sshagent (credentials: ["${sshAgentId}"]) {
        bat """
          git tag ${NEW_TAG}
          GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" git push --tags
        """
    }
    echo "Setting Build info: ${TARGET_BRANCH} with Tag ${NEW_TAG}"
    setBuildInfo("${TARGET_BRANCH}", "${NEW_TAG}")
    return NEW_TAG
}
