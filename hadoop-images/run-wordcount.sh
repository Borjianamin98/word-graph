#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
DOCKER_NETWORK="complex_infrastructure_default"
ENV_FILE="${SCRIPT_DIR}/../hadoop.env"

docker run -it --name hadoop-wordcount --network ${DOCKER_NETWORK} --env-file "${ENV_FILE}" hadoop-wordcount
docker rm -f hadoop-wordcount