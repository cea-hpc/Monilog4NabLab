#!/usr/bin/env bash

start=$(pwd)

#Build truffle interpreter
echo "Building truffle interpreter component."
cd $start/truffle
mvn clean package --settings $start/.github/workflows/settings.xml
echo "Done."

echo "Moving nabla-component to /plugins/fr.cea.nabla.ui.graalvm.p2.main"
cd $start/plugins/fr.cea.nabla.ui.graalvm.p2.main
cp $start/truffle/component/nabla-component.jar .
echo "Downloading monilogger.jar to /plugins/fr.cea.nabla.ui.graalvm.p2.main"
curl -H "Accept: application/zip" -L https://github.com/gemoc/monilog/releases/download/v2.0.0/monilogger.jar -o monilogger.jar
echo "Done."

