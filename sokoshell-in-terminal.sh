mvn compile

if [ "$1" = "debug" ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dfile.encoding=UTF-8 -classpath /home/poulpogaz/Documents/MP2I/TIPE/target/classes:/home/poulpogaz/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell2
else
  java -Dfile.encoding=UTF-8 -classpath /home/poulpogaz/Documents/MP2I/TIPE/target/classes:/home/poulpogaz/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell2
fi