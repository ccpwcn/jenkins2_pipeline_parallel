# 1. 简要介绍
SonarQube能够提供对代码的一整套检查扫描和分析功能，拥有一套服务器端程序，然后再通过客户端或者别的软件的插件的形式完成对各开发环境和软件的支持。

- 对编程语言的支持非常广泛，包括C、C++、Java、Objective C、Python、JavaScript、PHP、C#、Swift、Erlang、Groovy等众多语言
- 提供了对HTML、CSS、JSON、XML、CSV、SQL、JSP/JSF等类型的文档的支持
- 提供了以FindBugs、PMD、CheckStyle方式执行代码分析和测试检查的功能
- 登录认证方式支持LDAP、Bitbucket、Azure Active Directory（AAD）、Crowd等方式
- 提供了优美的3D视图方式下查看代码分析和测试结果报告

# 1. 环境要求
本文中，我们假定我们的服务器IP是192.168.100.194

## 1.1 软件环境要求
### 1.1.1 JDK
现在最新的SonarQube版本是6.1，要求JDK8及以上版本，官方要求如果是在Mac OS X系统上，使用Oracle JDK8取代Oracle JRE8，否则可能因为环境不完整而导致运行出现错误。
### 1.1.2 数据库
1. 绑定了Microsoft JDBC驱动的情况下，对于Microsoft SQL Server，支持2012（MSSQL Server 11.0）和2014（MSSQL Server 12.0），并且在Windows系统上的操作对象是区分大小写的
2. 绑定了MySQL JDBC驱动的情况下，对于MySQL，支持5.6和5.7，并且MySQL数据库的数据集必须配置为UTF8字符集，并且操作对象也是区分大小写的，并且仅支持InnoDB存储引擎，不支持MyISAM存储引擎
3. 对于Oracle数据库，支持11G和12C，但是12.x的驱动是不被支持的，XE版本是支持的，必须设置为使用UTF8字符集，并且支持BINARY排序，数据库模式仅支持thin，不支持OCI
4. PostgreSQL，支持8.x和9.x，必须配置为使用UTF8字符集

CentOS下默认的MySQL的数据库版本是5.1，是不被受支持的，如果直接部署和运行SonarQube，会无法使用，所以需要更新MySQL数据库版本，然而直接使用新版本覆盖安装Linux发行版自带的5.1会有N多的问题，坑太多，所以最好的解决方法是先备份现有的数据库，然后再删除5.1版的MySQL，再安装新的，再将旧的数据导进来。**重要的事情说三遍：先备份旧数据库，先备份旧数据库，先备份旧数据库，然后再删除，再安装新的**。具体的操作方法如下：
先从链接[http://dev.mysql.com/get/mysql57-community-release-el6-9.noarch.rpm](http://dev.mysql.com/get/mysql57-community-release-el6-9.noarch.rpm)上下载新版本的发布安装包信息。然后再执行命令
```
md5sum mysql57-community-release-el6-9.noarch.rpm
```
验证包的MD5，务必要和官方给出的保持一致，确认无误才行。
现在，执行命令
```
rpm -ivh mysql57-community-release-el6-9.noarch.rpm
```
> -i 是安装指令install的简写，-v是详尽模式verbose的简写，-h是打印要安装的包的Hash码（与-v组合使用）

开始安装MySQL的发布包，这个过程会瞬间完成。输出类似于下面这样的信息：
```
Preparing...                ########################################### [100%]
   1:mysql57-community-relea########################################### [100%]
```
现在我们就可以获取到官网上最新的稳定的Release版本5.7.16了。执行下面的命令即可：
```
yum install -y mysql-community-client mysql-community-server
```
这个过程会需要一些时间，并且不再需要我们的确认即可完成安装，并且会明确提示会使用5.7.16替换已经存在的5.1.73，输出类似于这样的信息说明：
```
==================================================================================================================================================================================================
 Package                                                   Arch                                 Version                                     Repository                                       Size
==================================================================================================================================================================================================
Installing:
 mysql-community-client                                    x86_64                               5.7.16-1.el6                                mysql57-community                                23 M
     replacing  mysql.x86_64 5.1.73-7.el6
 mysql-community-libs                                      x86_64                               5.7.16-1.el6                                mysql57-community                               2.1 M
     replacing  mysql-libs.x86_64 5.1.73-7.el6
 mysql-community-libs-compat                               x86_64                               5.7.16-1.el6                                mysql57-community                               1.6 M
     replacing  mysql-libs.x86_64 5.1.73-7.el6
 mysql-community-server                                    x86_64                               5.7.16-1.el6                                mysql57-community                               144 M
     replacing  mysql-server.x86_64 5.1.73-7.el6
Installing for dependencies:
 mysql-community-common                                    x86_64                               5.7.16-1.el6                                mysql57-community                               327 k
```
完成之后会给出这样的结果输出
```
Installed:
  mysql-community-client.x86_64 0:5.7.16-1.el6   mysql-community-libs.x86_64 0:5.7.16-1.el6   mysql-community-libs-compat.x86_64 0:5.7.16-1.el6   mysql-community-server.x86_64 0:5.7.16-1.el6  

Dependency Installed:
  mysql-community-common.x86_64 0:5.7.16-1.el6                                                                                                                                                    

Replaced:
  mysql.x86_64 0:5.1.73-7.el6                                 mysql-libs.x86_64 0:5.1.73-7.el6                                 mysql-server.x86_64 0:5.1.73-7.el6                                

Complete!
```
安装完成之后，还需要设置数据库的字符集，打开/etc/my.cnf，在[mysqld]这个节下面添加这两行：
```
character_set_server=utf8  
init_connect='SET NAMES utf8'
```
然后重新启动mysqld服务。如果启动mysqld服务时卡在```Installing validate password plugin:```这个步骤过不去，可以先中止这个过程，然后打开/etc/my.cnf，在[mysqld]这个节下面添加一行内容：
```
validate_password=off
```
这是临时禁止密码验证插件，可以在以后需要的时候再配置即可。

此时的数据库已经正常工作了，还是我们还不能使用，需要打开/var/log/mysqld.log，找到类似于下面的内容：
```
A temporary password is generated for root@localhost: -?i;XHTuh5eH
```
这里的```-?i;XHTuh5eH```就是临时密码，使用它可以在键入
```
mysql -u root -p
```
之后再输入密码，成功登录到MySQL，此时再使用命令
```
alter user 'root'@'localhost' identified by '123';
```
更新root用户的默认密码，5.6以后的版本安全机制增强了，临时密码只允许登录，不允许操作数据库，所以必须更改。
此时已经完全可以操作数据库了，验证方法是敲入下面的命令能够看到正确的结果：
```
select host,user from user;
```
但是现在我们只能在本地使用，无法使用远程客户端连接数据库，因此需要再次配置一下：
```
update user set host='%' where user='root';
flush privileges;
```
现在，就可以使用Navicat之类的工具进行远程访问了。

### 1.1.3 浏览器
- IE9，不支持
- IE10，不支持
- IE11，支持
- Microsoft Edge，支持
- Mozilla Firefox，支持
- Google Chrome，支持
- Opera，未测试，情况不明
- Safari，支持

## 1.2 硬件环境要求
1. 系统安装内存不小于2GB，系统可用内存不小于1GB
2. 磁盘空间需求的大小，依赖于SonarQube Server上分析的项目的数量和大小，官方称，SonarQube.com上公开的SonarQube中的实例中，2500万行的代码分析，累计了4年以上的时间，消耗磁盘空间达到10GB
3. SonarQube安装的硬盘设备必须具备有可读可写的权限，尤其重要的是数据目录（data folder）在ElasticSearch引擎创建索引的时候，以及SonarQube Server启动和运行期间，会有大量的I/O操作，因此性能消耗会比较大

# 2. 初步应用
## 2.1 下载安装
- 从官网下载zip包
- 在本地解压
- 进入bin目录，再进入相应的操作系统版本，运行即可，在64位的Linux为例，进入```sonarqube-6.1/bin/linux-x86-64```，再运行命令```nohup ./sonar.sh start &```即可
此时使用的是一个内置的小型数据库，仅支持体验产品功能，无法规模化使用，要想规模化使用，需要与数据库连接，我们以MySQL为例：

现在登录到数据库，输入下面的命令为SonarQube Server端创建数据库：
```
CREATE DATABASE sonar CHARACTER SET utf8 COLLATE utf8_general_ci;
```
创建SonarQube Server访问数据库的用户：
```
CREATE USER 'sonar' IDENTIFIED BY 'sonar';
```
配置SonarQube Server访问数据库用户的权限
```
GRANT ALL ON sonar.* TO 'sonar'@'%' IDENTIFIED BY 'sonar';
GRANT ALL ON sonar.* TO 'sonar'@'localhost' IDENTIFIED BY 'sonar';
flush privileges;
```
打开SonarQube目录下的conf/sonar.properties文件，配置它的数据库连接，启用和配置下面的选项：
```
sonar.jdbc.username=root
sonar.jdbc.password=123456
sonar.jdbc.url=jdbc:mysql://localhost:3306/sonar?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true&useConfigs=maxPerformance
```
事实上这个文件中还有很多选项可以设置，我们先保持默认值，以后再说。现在保存退出之后，转到SonarQube目录下的bin/linux-x86-64目录去，执行下面的命令启动SonarQube
```
nohup ./sonar.sh start
```
现在，就可以开始访问了，SonarQube默认的端口号是9000，请确认它没有被占用，如果占用了，就继续在sonar.properties中修改sonar.web.port这个选项的值并且启用它。现在我们就以下面的地址为例：
```
http://192.168.100.194:9000
```
访问成功时会显示下面的画面：
![SonarQube Dashboard](https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/SonarQube%20Dashboard.png)

> 配置成功之后，再次从客户端使用浏览器打开SonarQube，不会再在页面下沿提示使用临时数据库面不能规模化使用的提示。


# 3. 与Jenkins联合应用
## 3.1 安装Jenkins插件并配置选项
在Jenkins中安装和使用SonarQube的先决条件：
- 安装插件SonarQube Plugin，并且Jenkins插件SonarQube Plugin在配置位置Jenkins->Configuration->Configure System->SonarQube servers中的选项Server URL要配置正确，使用已经安装好的SonarQube Server的URL

## 3.2 第一种做法（Pipeline脚本方式）
这种做法的要点是通过调用SonarQube的Server插件和安装在Jenkins服务器上的SonarScanner来完成对代码的扫描和检查。所以需要在Jenkins所在服务器上安装SonarScanner，这个步骤其实很简单，将SonarScanner下载之后解压到任意位置就可以了。然后需要确认下面的配置正确有效：
- SonarScanner安装目录中配置文件conf/sonar-scanner.properties中的选项sonar.host.url

配置成功之后，在Pipeline脚本中添加下面的代码：
```
stage 'analysis'
def scannerHome = tool 'SonarQube Scanner';
withSonarQubeEnv('SonarQube server') {
    sh "${scannerHome}/bin/sonar-scanner"
}
```
代码中，第2行是引用Jenkins的SonarQube Plugin的配置名称，第3行是在插件中对SonarQube Server的配置名称，表示在此处指定使用这个服务器的配置环境执行检查，如果你配置了多个SonarQube Sever，它们是支持通过这种方式随时任意切换的，甚至支持根据特定条件走不同的服务器配置和检查流程，这就是Pipeline脚本带来的巨大优势了。

到此为止，我们以为立即可以见到结果了，其实不然，会报失败，原因是我们缺少一个名叫Project root configuration file的东西，其实这个文件就是一个Java的Properties文件，我们创建一个名叫sonar-project.properties的文件，放在项目的根目录，然后和pom.xml放在一起，当然，也可以放在其他位置。文件的内容大概是这个样子的：
```
# 指定一个项目Key，SonarQube会以这个去创建Scanner实例，所以它必须是唯一的
sonar.projectKey=ptkfz:testweb_mvn
# 下面指定的项目名称和版本号将会在SonarQube界面上显示出来
sonar.projectName=testweb_mvn
sonar.projectVersion=0.0.1-SNAPSHOT

# 相对于配置文件sonar-project.properties file的源码路径
# 自SonarQube 4.2之后，这个选项在sonar.modules已经设置了的情况下会被忽略
# 如果没有设置，SonarQube将会从包含了sonar-project.properties文件的目录中开始查找
# 源代码文件
sonar.sources=.

# 源码文件的字符编码，默认使用操作系统的编码方案
sonar.sourceEncoding=UTF-8
```
再次提交，然后我们将Pipeline脚本改一下，让SonarScanner能够找到我们配置的这个文件：
```
stage 'analysis'
def scannerHome = tool 'SonarQube Scanner';
withSonarQubeEnv('SonarQube server') {
    sh "${scannerHome}/bin/sonar-scanner -Dproject.settings=testweb_mvn/sonar-project.properties"
}
```
现在就可以成功了。结果如下图所示：
![https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/Jenkins%26Pipeline%26SonarScanner.png](https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/Jenkins%26Pipeline%26SonarScanner.png)

构建成功之后我们登录到SonarQube服务器上去，就能看到刚才构建的任务的分析情况了，如下图所示：
![https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/SonarQube%20Dashboard%20001.png](https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/SonarQube%20Dashboard%20001.png)

细节报表图：
![https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/SonarQube%20Dashboard%20002.png](https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/SonarQube%20Dashboard%20002.png)

对于复杂的项目配置，官方还提供了一份指导文档，入口：[http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner](
http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner)

> 使用Pipeline这种做法的好处显而易见，不需要做全局范围的配置改动，适用于不同项目的不同策略，具有很强的个性化配置功能。

## 3.3 第二种做法（传统Maven项目）
这种做法的要点是通过Maven对Sonar的支持来完成，它有一个要求，要修改Maven的setting.xml的配置，否则不能生效。
> Maven配置文件中的Sonar相关配置，这又涉及到全局配置和个性化配置的问题，Sonar官方给出了一个配置方法示例，传送门：[http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven](http://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven)

修改Maven配置之后，在Maven构建指令上加上一个Goals即可：
```
$SONAR_MAVEN_GOAL -Dsonar.host.url=$SONAR_HOST_URL
```
上面的指令中，$SONAR_MAVEN_GOAL的值一般是```sonar:sonar```，$SONAR_HOST_URL的值是SonarQube server的实际地址。**注意：通过对Jenkins中的Maven项目配置Post-build Action for Maven analysis这种方式实现调用Sonar对代码进行分析检查现在仍然是可用的，但是它已经被官方声明为废弃的做法，以后将不会被支持。**

> 这种做法的特点是一旦全局配置了，可以随时在任何项目上立即启用Sonar代码检查机制，但是它的影响面比较大，需要团队协同才能做好。

## 3.4 结果处理与分析

# 4. 与Eclipse联合应用
Sonar与Eclipse联合应用非常简单，只需要安装一个插件SonarLint for Eclipse即可，下载链接[http://www.sonarlint.org/eclipse/](http://www.sonarlint.org/eclipse/)，安装之后在Eclipse中创建一个项目，然后在 编写代码的时候，我们随时就可以在SonarLint Console上看到Sonar对我们的代码的分析情况了，如下图所示：
![https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/Eclipse%20SonarLint%20001.png](https://raw.githubusercontent.com/ccpwcn/GitRepository/master/resource/sonarqube/Eclipse%20SonarLint%20001.png)

还可以在工程上点击右键，选择SonarLint->Bind to SonarQube project，这个选项的目的是将当前工程直接与SonarQube Server相绑定，代码分析质量情况随时会上传到SonarQube Server上去，供团队使用，也是很方便的。

# 5. 附加信息
## 5.1 MySQL升级之故障与除错
将MySQL从5.1升级到5.7.16之后，再次输入下面的命令启动服务
```
service mysqld start
```
结果会启动失败，此时检查/var/log/mysqld.log，会发现首先是提示/var/lib/mysql/ib_buffer_pool这个文件找不到，那么我们创建一个，使用下面的命令
```
touch /var/lib/mysql/ib_buffer_pool
```
两次尝试，仍然没有能够启动成功，报的错误是
```
2016-11-01T07:22:07.120868Z 0 [Warning] Failed to open optimizer cost constant tables

2016-11-01T07:22:07.121704Z 0 [ERROR] Fatal error: mysql.user table is damaged. Please run mysql_upgrade.
2016-11-01T07:22:07.121845Z 0 [ERROR] Aborting
```
看提示是MySQL用户表出现了错误，被损坏了，需要升级，那么我们使用下面的命令
```
mysqld -initialize --user=mysql
```
命令没有报任何错误，再次尝试启动mysqld服务的时候，又失败了，再查日志，仍然是mysql.user table is damaged这个错误，找半天原因，发现在/etc/init.d/mysqld启动脚本109行中有这样的命令项
```
action $"Initializing MySQL database: " /usr/sbin/mysqld --initialize --datadir="$datadir" --user=mysql
```
事实上所谓的MySQL用户表损坏了要初始化修复，是个伪命题，原因很简单，看上面这个文件内容就知道，调用mysqld的start函数的时候，会自动调用这个命令的，手动操作也是徒劳。当然了，也并非一无所获，因为距离109行不远的91行有这样的命令选项
```
if [ ! -d "$datadir/mysql" ] ; then
```
这是在判断mysql不是一个有效的文件的时候，才会走这个逻辑，并且在这个逻辑之前，还有一个判断，在第93-96行，是这样的
```
if [ ! -e "$datadir" -a ! -h "$datadir" ]
then
mkdir -p "$datadir" || exit 1
fi
```
如果mysql的数据目录不存在，就会执行创建，看样子问题有可能出在这里，紧接着这里，123-124行上，还有这样的命令选项：
```
chown mysql:mysql "$datadir"
chmod 0751 "$datadir"
```
看样子不光要检查这个数据目录的有效性，还要设置权限的，那我们先检查mysql这个用户和组是否存在吧，使用下面的命令：
```
cat /etc/passwd | grep mysql
```
确认没问题，再检查组：
```
cat /etc/group | grep mysql
```
也是存在的，那么现在我们去解决数据目录的问题。打开/etc/my.cnf这个文件，可以看到确实是这样配置的
```
datadir=/var/lib/mysql
```
我们现在把它改成这个样子的：
```
datadir=/var/lib/mysql5.7
socket=/var/lib/mysql5.7/mysql.sock
```
保存退出，再启动mysqld服务

## 5.2 安装新版本的MySQL失败时回滚操作
备份已经安装的MySQL的数据
```
tar -cjf old_mysql_files.tar.bz2 /var/lib/mysql
```
删除所有已经安装的MySQL
```
yum remove mysql*
find / -name mysql
rm -rf mysql****
```
重新安装MySQL服务器端，然后恢复数据，并且重启MySQL服务
```
yum install mysql-server
tar -jxvf old_mysql_files.tar.bz2
service mysqld start
```

## 5.3 删除MySQL
删除以yum方式安装的包
```
yum remove mysql
```

删除配置文件
```
rm /etc/my.cnf
rm /etc/my.cnf.rpmsave
```

删除数据文件
```
rm -r -f /var/lib/mysql
```

删除运行文件
```
rm -r -f /usr/lib64/mysql
```

删除以rpm方式安装的包，要删除到直到使用```rpm -qa | grep mysql```查不到MySQL的包信息为止
```
rpm -qa | grep mysql
rpm -e --nodeps mysql57-community-release-el6-9.noarch
rpm -e --nodeps mysql-community-libs-5.7.16-1.el6.x86_64
rpm -e --nodeps mysql-community-common-5.7.16-1.el6.x86_64
```

查看其他存在的MySQL项：
```
find / -name mysql
```
