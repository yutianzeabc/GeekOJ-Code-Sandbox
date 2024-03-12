# 使用Debian作为基础镜像
FROM debian:latest
MAINTAINER "Tianze Yu"
LABEL version="1.0"

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    software-properties-common \
    build-essential \
    wget

# 安装Java 17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

# 安装GCC，支持C++ 17
RUN apt-get install -y g++

# 设置Go环境变量
ENV PATH=$PATH:/usr/local/go/bin

# 验证安装
RUN java -version
RUN g++ --version
