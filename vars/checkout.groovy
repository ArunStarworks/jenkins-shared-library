
def call(Map stageParams) {
 
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'gitUser', url: 'https://github.com/ArunStarworks/Helloworld.git']]])
  }


