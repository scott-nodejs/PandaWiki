# 使用 JDK 17 作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置时区为中国时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 Maven 配置文件
COPY settings.xml /root/.m2/settings.xml

# 复制项目文件
COPY pom.xml .
COPY src ./src

# 安装 Maven 并构建项目
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get remove -y maven && \
    apt-get autoremove -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 暴露应用端口
EXPOSE 8080

# 设置启动命令
CMD ["java", "-jar", "/app/target/pandawiki-backend.jar"] 