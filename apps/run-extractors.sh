#!/bin/bash

set -eu

SCRIPT_DIR=$(realpath "$(dirname "${BASH_SOURCE[0]}")")
cd "${SCRIPT_DIR}" || exit 1

if [[ "${#}" -lt 1 || "${#}" -gt 2 ]]; then
    echo "Illegal number of parameters: <script> <command> [--build]"
    echo "  <command>: start | stop"
    echo "  --build: build images from scratch"
    exit 1
fi

command="${1}"
if [[ "${#}" -eq 2 && "${2}" == "--build" ]]; then
  POM_FILE_PATH="${SCRIPT_DIR}/../pom.xml"
  mvn -f "${POM_FILE_PATH}" clean package -pl crawler -am -DskipTests
  mvn -f "${POM_FILE_PATH}" clean package -pl anchor-extractor -am -DskipTests
  mvn -f "${POM_FILE_PATH}" clean package -pl keyword-extractor -am -DskipTests
fi

${SCRIPT_DIR}/../scripts/run-docker-compose.sh \
  "complex_apps_extractors" \
  "${SCRIPT_DIR}/docker-compose-extractors.yaml" \
  "${command}"
