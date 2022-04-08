#!/bin/bash

export SPARK_MASTER_HOST=${SPARK_MASTER_HOST:-$(hostname)}
export SPARK_MASTER_LOG="/var/log/spark-master"

source "${SPARK_HOME}/sbin/spark-config.sh"
source "${SPARK_HOME}/bin/load-spark-env.sh"

mkdir -p ${SPARK_MASTER_LOG}
ln -sf /dev/stdout "${SPARK_MASTER_LOG}/spark-master.out"

"${SPARK_HOME}/bin/spark-class" org.apache.spark.deploy.master.Master \
    --ip "${SPARK_MASTER_HOST}" \
    --port 7077 \
    --webui-port 8080 >> "${SPARK_MASTER_LOG}/spark-master.out"
