#!groovy
// Author: lidawei
// Since: Jenkins 2.7.1, Groovy 2.4.7
////////////////////////////////////////////////////////////////

@NonCPS
def getSvnCommitInfo() {
    echo "Commit info list:"
    for (int i = 0; i < currentBuild.changeSets.size(); i++) {
        def changeSets = currentBuild.changeSets[i]
        def logEntry = changeSets.getLogs()
        for (int j = 0; j < logEntry.size(); j++) {
            echo "Change info Revision:${logEntry[j].getRevision()}"
            echo "Change info Message:${logEntry[j].getMsg()}"
            echo "Change info Author:${logEntry[j].getAuthor()}"
        }
    }
}