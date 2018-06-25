pipeline {
    agent {
        kubernetes {
            label "qa-tests"
        }
    }
    stages {
        stage("Prerequisites") {
            steps {
                container('automation-slave') {
                    sh """
                        whoami
                    """
                    sh """
                        apt-get -y update && apt-get -y install maven
                    """
                }
            }
        }
        stage('Git Test') {
            steps {
                container('automation-slave') {
                    sh 'pwd'
                    sh 'ls -l'
                    sh 'git status'
                    echo 'Git test.'
                }
            }
        }
        stage('Build') {
            steps {
                container('automation-slave') {
                    sh 'mvn -Dtest=#downloadAttachTest test'
                }
            }
        }
    }
    post {
        always {
            script {
                allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                ])
            }
        }
    }
}
