pipeline {
    agent any

    environment {
        IMAGE_NAME = 'english-interlocutor-backend'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        DEPLOY_PATH = '/home/yc-user/app'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                withCredentials([
                    string(credentialsId: 'registry-id', variable: 'REGISTRY_ID')
                ]) {
                    sh "docker build -t cr.yandex/${REGISTRY_ID}/${IMAGE_NAME}:${IMAGE_TAG} ."
                    // Сохраняем полный путь к образу для следующих стадий
                    script {
                        env.IMAGE_FULL = "cr.yandex/${REGISTRY_ID}/${IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }
        }

        stage('Push to YCR') {
            steps {
                withCredentials([
                    string(credentialsId: 'registry-id', variable: 'REGISTRY_ID'),
                    file(credentialsId: 'yc-sa-key', variable: 'SA_KEY_FILE')
                ]) {
                    sh '''
                        mkdir -p /tmp/jenkins-yc
                        echo "$SA_KEY_BASE64" | base64 -d > /tmp/jenkins-yc/sa-key.json
                    '''

                    sh '''
                        cat /tmp/jenkins-yc/sa-key.json | docker login cr.yandex --username json_key --password-stdin
                    '''

                    sh "docker push ${env.IMAGE_FULL}"

                    sh 'rm -f /tmp/jenkins-yc/sa-key.json'
                }
            }
        }

        stage('Deploy to VM') {
            steps {
                withCredentials([
                    string(credentialsId: 'vm-host', variable: 'VM_HOST'),
                    sshUserPrivateKey(credentialsId: 'yc-ssh-key',
                                      keyFileVariable: 'SSH_KEY',
                                      usernameVariable: 'SSH_USER')
                ]) {
                    sshagent(credentials: ['yc-ssh-key']) {
                        // Копируем docker-compose.yml на ВМ
                        sh "scp -o StrictHostKeyChecking=no docker-compose.yml ${SSH_USER}@${VM_HOST}:${DEPLOY_PATH}/"

                        // Подключаемся по SSH, обновляем тег и перезапускаем контейнер
                        sh """
                            ssh -o StrictHostKeyChecking=no ${SSH_USER}@${VM_HOST} '
                                cd ${DEPLOY_PATH} && \
                                docker compose pull && \
                                docker compose up -d --force-recreate && \
                                docker image prune -f
                            '
                        """
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                withCredentials([
                    string(credentialsId: 'vm-host', variable: 'VM_HOST')
                ]) {
                    sh """
                        sleep 15
                        curl -sf http://${VM_HOST}:8080/actuator/health | grep -q '"status":"UP"' || exit 1
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Сборка #${BUILD_NUMBER} успешно задеплоена"
        }
        failure {
            echo "Сборка #${BUILD_NUMBER} провалилась"
        }
        always {
            sh "docker rmi ${env.IMAGE_FULL} || true"
        }
    }
}
