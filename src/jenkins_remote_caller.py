'''
    Name: Jenkins调度控制器
    Version: 1.0.1
    Author: lidawei
    Date: 2017-02-24
'''
#!/usr/bin/python3
# -*- coding: UTF-8 -*-

import re
import urllib
import urllib.request
import urllib.parse
import requests

# Jenkins服务器主路径
JENKINS_SERVER = 'http://192.168.104.186:8080/jenkins'


class JenkinsController(object):
    '''Jenkins控制器'''

    _job_name = ''
    _token = ''
    _cause = ''
    _job_page_url = ''

    def __init__(self, job_name, token, cause):
        '''构造函数，job_name是必须的，token也是必须的，cause是可选的，允许为空'''
        self._job_name = job_name
        self._token = token
        self._cause = cause
        job_name_elements = self._job_name.split('/')
        assert len(job_name_elements) > 1
        location = ''
        for element in job_name_elements:
            location += '/job/'
            location += element
        self._job_page_url = JENKINS_SERVER + location

    def __get_jenkins_crumb(self):
        '''获得Jenkins远程授权元数'''

        response = urllib.request.urlopen(self._job_page_url)
        content = response.read()
        response_body = content.decode('utf-8')
        assert len(response_body) > 0
        matcher = re.search(r'\("Jenkins-Crumb", ".+?"\)', response_body)
        jenkins_crumb = matcher.group()
        matcher = re.search(' ".+?"', jenkins_crumb)
        return matcher.group()[2:-1]
    # ... end function __get_jenkins_crumb()

    def build_job(self):
        '''构建Job'''

        crumb = self.__get_jenkins_crumb()
        if crumb is None or crumb == '':
            raise Exception('解析Jenkins-Crumb失败')
        # print('Current Jenkins-Crumb is:%s' % crumb)
        headers = {
            'Content-type': r'application/x-www-form-urlencoded',
            'Accept-Charset': 'utf-8',
            'Accept-Encoding': 'gzip,deflate',
            'Jenkins-Crumb': crumb
            }
        location = ''
        if self._cause is not None and self._cause is not '':
            location = self._job_page_url + '/build?token=' + self._token + '&cause=' + self._cause
        else:
            location = self._job_page_url + '/build?token=' + self._token
        req = requests.get(location, headers=headers)
        assert req.status_code == 201
        print('启动Jenkins Job %s 构建成功' % self._job_name)
    # ... end function build_job()


def main():
    '''主功能函数'''

    job_name = 'PTKFZ/demo_job'
    token = 'F7C2FC14-EC14-41ad-9E5D-5A5424F67EAE'
    cause = '中石化联名卡项目已经部署成功，远程回调'
    jenkins_controller = JenkinsController(job_name, token, urllib.parse.quote(cause))
    jenkins_controller.build_job()


if __name__ == '__main__':
    main()
