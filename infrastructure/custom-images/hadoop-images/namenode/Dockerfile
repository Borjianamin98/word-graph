FROM hadoop-base:latest

HEALTHCHECK CMD curl -f http://localhost:50070/ || exit 1

ENV HDFS_CONF_dfs_namenode_name_dir=file:///dfs/name \
    NAMENODE_DATA_DIR=/dfs/name

ADD run.sh /run.sh

RUN mkdir -p ${NAMENODE_DATA_DIR} \
    && chmod a+x /run.sh

VOLUME ${NAMENODE_DATA_DIR}

EXPOSE 50070

CMD ["/run.sh"]
