#!/bin/bash

function addProperty() {
  local path=$1
  local name=$2
  local value=$3

  local entry="<property><name>$name</name><value>${value}</value></property>"
  local escaped_entry=$(echo "${entry}" | sed 's/\//\\\//g')
  sed -i "/<\/configuration>/ s/.*/${escaped_entry}\n&/" "${path}"
}

function configure() {
    local path=$1
    local module=$2
    local envPrefix=$3

    local var
    local value

    echo "Configuring $module"
    for c in $(printenv | perl -sne 'print "$1 " if m/^${envPrefix}_(.+?)=.*/' -- -envPrefix="${envPrefix}"); do
        name=$(echo "${c}" | perl -pe 's/___/-/g; s/__/@/g; s/_/./g; s/@/_/g;')
        var="${envPrefix}_${c}"
        value=${!var}
        echo " - Setting ${name}=${value}"
        addProperty "${path}" "${name}" "$value"
    done
}

configure /opt/hadoop/etc/hadoop/core-site.xml core CORE_CONF
configure /opt/hadoop/etc/hadoop/hdfs-site.xml hdfs HDFS_CONF
configure /opt/hadoop/etc/hadoop/yarn-site.xml yarn YARN_CONF
configure /opt/hadoop/etc/hadoop/httpfs-site.xml httpfs HTTPFS_CONF
configure /opt/hadoop/etc/hadoop/kms-site.xml kms KMS_CONF
configure /opt/hadoop/etc/hadoop/mapred-site.xml mapred MAPRED_CONF

# HDFS
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.namenode.rpc-bind-host 0.0.0.0
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.namenode.servicerpc-bind-host 0.0.0.0
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.namenode.http-bind-host 0.0.0.0
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.namenode.https-bind-host 0.0.0.0
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.client.use.datanode.hostname true
addProperty /opt/hadoop/etc/hadoop/hdfs-site.xml dfs.datanode.use.datanode.hostname true

# YARN
addProperty /opt/hadoop/etc/hadoop/yarn-site.xml yarn.resourcemanager.bind-host 0.0.0.0
addProperty /opt/hadoop/etc/hadoop/yarn-site.xml yarn.nodemanager.bind-host 0.0.0.0

# MAPRED
addProperty /opt/hadoop/etc/hadoop/mapred-site.xml yarn.nodemanager.bind-host 0.0.0.0

function wait_for_it()
{
    local serviceport=$1
    local service=${serviceport%%:*}
    local port=${serviceport#*:}
    local retry_seconds=5
    local max_try=100
    i=1

    nc -z "${service}" "${port}"
    result=${?}

    until [ ${result} -eq 0 ]; do
      echo "[${i}/${max_try}] check for ${service}:${port}..."
      echo "[${i}/${max_try}] ${service}:${port} is not available yet"
      if (( "${i}" == "${max_try}" )); then
        echo "[${i}/${max_try}] ${service}:${port} is still not available; giving up after ${max_try} tries. :("
        exit 1
      fi

      echo "[${i}/${max_try}] try in ${retry_seconds}s once again ..."
      (( i++ ))
      sleep ${retry_seconds}

      nc -z "${service}" "${port}"
      result=${?}
    done
    echo "[${i}/${max_try}] ${service}:${port} is available."
}

for i in "${SERVICE_PRECONDITION[@]}"
do
    wait_for_it "${i}"
done
echo "All precondition services are available."

echo "$@"
exec "$@"
