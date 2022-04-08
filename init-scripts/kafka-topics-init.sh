#!/bin/bash

INITIAL_DATA=true
TOPIC_NAME="links"

topics=$(kafka-topics.sh --bootstrap-server localhost:9092 --list)
if ! echo "${topics}" | grep -q "${TOPIC_NAME}"; then
  kafka-topics.sh \
    --create \
    --bootstrap-server localhost:9092 \
    --topic "${TOPIC_NAME}" \
    --partitions 1 \
    --replication-factor 1
fi

cat << EOF > /tmp/initial-data.txt
test1
test2
EOF

if [[ "${INITIAL_DATA}" == true ]]; then
  kafka-console-producer.sh \
    --broker-list localhost:9092 \
    --topic "${TOPIC_NAME}" < /tmp/initial-data.txt
fi