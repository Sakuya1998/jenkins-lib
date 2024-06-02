package org.devops

// 安装 SonarQube Scanner
def installSonarScanner() {
    return {
        sh '''
        # 检查是否已安装 sonar-scanner
        if ! command -v sonar-scanner &> /dev/null; then
            echo "Installing SonarQube Scanner..."
            curl -sSfL https://get.sonarqube.com/sonar-scanner-cli/jenkins -o sonar-scanner.zip
            unzip sonar-scanner.zip -d /opt/
            rm sonar-scanner.zip
            chmod +x /opt/sonar-scanner-*/bin/sonar-scanner
            /opt/sonar-scanner-*/bin/sonar-scanner --help
        else
            echo "SonarQube Scanner is already installed."
        fi
        '''
    }
}

// 发送请求到 SonarQube API
def SonarRequest(String apiUrl, String method, String sonarToken) {
    return {
        def sonarApi = "http://sonar:9000/api"
        def response = sh returnStdout: true, script: """
        curl --location \
             --request ${method} \
             --header "Authorization: Basic ${sonarToken}" \
             --url "${sonarApi}/${apiUrl}"
        """.trim()

        try {
            def responseJson = readJSON text: response
            return responseJson
        } catch (e) {
            return ["errors": "无法解析响应为 JSON"]
        }
    }
}

// 查找 SonarQube 项目
def ProjectSearch(String projectName, String sonarToken) {
    return {
        def apiUrl = "projects/search?q=${projectName}&ps=1"
        def response = SonarRequest(apiUrl, "GET", sonarToken).call()
        
        if (response.components.size() == 0) {
            println("Project not found!")
            return false
        } else {
            println("Project found: ${response.components[0].name}")
            return true
        }
    }
}

// 创建 SonarQube 项目
def CreateProject(String projectName, String sonarToken) {
    return {
        def apiUrl = "projects/create?name=${projectName}&project=${projectName}"
        def response = SonarRequest(apiUrl, "POST", sonarToken).call()
        
        if (response.project && response.project.key) {
            if (response.project.key == projectName.toLowerCase()) {
                println("Project created successfully: ${projectName}")
                return true
            } else {
                println("Project key mismatch. Expected: ${projectName}, found: ${response.project.key}")
                return false
            }
        } else {
            println("Failed to create project. Response: ${response}")
            return false
        }
    }
}

// 更新质量配置文件
def UpdateQualityProfiles(lang, projectName, profileName) {
    def apiUrl = "qualityprofiles/add_project?language=${lang}&project=${projectName}&qualityProfile=${profileName}"
    def response = SonarRequest(apiUrl, "POST")
    
    // 检查响应以确认操作成功
    if (response.errors) {
        echo "ERROR: UpdateQualityProfiles failed: ${response.errors}..."
        return false
    } else {
        echo "SUCCESS: UpdateQualityProfiles for ${lang} > ${projectName} > ${profileName}."
        return true
    }
}


// 设置 SonarQube 环境变量
def withSonarQubeEnv(String projectKey, String sonarToken) {
    return {
        env.SONAR_HOST_URL = 'http://your-sonarqube-server' // 替换成您的 SonarQube 服务器地址
        env.SONAR_PROJECT_KEY = projectKey
        env.SONAR_TOKEN = sonarToken
    }
}