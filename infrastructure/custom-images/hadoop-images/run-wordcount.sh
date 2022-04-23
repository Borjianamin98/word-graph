#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

DOCKER_NETWORK="complex_infrastructure_default"
ENV_FILE="../../hadoop.env"

docker run -it --name hadoop-wordcount --network ${DOCKER_NETWORK} --env-file "${ENV_FILE}" hadoop-wordcount
docker rm -f hadoop-wordcount