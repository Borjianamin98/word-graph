#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")

cd "${SCRIPT_DIR}" || exit 1
docker build --tag spark-base --file base/Dockerfile base
docker build --tag spark-master --file master/Dockerfile master
docker build --tag spark-worker --file worker/Dockerfile worker