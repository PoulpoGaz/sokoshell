#!/bin/bash

# argument parsing

DEBUG=0
MAIN_CLASS=fr.valax.sokoshell.SokoShell
N=0

while getopts dm: opt; do
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
      exit 1
  esac
done

shift $N

# compile and run

/opt/maven/bin/mvn compile

PATH_TO_PROJECT=$('pwd')

CLASSPATH="$PATH_TO_PROJECT"/args/target/classes:\
"$PATH_TO_PROJECT"/interval/target/classes:\
"$PATH_TO_PROJECT"/sokoshell/target/classes:\
"$HOME"/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar:\
"$HOME"/.m2/repository/io/github/poulpogaz/json/1.2.2/json-1.2.2.jar:\
"$HOME"/.m2/repository/org/dom4j/dom4j/2.1.3/dom4j-2.1.3.jar:\
"$HOME"/.m2/repository/jaxen/jaxen/1.2.0/jaxen-1.2.0.jar:\
"$HOME"/.m2/repository/org/openjdk/jol/jol-core/0.16/jol-core-0.16.jar

JVM_ARGS="-Dfile.encoding=UTF-8 -classpath ${CLASSPATH} ${MAIN_CLASS}"

# word splitting is very important
if [ $DEBUG -ne 0 ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -Djol.magicFieldOffset=true $JVM_ARGS $*
else
  java -Xmx6G -Djol.magicFieldOffset=true $JVM_ARGS $*
fi