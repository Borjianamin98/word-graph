#!/bin/bash

set -eu

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
GRAPH_DRAWER_DIRECTORY="${SCRIPT_DIR}/../graph-drawer"
cd "${GRAPH_DRAWER_DIRECTORY}" || exit 1

# apt install python3-pip -y --no-install-recommends
# pip install pipenv
pipenv install
pipenv run python main.py
