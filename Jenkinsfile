pipeline {
    agent {
        kubernetes {
            label "altered-elk-jenkins-slave"
        }
    }
    stages {
        stage('Git Test') {
            steps {
                container('jnlp') {
                    sh 'pwd'
                    sh 'ls -l'
                    sh 'git status'
                    echo 'Git test.'
                }
            }
        }
        stage('Build') {
            steps {
                container('jnlp') {
                    sh '-Dtest=#downloadAttachTest test'
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
