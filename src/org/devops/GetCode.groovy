package org.devops

//下载代码
package org.devops

def GetCode(branchName, srcUrl, credentialsId = 'default-credentials-id') {
    echo "Starting checkout for branch: ${branchName}, source URL: ${srcUrl}"
    
    try {
        // 验证参数
        if (!branchName || !srcUrl) {
            error "Branch name or source URL is not provided."
        }
        
        // 检出代码
        checkout scmGit(
            branches: [[name: "${branchName}"]],
            extensions: [],
            userRemoteConfigs: [
                [
                    credentialsId: credentialsId,
                    url: "${srcUrl}"
                ]
            ]
        )
        
        // 列出当前目录内容
        sh "ls -l"
        
        echo "Checkout successful."
    } catch (Exception e) {
        echo "Error during checkout: ${e.getMessage()}"
        // 根据需要处理不同类型的异常
        throw e
    }
}