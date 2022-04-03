#!/bin/bash

if [ -z "${SPARK_MASTERS}" ]; then
  echo "Spark masters not specified"
  exit 2
fi

export SPARK_WORKER_LOG="/var/log/spark-worker"

source "${SPARK_HOME}/sbin/spark-config.sh"
source "${SPARK_HOME}/bin/load-spark-env.sh"

mkdir -p ${SPARK_WORKER_LOG}
ln -sf /dev/stdout "${SPARK_WORKER_LOG}/spark-worker.out"

"${SPARK_HOME}/bin/spark-class" org.apache.spark.deploy.worker.Worker \
    --webui-port 8081 "${SPARK_MASTERS}" >> "${SPARK_WORKER_LOG}/spark-worker.out"
