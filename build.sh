#!/usr/bin/env bash

start=$(pwd)

#Build truffle interpreter
echo "Building truffle interpreter component."
cd $start/truffle
mvn clean package
echo "Done."

#Build monilog
echo "Building monilog component."
cd $1
mvn install
cd truffle/org.gemoc.monilog.tool
mvn clean package
echo "Done."

echo "Moving components to /plugins/fr.cea.nabla.ui.graalvm.p2.main"
cd $start/plugins/fr.cea.nabla.ui.graalvm.p2.main
cp $start/truffle/component/nabla-component.jar .
cp $1/truffle/org.gemoc.monilog.tool/target/monilogger.jar .
echo "Done."

