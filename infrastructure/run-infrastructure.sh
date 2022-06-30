#!/bin/bash

set -eu

SCRIPT_DIR=$(realpath "$(dirname "${BASH_SOURCE[0]}")")
cd "${SCRIPT_DIR}" || exit 1

if [[ "${#}" -ne 1 ]]; then
    echo "Illegal number of parameters: <script> <command>"
    echo "  <command>: start | stop"
    exit 1
fi

command="${1}"
${SCRIPT_DIR}/../scripts/run-docker-compose.sh \
  "complex_infrastructure" \
  "${SCRIPT_DIR}/docker-compose.yaml" \
  "${command}"
