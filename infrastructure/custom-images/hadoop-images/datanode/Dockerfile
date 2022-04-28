FROM hadoop-base:latest

HEALTHCHECK CMD curl -f http://localhost:50075/ || exit 1

ENV HDFS_CONF_dfs_datanode_data_dir=file:///dfs/data \
    DATANODE_DATA_DIR=/dfs/data

ADD run.sh /run.sh

RUN mkdir -p ${DATANODE_DATA_DIR} \
    && chmod a+x /run.sh

VOLUME ${DATANODE_DATA_DIR}

EXPOSE 50075

CMD ["/run.sh"]
