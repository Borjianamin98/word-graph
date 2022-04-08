FROM java-base:latest

RUN curl -o /tmp/hadoop-keys https://dist.apache.org/repos/dist/release/hadoop/common/KEYS \
    && gpg --import /tmp/hadoop-keys \
    && rm /tmp/hadoop-keys

ENV HADOOP_VERSION=2.7.7
ENV HADOOP_URL https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz
RUN set -x \
    && mkdir /tmp/src \
    && curl -fSL "${HADOOP_URL}" -o /tmp/src/hadoop.tar.gz \
    && curl -fSL "${HADOOP_URL}.asc" -o /tmp/src/hadoop.tar.gz.asc \
    && gpg --verify /tmp/src/hadoop.tar.gz.asc \
    && tar -xvf /tmp/src/hadoop.tar.gz -C /opt/ \
    && ln -s /opt/hadoop-${HADOOP_VERSION} /opt/hadoop \
    && rm -rf /tmp/src

RUN mkdir -p /opt/hadoop/logs \
    && cp /opt/hadoop/etc/hadoop/mapred-site.xml.template /opt/hadoop/etc/hadoop/mapred-site.xml

ENV HADOOP_HOME=/opt/hadoop \
    HADOOP_CONF_DIR=/opt/hadoop/etc/hadoop \
    USER=root \
    PATH=/opt/hadoop/bin/:$PATH

ADD entrypoint.sh /entrypoint.sh

RUN chmod a+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
