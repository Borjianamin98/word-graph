FROM library/ubuntu:18.04

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    openjdk-8-jdk \
    net-tools \
    curl \
    wget \
    netcat \
    gnupg \
    libsnappy-dev \
    nano \
    vim \
    htop \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
