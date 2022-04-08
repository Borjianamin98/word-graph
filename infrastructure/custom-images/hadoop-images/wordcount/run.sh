#!/bin/bash

export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

"${HADOOP_HOME}/bin/hadoop" com.sun.tools.javac.Main WordCount.java
jar cf WordCount.jar WordCount*.class

"${HADOOP_HOME}/bin/hadoop" fs -mkdir /input
"${HADOOP_HOME}/bin/hadoop" fs -rm -r /output 2> /dev/null

# Run wordcount map-reduce
echo -e "word1\nword1\nword1\nword2" > /tmp/input.txt
"${HADOOP_HOME}/bin/hadoop" fs -copyFromLocal /tmp/input.txt /input/
"${HADOOP_HOME}/bin/hadoop" jar WordCount.jar WordCount /input /output

# Show results
"${HADOOP_HOME}/bin/hadoop" fs -cat /output/*

"${HADOOP_HOME}/bin/hadoop" fs -rm -r /input
"${HADOOP_HOME}/bin/hadoop" fs -rm -r /output