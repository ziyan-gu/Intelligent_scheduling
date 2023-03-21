# 使用Java 8作为基础镜像
FROM openjdk:8-jdk-alpine

# 复制Jar文件到容器内
COPY Intelligent_scheduling_user_service-0.0.1-SNAPSHOT.jar /app/user.jar
COPY Intelligent_scheduling_employee_service-0.0.1-SNAPSHOT.jar /app/employee.jar
COPY Intelligent_scheduling_gateway-0.0.1-SNAPSHOT.jar /app/gateway.jar
COPY Intelligent_scheduling_algorithm-1.0-SNAPSHOT.jar /app/Intelligent_scheduling_algorithm.jar

# 在容器中设置工作目录
WORKDIR /app

# 暴露4个服务所使用的端口
EXPOSE 8432
EXPOSE 8431
EXPOSE 80
EXPOSE 8430

# 运行每个服务
#CMD java -jar /app/user.jar --server.port=8430 &
#CMD java -jar /app/employee.jar --server.port=8431 &
#CMD java -jar /app/gateway.jar --server.port=80 &
#CMD java -jar /app/Intelligent_scheduling_algorithm.jar --server.port=8432 &

CMD java -jar /app/user.jar --server.port=8430 & \
    java -jar /app/employee.jar --server.port=8431 & \
    java -jar /app/gateway.jar --server.port=80 & \
    java -jar /app/Intelligent_scheduling_algorithm.jar --server.port=8432


#docker build -t intelligent-schedule .
#docker run -d -p 80:80 -p 8430:8430 -p 8431:8431 -p 8432:8432 intelligent-schedule
