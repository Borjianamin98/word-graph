#!/bin/bash

set -eu

SCRIPT_DIR=$(realpath "$(dirname "${BASH_SOURCE[0]}")")
cd "${SCRIPT_DIR}" || exit 1

if [[ "${#}" -gt 1 ]]; then
    echo "Illegal number of parameters: <script> [--build]"
    echo "  --build: build images from scratch"
    exit 1
fi

if [[ "${#}" -eq 1 && "${1}" == "--build" ]]; then
  POM_FILE_PATH="${SCRIPT_DIR}/../pom.xml"
  mvn -f "${POM_FILE_PATH}" clean package -pl graph-producer -am -DskipTests
fi

DOCKER_NETWORK="complex_infrastructure_default"
docker rm -f graph-producer || true
docker run -it --name graph-producer --network ${DOCKER_NETWORK} -p 127.0.0.1:4040:4040 word-graph/graph-producer
docker rm -f graph-producer