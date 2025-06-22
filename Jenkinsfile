#!/usr/bin/env groovy
pipeline {
    agent any
    tools { jdk 'jdk-17.0.1' }

    environment {
        CURSEFORGE_API_TOKEN = credentials('curseforge-api-token')
        MODRINTH_API_TOKEN = credentials('modrinth-api-token')
        GITHUB_TOKEN = credentials('github-api-token')
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
                branch 'mc1.20.1/main'
            }
            steps {
                sh './gradlew publishMod'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
        }
    }
}
