pipeline {
    agent none

    triggers {
        pollSCM 'H/10 * * * *'
        upstream(upstreamProjects: "spring-data-commons/1.13.x", threshold: hudson.model.Result.SUCCESS)
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '14'))
    }
    
    stages {
        stage("Test") {
            when {
                anyOf {
                    branch '1.11.x'
                    not { triggeredBy 'UpstreamCause' }
                }
            }
            parallel {
                stage("test: baseline") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Phibernate-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-41") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-41 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-42") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-42 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-42-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-42-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-43") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-43 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-43-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-43-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-5") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-5 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-51") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-51 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-51-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-51-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-52") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-52 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-52-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            args '-v $HOME/.m2:/tmp/spring-data-maven-repository'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/spring-data-maven-repository" ./mvnw -Phibernate-52-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-53") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Phibernate-53 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-53-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Phibernate-53-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-54") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Phibernate-54 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: hibernate-54-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Phibernate-54-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: eclipselink-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Peclipselink-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: eclipselink-27") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Peclipselink-27 clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
                stage("test: eclipselink-27-next") {
                    agent {
                        docker {
                            image 'adoptopenjdk/openjdk8:latest'
                            label 'data'
                            args '-v $HOME:/tmp/jenkins-home'
                        }
                    }
                    options { timeout(time: 30, unit: 'MINUTES') }
                    steps {
                        sh 'rm -rf ?'
                        sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Peclipselink-27-next clean dependency:list test -Dsort -Dbundlor.enabled=false -B'
                    }
                }
            }
        }
        stage('Release to artifactory') {
            when {
                branch 'issue/*'
                not { triggeredBy 'UpstreamCause' }
            }
            agent {
                docker {
                    image 'adoptopenjdk/openjdk8:latest'
                    label 'data'
                    args '-v $HOME:/tmp/jenkins-home'
                }
            }
            options { timeout(time: 20, unit: 'MINUTES') }

            environment {
                ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
            }

            steps {
                sh 'rm -rf ?'
                sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Pci,artifactory ' +
                        '-Dartifactory.server=https://repo.spring.io ' +
                        "-Dartifactory.username=${ARTIFACTORY_USR} " +
                        "-Dartifactory.password=${ARTIFACTORY_PSW} " +
                        "-Dartifactory.staging-repository=libs-snapshot-local " +
                        "-Dartifactory.build-name=spring-data-jpa " +
                        "-Dartifactory.build-number=${BUILD_NUMBER} " +
                        '-Dmaven.test.skip=true clean deploy -B'
            }
        }
        stage('Release to artifactory with docs') {
            when {
                branch '1.11.x'
            }
            agent {
                docker {
                    image 'adoptopenjdk/openjdk8:latest'
                    label 'data'
                    args '-v $HOME:/tmp/jenkins-home'
                }
            }
            options { timeout(time: 20, unit: 'MINUTES') }

            environment {
                ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
            }

            steps {
                sh 'rm -rf ?'
                sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Pci,artifactory ' +
                        '-Dartifactory.server=https://repo.spring.io ' +
                        "-Dartifactory.username=${ARTIFACTORY_USR} " +
                        "-Dartifactory.password=${ARTIFACTORY_PSW} " +
                        "-Dartifactory.staging-repository=libs-snapshot-local " +
                        "-Dartifactory.build-name=spring-data-jpa " +
                        "-Dartifactory.build-number=${BUILD_NUMBER} " +
                        '-Dmaven.test.skip=true clean deploy -B'
            }
        }
    }

    post {
        changed {
            script {
                slackSend(
                        color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
                        channel: '#spring-data-dev',
                        message: "${currentBuild.fullDisplayName} - `${currentBuild.currentResult}`\n${env.BUILD_URL}")
                emailext(
                        subject: "[${currentBuild.fullDisplayName}] ${currentBuild.currentResult}",
                        mimeType: 'text/html',
                        recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
                        body: "<a href=\"${env.BUILD_URL}\">${currentBuild.fullDisplayName} is reported as ${currentBuild.currentResult}</a>")
            }
        }
    }
}
