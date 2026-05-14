@Library('shared@v2') _

pipeline {
  agent { docker { image 'node:20-alpine'; args '-u root' } }

  options {
    timeout(time: 30, unit: 'MINUTES')
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '10'))
  }

  environment {
    CI = 'true'
    NODE_OPTIONS = '--max-old-space-size=4096'
    IMAGE = "ghcr.io/kubes1598/api:${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'npm ci --no-audit --prefer-offline'
      }
    }

    stage('Quality (parallel)') {
      parallel {
        stage('Unit tests')   { steps { sh 'npm run test:unit -- --coverage' } }
        stage('Lint')         { steps { sh 'npm run lint' } }
        stage('Type-check')   { steps { sh 'npm run typecheck' } }
        stage('Trivy')        {
          steps {
            sh 'docker build -t $IMAGE .'
            runTrivy(image: env.IMAGE, severity: 'CRITICAL,HIGH', failOn: 'CRITICAL')
          }
        }
      }
    }

    stage('Integration (parallel)') {
      parallel {
        stage('Integration tests') {
          steps { sh 'npm run test:integration' }
        }
        stage('Smoke E2E') {
          agent { docker { image 'mcr.microsoft.com/playwright:v1.50.0-jammy' } }
          steps { sh 'npx playwright install --with-deps && npm run test:e2e:smoke' }
        }
      }
    }

    stage('Publish') {
      when { branch 'main' }
      steps {
        withCredentials([usernamePassword(credentialsId: 'ghcr', usernameVariable: 'GHCR_USER', passwordVariable: 'GHCR_PW')]) {
          sh 'echo $GHCR_PW | docker login ghcr.io -u $GHCR_USER --password-stdin'
          sh 'docker push $IMAGE'
        }
      }
    }
  }

  post {
    always {
      publishCoverage()
      uploadArtifact(name: 'test-results', path: 'reports/')
    }
    success { notifySlack(channel: '#builds', status: 'PASSED') }
    failure { notifySlack(channel: '#builds', status: 'FAILED', mention: '@oncall') }
    unstable { notifySlack(channel: '#builds', status: 'UNSTABLE') }
  }
}
