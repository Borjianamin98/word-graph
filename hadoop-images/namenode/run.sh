#!/bin/bash

if [ ! -d "${NAMENODE_DATA_DIR}" ]; then
  echo "Namenode data directory not found: ${NAMENODE_DATA_DIR}"
  exit 2
fi

if [ -z "${CLUSTER_NAME}" ]; then
  echo "Cluster name not specified"
  exit 2
fi

echo "Remove lost+found from ${NAMENODE_DATA_DIR}"
rm -r "${NAMENODE_DATA_DIR}/lost+found"

if [ "$(ls -A "${NAMENODE_DATA_DIR}")" == "" ]; then
  echo "Formatting namenode data directory: ${NAMENODE_DATA_DIR}"
  "${HADOOP_HOME}/bin/hdfs" --config "${HADOOP_CONF_DIR}" namenode -format "${CLUSTER_NAME}"
fi

"${HADOOP_HOME}/bin/hdfs" --config "${HADOOP_CONF_DIR}" namenode
