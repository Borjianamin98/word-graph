#!/bin/bash

set -eu

BOOTSTRAP_SERVER="localhost:9092"

if [[ "${#}" -ne 1 ]]; then
    echo "Illegal number of parameters: <script> <topic_name>"
    exit 1
fi

links_topic_name="${1}"

cat << EOF > /tmp/initial-data.txt
http://google.com
http://wikipedia.org
http://instagram.com
http://yahoo.com
http://whatsapp.com
http://amazon.com
http://live.com
http://netflix.com
http://reddit.com
http://office.com
http://linkedin.com
http://zoom.us
http://discord.com
http://twitch.tv
http://bing.com
http://roblox.com
http://microsoftonline.com
http://duckduckgo.com
http://pinterest.com
http://msn.com
http://microsoft.com
http://ebay.com
http://accuweather.com
http://fandom.com
http://weather.com
http://paypal.com
http://walmart.com
EOF

kafka-console-producer.sh \
  --broker-list "${BOOTSTRAP_SERVER}" \
  --topic "${links_topic_name}" < /tmp/initial-data.txt
