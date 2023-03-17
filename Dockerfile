# 使用Java 8作为基础镜像
FROM openjdk:8-jdk-alpine

# 复制Jar文件到容器内
COPY Intelligent_scheduling_user_service-0.0.1-SNAPSHOT.jar /app/employee.jar
COPY Intelligent_scheduling_employee_service-0.0.1-SNAPSHOT.jar /app/user.jar
COPY Intelligent_scheduling_gateway-0.0.1-SNAPSHOT /app/gateway.jar

# 使用Java 17作为基础镜像
FROM openjdk:17-jdk-alpine
COPY Intelligent_scheduling_algorithm-1.0-SNAPSHOT.jar /app/algorithm.jar

# 设置工作目录和启动命令
WORKDIR /app
CMD java -jar employee.jar --server.port=8430 & java -jar user.jar --server.port=8431 & java -jar algorithm.jar --server.port=8432 & java -jar gateway.jar --server.port=80

# 暴露端口
EXPOSE 80 8430 8431 8432

#docker build -t intelligent-schedule .
#docker run -d -p 80:80 -p 8430:8430 -p 8431:8431 -p 8432:8432 intelligent-schedule
