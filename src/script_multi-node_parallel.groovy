#!groovy
// Author: lidawei
// version: 4.0.9
// Since: Jenkins 2.7.1, Groovy 2.4.7
// Describe: 多配置多节点并发构建
////////////////////////////////////////////////////////////////
import java.util.concurrent.ConcurrentHashMap

// 核心节点，需要关闭Sandbox的限制
node {
    stage('initialization') {
        jdkHome = tool 'JDK_1.8'
        mvnHome = tool 'M3'
        targetReferenceUrls = new ConcurrentHashMap<>() // 构建成功并且上传到目标服务器上的所有Artifacts的URLS
    }

    // 使用多节点构建，各个节点之间的环境是相互独立的，不会互相影响
    stage('building') {
        def profiles = ['testing', 'production', 'spare', 'demo']
        def stepsForParallel = [:]
        for (int i = 0; i < profiles.size(); i++) {
            def profile = "${profiles[i]}"
            stepsForParallel[profile] = getAntBuildProcess(profile)
        }
        parallel(stepsForParallel)
    }

    stage('notification') {
        echo "Building job finished, total artifact(s) count:${targetReferenceUrls.size()}"
        for (String url : targetReferenceUrls.keySet()) {
            println('Url:' + url)
            def fingerprints = targetReferenceUrls.get(url)
            for (String fingerprint : fingerprints.keySet()) {
                println('\tItem:' + fingerprint + '--->' + fingerprints.get(fingerprint))
            }
        }
    }
}

def getAntBuildProcess(def profile) {
    return {
        node {
            // 假定本节点构建出了3个包
            for (int i = 0; i < 3; i++) {
                echo "Building profile ${profile}..."
                // TODO...构建过程。。。
                sleep(1)

                // 构建完成，收集构建结果包信息
                def url = "http://www.example.com/a/b/c/service_${profile}_${i}.war"
                def fingerprint = [:]
                fingerprint.put('md5', "md5-example-123-${i * i}")
                fingerprint.put('sha1', "sha1-example-123-${i * i}")
                targetReferenceUrls.put(url, fingerprint)
            }
        }
    }
}
