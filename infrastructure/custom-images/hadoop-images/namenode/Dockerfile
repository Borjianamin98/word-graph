FROM hadoop-base:latest

HEALTHCHECK CMD curl -f http://localhost:50070/ || exit 1

ENV HDFS_CONF_dfs_namenode_name_dir="file:///dfs/name" \
    NAMENODE_DATA_DIR="/dfs/name" \
    PARQUET_CLI_URL="https://repo1.maven.org/maven2/org/apache/parquet/parquet-cli/1.12.2/parquet-cli-1.12.2-runtime.jar" \
    PARQUET_CLI_PATH="/opt/parquet-cli.jar" \
    PARQUET_CLI_PARAMETERS="org.apache.parquet.cli.Main --dollar-zero parquet" \
    PARQUET_TOOLS_URL="https://repo1.maven.org/maven2/org/apache/parquet/parquet-tools/1.11.2/parquet-tools-1.11.2.jar" \
    PARQUET_TOOLS_PATH="/opt/parquet-tools.jar"

# Put parquet-cli jar in namenode image
RUN curl -o "${PARQUET_CLI_PATH}" "${PARQUET_CLI_URL}" \
    && echo '#!/bin/bash\nhadoop jar ${PARQUET_CLI_PATH} ${PARQUET_CLI_PARAMETERS} "$@"' > /usr/bin/parquet-cli \
    && chmod +x /usr/bin/parquet-cli

# Older parquet tools support extra functionalities
RUN curl -o "${PARQUET_TOOLS_PATH}" "${PARQUET_TOOLS_URL}" \
    && echo '#!/bin/bash\nhadoop jar ${PARQUET_TOOLS_PATH} "$@"' > /usr/bin/parquet-tools \
    && chmod +x /usr/bin/parquet-tools

ADD run.sh /run.sh

RUN mkdir -p ${NAMENODE_DATA_DIR} \
    && chmod a+x /run.sh

VOLUME ${NAMENODE_DATA_DIR}

EXPOSE 50070

CMD ["/run.sh"]
