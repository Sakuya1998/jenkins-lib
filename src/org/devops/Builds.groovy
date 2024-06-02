package org.devops

def InitSteps() {
    if ("${JOB_NAME}".contains("service")){
        SetJdk("${env.jdkVersion}")
    }
}

def SetJdk(jdkVersion) {
    def jdks = ["JDK8": "/usr/local/jdk8/bin", 
                "JDK11": "/usr/local/jdk11/bin"]
    sh """
        export JAVA_HOMEs=${jdks[jdkVersion]}
        echo \${JAVA_HOME}
    """
}

//SetProjectType
//根据构建命令判断项目的类型和一些初始化的操作
def ProjectBuild(buildShell){
    println("Build")
    if (buildShell.contains("gradle")){
        env.project_build_type = "gradle"
        GradleBuild(buildShell)
    } 
    if (buildShell.contains("mvn")){
        env.project_build_type = "maven"
        MavenBuild(buildShell)
    } 
    if (buildShell.contains("npm")){
        env.project_build_type = "npm"
        NpmBuild(buildShell)
    } 
    if (buildShell.contains("yarn")){
        env.project_build_type = "yarn"
        YarnBuild(buildShell)
    }
    if (buildShell.contains("docker")){
        env.project_build_type = "docker"
        DockerBuild(buildShell)
    } 
}

/MavenBuild
def MavenBuild(buildShell){
    // Get settings.xml
    data = libraryResource 'settings.xml'
    //println(data)
    writeFile file: 'settings.xml', text: data
    sh "ls -l && ${buildShell} && ls -l target/*"
}

//GradleBuild
def GradleBuild(buildShell){
    sh "ls -l && ${buildShell} && ls -l "
}

//NpmBuild
def NpmBuild(buildShell){
    //依赖步骤写在这里
    sh """
        npm config set registry https://registry.npm.taobao.org 
        ls -l && ${buildShell} && ls -l 
    """
}

//YarnBuild
def YarnBuild(buildShell){
    sh """
        npm config set registry https://registry.npm.taobao.org
        npm install -g yarn
        yarn config set registry https://registry.npm.taobao.org
        ls -l && ${buildShell} && ls -l
    """
}

def DockerBuild(buildShell){
    sh """
        docker build -t ${env.image_name} .
        docker login -u ${env.docker_user} -p ${env.docker_password}
        docker push ${env.image_name}
        docker logout
        ls -l && ${buildShell} && ls -l
    """
}