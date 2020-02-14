
def call(Map templateParams) 	{
 
    checkout([$class: 'GitSCM', branches: [[name: templateParams.gitbranch]], 
doGenerateSubmoduleConfigurations: false, 
extensions: [], 
submoduleCfg: [], 
userRemoteConfigs: [[credentialsId: 'gitUser', 
url: templateParams.giturl ]]])
  }


