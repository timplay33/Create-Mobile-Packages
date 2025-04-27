#!/usr/bin/env groovy
pipeline {
  agent any
  tools { jdk 'jdk-17.0.1' }
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
  }
  post {
    always {
      archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
    }
  }
}
