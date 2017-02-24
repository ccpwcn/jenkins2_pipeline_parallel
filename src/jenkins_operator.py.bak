'''
  Jenkins操作员 v1.0
'''
#!/usr/bin/python3


import jenkins


SERVER = jenkins.Jenkins(
    'http://192.168.104.186:8080/jenkins', username='remote', password='zjxl2012#')
USER = SERVER.get_whoami()
VERSION = SERVER.get_version()
print('Jenkins(%s) job started by %s' % (VERSION, USER['fullName']))
SERVER.build_job('PTKFZ/demo_job')

