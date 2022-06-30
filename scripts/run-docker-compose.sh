#!/bin/bash

set -eu

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

if [[ "${#}" -ne 3 ]]; then
    echo "Illegal number of parameters: <script> <project_name> <file_path> <command>"
    echo "  <project_name>: name of project"
    echo "  <file_path>: file path"
    echo "  <command>: start | stop"
    exit 1
fi

project_name="${1}"
file_path="${2}"
command="${3}"
docker_compose_command="docker-compose --project-name ${project_name} --file ${file_path}"

case ${command} in
  start)
    ${docker_compose_command} up -d
    ;;

  stop)
    ${docker_compose_command} down
    ;;

  *)
    echo "Unknown command: ${command}"
    exit 1
    ;;
esac