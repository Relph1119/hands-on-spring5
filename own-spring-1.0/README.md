# 项目调试步骤
1. 下载apache-tomcat-8.5.81
2. 在idea中配置Configurations，添加Tomcat Server
3. 在Deployment中添加`Artifact`，选择own-spring-1.0:war exploded
4. 在Server中，`Application server`选择本地的tomcat安装目录
5. 在Server中，`On Update action`和`On frame deactivation`均选择`Update resources`
6. 在Server中，配置`Http port`为8080
7. 使用Debug模式运行