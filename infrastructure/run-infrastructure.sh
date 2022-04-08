#!/bin/bash

set -eu

if [[ "${#}" -ne 1 ]]; then
    echo "Illegal number of parameters: <script> <command>"
    echo "  <command>: start | stop"
    exit 1
fi

command="${1}"
docker_compose_command="docker-compose --project-name complex_infrastructure --file docker-compose.yaml"

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