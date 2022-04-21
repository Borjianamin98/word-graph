#!/bin/bash

set -eu

BOOTSTRAP_SERVER="localhost:9092"

function describe_topic_config() {
  kafka-configs.sh \
    --describe --all \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${topic_name}"
}

function alter_topic_config() {
  config_name="${1}"
  config_value="${2}"
  kafka-configs.sh  \
    --alter \
    --add-config "${config_name}=${config_value}" \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${topic_name}"
}

if [[ "${#}" -ne 1 ]]; then
    echo "Illegal number of parameters: <script> <topic_name>"
    exit 1
fi

topic_name="${1}"

echo "Create topic '${topic_name}' if necessary ..."
topics=$(kafka-topics.sh --bootstrap-server localhost:9092 --list)
if ! echo "${topics}" | grep -q "${topic_name}"; then
  kafka-topics.sh \
    --create \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${topic_name}" \
    --partitions 1 \
    --replication-factor 1
fi
echo "Topic '${topic_name}' created"

echo "Configure topic '${topic_name}' ..."
alter_topic_config "retention.ms" "7776000000" # 30 * 3 * 24 * 60 * 60 * 1000 (90 days)
describe_topic_config
