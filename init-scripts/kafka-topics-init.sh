#!/bin/bash

set -eu

INITIAL_DATA=true
TOPIC_NAME="links"
BOOTSTRAP_SERVER="localhost:9092"

function describe_topic_config() {
  kafka-configs.sh \
    --describe --all \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${TOPIC_NAME}"
}

function alter_topic_config() {
  config_name="${1}"
  config_value="${2}"
  kafka-configs.sh  \
    --alter \
    --add-config "${config_name}=${config_value}" \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${TOPIC_NAME}"
}

topics=$(kafka-topics.sh --bootstrap-server localhost:9092 --list)
if ! echo "${topics}" | grep -q "${TOPIC_NAME}"; then
  kafka-topics.sh \
    --create \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --topic "${TOPIC_NAME}" \
    --partitions 1 \
    --replication-factor 1
fi

alter_topic_config "retention.ms" "7776000000" # 30 * 3 * 24 * 60 * 60 * 1000 (90 days)
describe_topic_config

cat << EOF > /tmp/initial-data.txt
test1
test2
EOF

if [[ "${INITIAL_DATA}" == true ]]; then
  kafka-console-producer.sh \
    --broker-list localhost:9092 \
    --topic "${TOPIC_NAME}" < /tmp/initial-data.txt
fi