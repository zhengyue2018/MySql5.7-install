# MySQL安装

## 部署前准备
### 安装版本
+ mysql-5.7.23-1.el7.x86_64
### 下载安装文件
```url
https://cdn.mysql.com//Downloads/MySQL-5.7/mysql-5.7.23-1.el7.x86_64.rpm-bundle.tar
```
### 将文件上传服务器并解压
1. 服务器创建文件夹放置下载文件
```sh
mkdir /opt/mysql
```
2. 通过FileZilla将下载的文件上传到服务器

3. 创建安装文件夹将安装文件解压进去
```sh
#创建安装文件夹mysql5.7
mkdir /opt/mysql5.7
#切换到安装包存放文件夹
cd /opt/mysql/
#解压安装包
tar xvf mysql-5.7.23-1.el7.x86_64.rpm-bundle.tar -C /opt/mysql5.7/
```
## 安装部署
### 查看安装文件list
```sh
cd /opt/mysql5.7/
ls
```

### 开始安装
- 安装方式可以采用rpm或者yum install，rpm方式缺少的关联包需要手动安装，这里采用rpm方式安装
- 几个包都存在相互依赖的关系，所用使用--nodeps参数忽略依赖关系
- 若出现如下报错，则直接卸载掉冲突的包：
```sh
yum remove mariadb-libs-1*
```
- 组件安装
```sh
rpm -ivh mysql-community-server-5.7.23-1.el7.x86_64.rpm --nodeps
rpm -ivh mysql-community-client-5.7.23-1.el7.x86_64.rpm --nodeps
rpm -ivh mysql-community-libs-compat-5.7.23-1.el7.x86_64.rpm --nodeps
rpm -ivh mysql-community-common-5.7.23-1.el7.x86_64.rpm --nodeps
```
- 安装完成后启动mysql
```sh
service mysqld restart
```
### 更改密码
- 找到日志路径
```sh
cat /etc/my.cnf
```
- 查看初始密码
```sh
cat /var/log/mysqld.log | grep password
```
比如我的随机密码是：z(My-;CXc6F&
- 安装完mysql后执行自带的安全设置
```sh
/usr/bin/mysql_secure_installation
```
```shell
Securing the MySQL server deployment.
 
Enter password for user root: 
 
The existing password for the user account root has expired. Please set a new password.
 
New password: 
 
Re-enter new password: 
The 'validate_password' plugin is installed on the server.
The subsequent steps will run with the existing configuration
of the plugin.
Using existing password for root.
 
Estimated strength of the password: 100 
Change the password for root ? ((Press y|Y for Yes, any other key for No) : y
 
New password: 
 
Re-enter new password: 
 
Estimated strength of the password: 100 
Do you wish to continue with the password provided?(Press y|Y for Yes, any other key for No) : y
By default, a MySQL installation has an anonymous user,
allowing anyone to log into MySQL without having to have
a user account created for them. This is intended only for
testing, and to make the installation go a bit smoother.
You should remove them before moving into a production
environment.
 
Remove anonymous users? (Press y|Y for Yes, any other key for No) : y  --移除匿名用户
Success.
 
 
Normally, root should only be allowed to connect from
'localhost'. This ensures that someone cannot guess at
the root password from the network.
 
Disallow root login remotely? (Press y|Y for Yes, any other key for No) : y  --不允许远程连接
Success.
 
By default, MySQL comes with a database named 'test' that
anyone can access. This is also intended only for testing,
and should be removed before moving into a production
environment.
 
 
Remove test database and access to it? (Press y|Y for Yes, any other key for No) : y  --移除测试数据库
 - Dropping test database...
Success.
 
 - Removing privileges on test database...
Success.
 
Reloading the privilege tables will ensure that all changes
made so far will take effect immediately.
 
Reload privilege tables now? (Press y|Y for Yes, any other key for No) : y  --重读授权表使前面修改生效
Success.
 
All done! 
```
- 进入Mysql
```sh
#输入如下指令，提示输入密码，这里我把密码修改为Root-123，所以我输入Root-123就可以了
mysql -u root -p
```
### 开放3306端口
```sql
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'Root-123' WITH GRANT OPTION;
FLUSH PRIVILEGES;
exit;
```
开启防火墙mysql 3306端口的外部访问
```sh
firewall-cmd --zone=public --add-port=3306/tcp --permanent
firewall-cmd --reload
```

<font size='3'> **至此安装完成，可以对MySql进行操作了**
