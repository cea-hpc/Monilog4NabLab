#!/usr/bin/env bash

tar -xf $1/graalvm-ce-java11-linux-amd64-21.0.0.tar.gz
$1/graalvm-ce-java11-21.0.0/bin/gu -L install -f $2
mkdir -p $1/graalvm-ce-java11-21.0.0/tools/monilogger
cp $3 $1/graalvm-ce-java11-21.0.0/tools/monilogger/
