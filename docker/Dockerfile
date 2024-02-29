# 步骤1 前端打包
FROM node:14-alpine as WEB
WORKDIR /tmp/build

#  构建缓存
ADD ../web/package.json .
RUN npm install

# 前端打包，生成dist文件
ADD ../web .
RUN npm run build

# 步骤2 后台打包
FROM maven:3-openjdk-8 as java
WORKDIR /tmp/build

# 构建缓存
ADD ../pom.xml .
ADD ../src/main/java/cn/BootApplication.java src/main/java/cn/BootApplication.java
RUN mvn -DskipTests=true -q  package


#  构建
ADD ../src ./src

#  将WEB界面融合到一起
COPY --from=WEB /tmp/build/dist/ src/main/resources/static/

#  maven打包
RUN mvn -DskipTests=true -q package && mv target/*.jar /home/app.jar

# 步骤3 使用干净的java环境作为镜像
FROM openjdk:8-alpine

# 打包生成的文件放到 /home下
COPY --from=JAVA /home/app.jar /home/app.jar

EXPOSE 7001

# 启动命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Shanghai","-jar","/home/app.jar"]