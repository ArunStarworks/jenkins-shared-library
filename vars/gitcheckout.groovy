
def call(String gitbranch, String giturl) 	{
 
    checkout([$class: 'GitSCM', branches: [[name: gitbranch]], 
doGenerateSubmoduleConfigurations: false, 
extensions: [], 
submoduleCfg: [], 
userRemoteConfigs: [[credentialsId: 'gitUser', 
url: giturl ]]])
  }


