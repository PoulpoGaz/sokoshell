#!/bin/bash

# argument parsing

DEBUG=0
MAIN_CLASS=fr.valax.sokoshell.SokoShell

while getopts dm: opt; do
  case "$opt" in
    d)
      DEBUG=1
      shift
      ;;
    m)
      MAIN_CLASS="$OPTARG"
      shift 2
      ;;
    \?)
      exit 1
  esac
done

# compile and run

mvn compile

PATH_TO_PROJECT=$('pwd')
CLASSPATH="$PATH_TO_PROJECT"/target/classes:"$HOME"/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar
JVM_ARGS="-Dfile.encoding=UTF-8 -classpath ${CLASSPATH} ${MAIN_CLASS}"

# word splitting is very important
if [ $DEBUG -ne 0 ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 $JVM_ARGS $*
else
  java $JVM_ARGS $*
fi