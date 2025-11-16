pipeline {
    agent {
        kubernetes {
            yamlFile 'k8s-smart-mvn-pod.yaml'
        }
    }

    environment {
        KUBECONFIG = credentials('k8s.s3t.co')
    }

    stages {
        stage('prepare') {
            steps {
                script {
                    version = "1.0.${BUILD_NUMBER}"
                }
            }
        }
        
        stage("build") {
            steps {
                container("smart-mvn-agent") {
                    withCredentials([usernamePassword(credentialsId: "yycelik.github.com", usernameVariable: "USERNAME0", passwordVariable: "PASSWORD0"),
                                     usernamePassword(credentialsId: "nexus", usernameVariable: "USERNAME1", passwordVariable: "PASSWORD1"),
                                     usernamePassword(credentialsId: "database", usernameVariable: "USERNAME2", passwordVariable: "PASSWORD2")]) {
                        sh """
                            git clone https://$USERNAME0:$PASSWORD0@github.com/yycelik/smart-edilek-api.git

                            export MVN_REGISTRY_GROUP="https://nexus.s3t.co/repository/maven-public/"
                            
                            export MVN_REGISTRY_SNAPSHOOT="https://nexus.s3t.co/repository/maven-snapshots/"
                            
                            export MVN_REGISTRY_RELEASE="https://nexus.s3t.co/repository/maven-releases/"

                            export DOCKER_REGISTRY_GROUP="docker-g.nexus.s3t.co"

                            export DOCKER_REGISTRY_PROXY="docker-p.nexus.s3t.co"

                            export DOCKER_REGISTRY_SNAPSHOOT="docker-s.nexus.s3t.co"

                            export DOCKER_REGISTRY_RELEASE="docker-r.nexus.s3t.co"

                            export REGISTRY_USERNAME="$USERNAME1"

                            export REGISTRY_PASSWORD="$PASSWORD1"

                            export DATASOURCE_USERNAME="$USERNAME2"

                            export DATASOURCE_PASSWORD="$PASSWORD2"

                            cd smart-edilek-api

                            mvn versions:set -DnewVersion=${version} --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

                            mvn install -P create-image -DskipTests --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
                        """
                    }
                }
            }
        }
        
        stage("deploy") {
            steps {
                container("smart-skaffold-agent") {
                    withCredentials([usernamePassword(credentialsId: "nexus", usernameVariable: "USERNAME1", passwordVariable: "PASSWORD1"),
                                     usernamePassword(credentialsId: "database", usernameVariable: "USERNAME2", passwordVariable: "PASSWORD2")]) {
                        sh """
                            export DOCKER_REGISTRY_GROUP="docker-g.nexus.s3t.co"

                            export DOCKER_REGISTRY_PROXY="docker-p.nexus.s3t.co"

                            export DOCKER_REGISTRY_SNAPSHOOT="docker-s.nexus.s3t.co"

                            export DOCKER_REGISTRY_RELEASE="docker-r.nexus.s3t.co"

                            export REGISTRY_USERNAME="$USERNAME1"

                            export REGISTRY_PASSWORD="$PASSWORD1"

                            export DATASOURCE_USERNAME="$USERNAME2"

                            export DATASOURCE_PASSWORD="$PASSWORD2"

                            cd smart-edilek-api

                            echo "################### ${version}"
                            skaffold deploy --namespace=e-dilek --images=docker-r.nexus.s3t.co/smart/edilek:${version} --profile=prod
                        """
                    }
                }
            }
        }
    }
}
