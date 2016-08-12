#!groovy
// Author: lidawei
// Date: 2016-08-12
// Describe: 并行构建，并发任务，提升Pipeline效率，改进脚本代码的扩展性和灵活性
///////////////////////////////////////////////////////////////////////////////

import java.lang.Math

stage "initialization"
// 并行任务初始化
def labels = ["testing", "production", "spare", "demo"]

// 这个Map用于保存将要被并行执行的Steps
def stepsForParallel = [:]

// 使用常规的'for (String s: labels)'这种方法不能成功
// 只能使用下面这种循环方式
for (int i = 0; i < labels.size(); i++) {
    // Get the actual string here.
    def s = labels.get(i)

    // 定义Step
    def stepName = "${s}"
    
    stepsForParallel[stepName] = transformIntoStep(s)
}

stage "building"
// parallel函数接受一个Map
parallel stepsForParallel

// Step执行单元
def transformIntoStep(label) {
    // 将节点代码转换成一个闭包才能成功
    return {
        node {
            switch ("${label}") {
                case "testing":
                    testingProc()
                    break
                case "production":
                    productionProc()
                    break
                case "spare":
                    spareProc()
                    break
                case "demo":
                    demoProc()
                    break
                default:
                    throw new Exception("[ERROR]无法识别的构建模式定义${label}，任务失败！！！")
                    break
            }
        }
    }
}

stage "publishing"
echo "Publishing the artifacts to remote server..."
sleep Math.random() * 15 + 1
echo "Publishing done"

//////////////////////////////////////////////////
//////////////////////////////////////////////////

// Testing模式关键函数
def testingProc() {
    echo "Testing building..."
    sleep Math.random() * 10 + 1
    echo "Testing building finished"
}

// Production模式关键函数
def productionProc() {
    echo "Production building..."
    sleep Math.random() * 10 + 1
    echo "Production building finished"
}

// Spare模式关键函数
def spareProc() {
    echo "Spare building..."
    sleep Math.random() * 10 + 1
    echo "Spare building finished"
}

// Demo模式关键函数
def demoProc() {
    echo "Demo building..."
    sleep Math.random() * 10 + 1
    echo "Demo building finished"
}

////////////////////////////////////////////////////////
// 业务和逻辑支持部分
////////////////////////////////////////////////////////
def version() {
    return "1.1.0"
}

class Example {
    def name
    def status
    
    def execute() {
        return "done"
    }
}