pipeline {
    agent any
    
    environment {
        // 设置 Docker 镜像名称和标签
        BACKEND_IMAGE = 'pandawiki-backend'
        ADMIN_IMAGE = 'pandawiki-admin'
        APP_IMAGE = 'pandawiki-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        
        // Docker Hub 凭证 ID（需要在 Jenkins 凭证管理中配置）
        DOCKER_CREDENTIALS = 'docker-hub-credentials'
    }
    
    stages {
        stage('检出代码') {
            steps {
                checkout scm
            }
        }
        
        stage('构建 Docker 镜像') {
            parallel {
                stage('构建后端镜像') {
                    steps {
                        dir('backend_java') {
                            script {
                                docker.build("${BACKEND_IMAGE}:${DOCKER_TAG}")
                            }
                        }
                    }
                }
                
                stage('构建管理界面镜像') {
                    steps {
                        dir('web/admin') {
                            script {
                                docker.build("${ADMIN_IMAGE}:${DOCKER_TAG}")
                            }
                        }
                    }
                }
                
                stage('构建前端应用镜像') {
                    steps {
                        dir('web/app') {
                            script {
                                docker.build("${APP_IMAGE}:${DOCKER_TAG}")
                            }
                        }
                    }
                }
            }
        }
        
        stage('推送 Docker 镜像') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', DOCKER_CREDENTIALS) {
                        // 推送后端镜像
                        docker.image("${BACKEND_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${BACKEND_IMAGE}:${DOCKER_TAG}").push('latest')
                        
                        // 推送管理界面镜像
                        docker.image("${ADMIN_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${ADMIN_IMAGE}:${DOCKER_TAG}").push('latest')
                        
                        // 推送前端应用镜像
                        docker.image("${APP_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${APP_IMAGE}:${DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
        
        stage('部署到生产环境') {
            steps {
                script {
                    // 使用 SSH 执行远程命令
                    sshagent(['production-server-credentials']) {
                        sh """
                            ssh user@your-server '
                                # 拉取最新镜像
                                docker pull ${BACKEND_IMAGE}:${DOCKER_TAG}
                                docker pull ${ADMIN_IMAGE}:${DOCKER_TAG}
                                docker pull ${APP_IMAGE}:${DOCKER_TAG}
                                
                                # 停止并删除旧容器
                                docker stop ${BACKEND_IMAGE} ${ADMIN_IMAGE} ${APP_IMAGE} || true
                                docker rm ${BACKEND_IMAGE} ${ADMIN_IMAGE} ${APP_IMAGE} || true
                                
                                # 创建网络（如果不存在）
                                docker network create pandawiki-network || true
                                
                                # 启动后端容器
                                docker run -d \\
                                    --name ${BACKEND_IMAGE} \\
                                    --network pandawiki-network \\
                                    -p 8080:8080 \\
                                    -v /path/to/logs:/app/logs \\
                                    -v /path/to/config:/app/config \\
                                    --restart unless-stopped \\
                                    ${BACKEND_IMAGE}:${DOCKER_TAG}
                                
                                # 启动管理界面容器
                                docker run -d \\
                                    --name ${ADMIN_IMAGE} \\
                                    --network pandawiki-network \\
                                    -p 8081:80 \\
                                    --restart unless-stopped \\
                                    ${ADMIN_IMAGE}:${DOCKER_TAG}
                                
                                # 启动前端应用容器
                                docker run -d \\
                                    --name ${APP_IMAGE} \\
                                    --network pandawiki-network \\
                                    -p 80:80 \\
                                    --restart unless-stopped \\
                                    ${APP_IMAGE}:${DOCKER_TAG}
                            '
                        """
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo '部署成功！'
        }
        failure {
            echo '部署失败！'
        }
        always {
            // 清理工作区
            cleanWs()
        }
    }
} 