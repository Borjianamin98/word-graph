#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")

cd "${SCRIPT_DIR}" || exit 1
docker build --tag java-base --file Dockerfile .