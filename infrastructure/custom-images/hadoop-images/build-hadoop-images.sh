#!/bin/bash

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
cd "${SCRIPT_DIR}" || exit 1

docker build --tag hadoop-base --file base/Dockerfile base
docker build --tag hadoop-namenode --file namenode/Dockerfile namenode
docker build --tag hadoop-datanode --file datanode/Dockerfile datanode
#docker build --tag hadoop-resourcemanager --file resourcemanager/Dockerfile resourcemanager
#docker build --tag hadoop-nodemanager --file nodemanager/Dockerfile nodemanager
#docker build --tag hadoop-historyserver --file historyserver/Dockerfile historyserver
#docker build --tag hadoop-wordcount --file wordcount/Dockerfile wordcount