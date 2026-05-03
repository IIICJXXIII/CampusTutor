# 阶段 1: 构建
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# 跳过测试以加快构建速度
RUN mvn clean package -DskipTests

# 阶段 2: 运行
FROM eclipse-temurin:17-jre-alpine
# 安装 libstdc++，供 ONNX Runtime native 库使用
RUN apk add --no-cache libstdc++
WORKDIR /app
# 复制构建好的 jar 包，文件名根据 pom.xml 生成，这里使用通配符适配
COPY --from=builder /app/target/campus-backend-*.jar app.jar
# 创建上传目录，对应 application.properties 中的 file.upload.path
RUN mkdir -p uploads

# 暴露端口，对应 server.port
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
