# 《Spring 5核心原理与30个类手写实战》学习笔记 {docsify-ignore-all}
主要记录我学习 [《Spring 5核心原理与30个类手写实战》](https://github.com/gupaoedu-tom/spring5-samples) 的笔记，包括阅读笔记和代码部分，其中包括Spring IoC、DI、MVC、AOP的各个组件，可以配合Tom老师的视频[《用30个类高仿真提炼纯手写Spring框架》](https://www.bilibili.com/video/BV1gA4y1Z7HY) 学习。

## 在线阅读地址
在线阅读地址：https://relph1119.github.io/hands-on-spring5

## 环境安装
Java版本：1.8

### 本地启动docsify
```shell
docsify serve ./docs
```

## 视频中的问题
1. 视频部分有些颠倒，在P36之后建议直接跳到P44~P51（Spring MVC），看完这部分之后，再回到P37~P43（Spring AOP），之后再看P52之后的部分。
2. 视频内容
    - P1~P18：完成Spring顶层设计、Spring IoC容器
    - P19~P24：完成Spring DI（依赖注入）
    - P25~P28：使用Test类进行调试ApplicationContext
    - P28~P29：完成PostProcessor 
    - P30~P36、P44~P51：完成Spring MVC
    - P37~P43、P52~P67：完成Spring AOP：打通Spring IoC和DI，完成Before、AfterReturning、AfterThrowing，完成调试