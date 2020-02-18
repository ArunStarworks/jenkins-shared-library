
def call(Map templateParams) 	

{
 
try{
checkout([$class: '', branches: [[name: templateParams.gitbranch]], 
doGenerateSubmoduleConfigurations: false, 
extensions: [], 
submoduleCfg: [], 
userRemoteConfigs: [[credentialsId: 'gitUser', 
url: templateParams.giturl ]]])
}
catch (err)
{
echo 'Test checkout not working '+ err

}

finally 
{
echo 'executing finally step'

}







}


