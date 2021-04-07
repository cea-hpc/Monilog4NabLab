#!/usr/bin/env bash

start=$(pwd)

#Build truffle interpreter
echo "Building truffle interpreter component."
cd $1
mvn package
cd $start/truffle
mvn install:install-file    -Dfile=$1/plugins/fr.cea.nabla/target/fr.cea.nabla-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=$1/plugins/fr.cea.nabla.ir/target/fr.cea.nabla.ir-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla.ir    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=$1/plugins/fr.cea.nabla.javalib/target/fr.cea.nabla.javalib-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla.javalib    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn package
echo "Done."

#Build monilog
echo "Building monilog component."
cd $2
mvn install
cd truffle/org.gemoc.monilog.tool
mvn package
echo "Done."

echo "Moving components to /plugins/fr.cea.nabla.ui.graalvm.p2.main"
cd $start/plugins/fr.cea.nabla.ui.graalvm.p2.main
cp $start/truffle/component/nabla-component.jar .
cp $2/truffle/org.gemoc.monilog.tool/target/monilogger.jar .
echo "Done."

