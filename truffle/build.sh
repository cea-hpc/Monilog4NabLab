#!/usr/bin/env bash

mvn install:install-file    -Dfile=../plugins/fr.cea.nabla/target/fr.cea.nabla-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=../plugins/fr.cea.nabla.ir/target/fr.cea.nabla.ir-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla.ir    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=../plugins/fr.cea.nabla.javalib/target/fr.cea.nabla.javalib-0.4.0-SNAPSHOT.jar    -DgroupId=fr.cea.nabla    -DartifactId=fr.cea.nabla.javalib    -Dversion=0.4.0-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn package

