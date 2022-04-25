#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

DOCKER_NETWORK="complex_infrastructure_default"

docker run -it --name graph-producer --network ${DOCKER_NETWORK} word-graph/graph-producer
docker rm -f graph-producer