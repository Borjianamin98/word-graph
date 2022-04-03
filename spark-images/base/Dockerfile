FROM java-base:latest

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    python3 \
    procps \
    libc6 \
    libnss3 \
    libpam-modules \
    && rm -rf /var/lib/apt/lists/*

# SPARK_HOME used by spark scritps
ENV SPARK_VERSION=3.2.1 \
    HADOOP_VERSION=2.7 \
    SPARK_HOME=/opt/spark

RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && tar -xvzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} ${SPARK_HOME} \
    && rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

WORKDIR "/opt/spark"
