#!/bin/bash

set -eu

BOOTSTRAP_SERVER="localhost:9092"

if [[ "${#}" -ne 1 ]]; then
    echo "Illegal number of parameters: <script> <topic_name>"
    exit 1
fi

links_topic_name="${1}"

cat << EOF > /tmp/initial-data.txt
test1
test2
EOF

kafka-console-producer.sh \
  --broker-list "${BOOTSTRAP_SERVER}" \
  --topic "${links_topic_name}" < /tmp/initial-data.txt
