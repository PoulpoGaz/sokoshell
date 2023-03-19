#!/bin/bash

# argument parsing

DEBUG=0
MAIN_CLASS=fr.valax.sokoshell.SokoShell
N=0

while getopts :dm: opt; do
  case "$opt" in
    d)
      DEBUG=1
      ((N++))
      ;;
    m)
      MAIN_CLASS="$OPTARG"
      ((N += 2))
      ;;
    \?)
      break
  esac
done

shift $N

# compile and run

# DO NOT MODIFY!!!!! IT'S SUPER ANNOYING!!!! ADD MVN TO YOUR PATH!!!!!
mvn compile

PATH_TO_PROJECT=$('pwd')

CLASSPATH="$PATH_TO_PROJECT"/args/target/classes:\
"$PATH_TO_PROJECT"/interval/target/classes:\
"$PATH_TO_PROJECT"/sokoshell/target/classes:\
"$HOME"/.m2/repository/org/jline/jline/3.23.0/jline-3.23.0.jar:\
"$HOME"/.m2/repository/io/github/poulpogaz/json/1.2.3/json-1.2.3.jar:\
"$HOME"/.m2/repository/org/dom4j/dom4j/2.1.4/dom4j-2.1.4.jar:\
"$HOME"/.m2/repository/jaxen/jaxen/2.0.0/jaxen-2.0.0.jar:\
"$HOME"/.m2/repository/org/openjdk/jol/jol-core/0.17/jol-core-0.17.jar

JVM_ARGS="-Dfile.encoding=UTF-8 -classpath ${CLASSPATH} ${MAIN_CLASS}"

# word splitting is very important
if [ $DEBUG -ne 0 ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -Djol.magicFieldOffset=true $JVM_ARGS $*
else
  java -Xmx6G -Djol.magicFieldOffset=true $JVM_ARGS $*
fi