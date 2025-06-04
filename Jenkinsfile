#!/usr/bin/env groovy
pipeline {
    agent any
    tools { jdk 'jdk-21' }

    environment {
        CURSEFORGE_API_TOKEN = credentials('curseforge-api-token')
        MODRINTH_API_TOKEN = credentials('modrinth-api-token')
    }
    
    stages {
        stage('Setup') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
        stage('Publish') {
            when {
                branch 'mc1.21.1/main'
            }
            steps {
                sh './gradlew publishMod --no-configuration-cache'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
        }
    }
}