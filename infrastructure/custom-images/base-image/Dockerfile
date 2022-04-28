FROM library/ubuntu:18.04

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    TZ="Asia/Tehran" \
    JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/

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
    tzdata \
    && rm -rf /var/lib/apt/lists/*
