#!/bin/bash

set -eu

INITIAL_DATA=true
LINKS_TOPIC_NAME=links
PAGES_TOPIC_NAME=pages

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

./run-in-container.sh kafka kafka-topic-init.sh "${LINKS_TOPIC_NAME}"
./run-in-container.sh kafka kafka-topic-init.sh "${PAGES_TOPIC_NAME}"

if [[ "${INITIAL_DATA}" == true ]]; then
  ./run-in-container.sh kafka put-initial-links.sh "${LINKS_TOPIC_NAME}"
fi