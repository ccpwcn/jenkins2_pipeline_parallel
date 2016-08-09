#!groovy
// Author: lidawei
// Since: Jenkins 2.7.1, Groovy 2.4.7
////////////////////////////////////////////////////////////////

import java.util.Random

def g_MAX = 5

node {
    stage 'checkout'
    svn url: 'svn://192.168.100.194/testweb_mvn'
    def random = new Random()
    
    stage 'build'
    parallel (
        phase1: {
            for (int i = 0; i < g_MAX; i++) {
                echo "phase1 Count:${i}"
                sleep random.nextInt(5) + 1
            }
            echo 'phase 1 done'
        },
        phase2: {
            for (int i = 0; i < g_MAX; i++) {
                echo "phase2 Count:${i}"
                sleep random.nextInt(5) + 1
            }
            echo 'phase 2 done'
        }
    )
    sh "echo run this after both phases complete"
}