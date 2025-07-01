#!/bin/sh

# 设置Java版本为17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 编译项目
./mvnw clean package -DskipTests

# 启动应用
java -jar target/ai-tool-backend-0.1.0-SNAPSHOT.jar 