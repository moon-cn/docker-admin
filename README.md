# 项目介绍
容器管理，支持CICD。

# 快速开始
```
docker pull mooncn/docker-admin
docker run -d -p 7001:7001  -e dpip=127.0.0.1 -dpport=3306 -e dbpwd=123456  -v /var/run/docker.sock:/var/run/docker.sock mooncn/docker-admin
```
浏览器访问 http://127.0.0.1:7001 


# docker-compose
参考本项目的docker目录


# 功能
一个注册中心有多个镜像仓库
一个仓库存放多个同一地址的镜像， 即 同一url + 不同tag的镜像，如 mysql:5.7 mysql:5.6



