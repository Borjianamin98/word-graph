#!/bin/bash

set -eu

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

DOCKER_NETWORK="complex_infrastructure_default"

# Run this before it: mvn clean package -pl graph-producer -am -DskipTests
docker rm -f graph-producer || true
docker run -it --name graph-producer --network ${DOCKER_NETWORK} -p 127.0.0.1:4040:4040 word-graph/graph-producer
docker rm -f graph-producer