import com.company.selenium.Browserversion

def call(Map templateParams, Browserversion bchrome) 	

{
 
echo bchrome.value()
try{
checkout([$class: 'GitSCM', branches: [[name: templateParams.gitbranch]], 
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


