#!/bin/bash

if [ ! -d "${DATANODE_DATA_DIR}" ]; then
  echo "Datanode data directory not found: ${DATANODE_DATA_DIR}"
  exit 2
fi

"${HADOOP_HOME}/bin/hdfs" --config "${HADOOP_CONF_DIR}" datanode
