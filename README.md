# 项目介绍
容器管理，支持CICD。

# 功能
一个注册中心有多个镜像仓库
一个仓库存放多个同一地址的镜像， 即 同一url + 不同tag的镜像，如 mysql:5.7 mysql:5.6


# 快速开始
```
docker pull mooncn/docker-admin
docker run -d -p 7001:7001  -e dpip=127.0.0.1 -dpport=3306 -e dbpwd=123456  -v /var/run/docker.sock:/var/run/docker.sock mooncn/docker-admin
```
浏览器访问 http://127.0.0.1:7001 


# docker-compose
## 一键部署， 含数据库
```
version: '3'

services:
  admin:
    image: mooncn/docker-admin:latest
    restart: always
    ports:
      - "7001:7001" # 主机端口:容器端口
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
      - "./application-prod.yml:/home/application-prod.yml"
    environment:
      dbip: 172.17.0.1 # 容器网关
      dbport: 3305
      dbpwd: 2zbdWEs6vRHe8wc
      spring.profiles.active: prod
  mysql:
    image: mysql:5.7.35
    privileged: true
    environment:
      MYSQL_ROOT_PASSWORD: 2zbdWEs6vRHe8wc
      MYSQL_DATABASE: docker_admin
      TZ: Asia/Shanghai
    command:
      --lower_case_table_names=0
      --max_connections=1000
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_general_ci
      --wait_timeout=31536000
      --interactive_timeout=31536000
      --default-authentication-plugin=mysql_native_password
      --max_allowed_packet=100M
      --transaction-isolation=READ-COMMITTED
    ports:
      - "3305:3306"
    volumes:
      - ./mysql_data:/var/lib/mysql
```

## 自定义数据库
```
version: '3'

services:
  admin:
    image: mooncn/docker-admin:latest
    restart: always
    ports:
      - "7001:7001" # 主机端口:容器端口
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
    environment:
      dbip: 127.0.0.1
      dbport: 3306
      dbpwd: 123456
```




