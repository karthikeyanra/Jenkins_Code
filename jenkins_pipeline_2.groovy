pipeline {
    
        agent any
    stages {

        stage("Download Code") {
            steps {
                sh 'echo $PWD'
                sh 'echo $WORKSPACE'
                sh 'cd $WORKSPACE && rm -rf .gitignore .gitattributes .git && rm -rf *'
                sh 'git clone "https://github.com/karthikeyanra/simple-java-maven-app.git" $WORKSPACE/'
            }
        }
        stage("Build") {
            agent {
               docker {
                    reuseNode true
                    image 'maven:3-alpine'
                    args '-v $WORKSPACE/.m2:/root/.m2'
                    args '-v $WORKSPACE/myapp:/app'
                    args '-w /app'
                    
                }
            }
            steps {

                sh 'mvn -B -DskipTests clean package'
                
            }
            
        }

        stage("Test") {
           agent {
               docker {
                    reuseNode true
                    image 'maven:3-alpine'
                    args '-v $WORKSPACE/.m2:/root/.m2'
                    args '-v $WORKSPACE/myapp:/app'
                    args '-w /app'
                    
                }
            }
            steps {
                sh 'mvn test'
                sh 'cp $WORKSPACE/target/*.jar /var/jenkins_home/dockerfolder/buildimage/'
                
            }
        }

        stage("Deploying") {
            
            environment {
                    BUILD_TAG = "${BUILD_NUMBER}"
                    
            }

            steps {
                sh '''echo BUILD_ID=${BUILD_TAG} > /var/jenkins_home/dockerfolder/buildimage/.env'''
                sh '''/var/jenkins_home/dockerfolder/buildimage/buildimage.sh'''
                
                
                
        }

    }
}
}