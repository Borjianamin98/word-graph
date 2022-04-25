FROM hadoop-base:latest

HEALTHCHECK CMD curl -f http://localhost:50070/ || exit 1

ENV HDFS_CONF_dfs_namenode_name_dir=file:///dfs/name \
    NAMENODE_DATA_DIR=/dfs/name \
    PARQUET_CLI_VERSION=1.12.2 \
    PARQUET_CLI_URL="https://repo1.maven.org/maven2/org/apache/parquet/parquet-cli" \
    PARQUET_CLI_PATH="/tmp/parquet-cli.jar" \
    PARQUET_CLI_PARAMETERS="org.apache.parquet.cli.Main --dollar-zero parquet"

# Put parquet-cli jar in namenode image
RUN curl -o "${PARQUET_CLI_PATH}" "${PARQUET_CLI_URL}/${PARQUET_CLI_VERSION}/parquet-cli-${PARQUET_CLI_VERSION}-runtime.jar" \
    && echo '#!/bin/bash\nhadoop jar ${PARQUET_CLI_PATH} ${PARQUET_CLI_PARAMETERS} "$@"' > /usr/bin/parquet \
    && chmod +x /usr/bin/parquet

ADD run.sh /run.sh

RUN mkdir -p ${NAMENODE_DATA_DIR} \
    && chmod a+x /run.sh

VOLUME ${NAMENODE_DATA_DIR}

EXPOSE 50070

CMD ["/run.sh"]
