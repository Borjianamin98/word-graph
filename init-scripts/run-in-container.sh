#!/bin/bash

set -eu

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

if [[ "${#}" -ne 2 ]]; then
    echo "Illegal number of parameters: <script> <script_file_name> <container_name>"
    exit 1
fi

script_file_name="${1}"
container_name="${2}"

echo "Execute script '${script_file_name}' in '${container_name}' container ..."
cat "${script_file_name}" > /tmp/init.sh
cd /tmp && tar -cf - init.sh --mode u=+rx,g=+rx,o=+rx --owner root --group root | docker cp - "${container_name}:/tmp"
docker exec -it "${container_name}" /tmp/init.sh
echo "Script '${script_file_name}' executed successfully."