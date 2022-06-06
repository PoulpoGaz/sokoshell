mvn compile

if [ "$1" = "debug" ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dfile.encoding=UTF-8 \
       -classpath /media/USER_DATA_PART/DOCS/SCHOOL/MPI/TIPE/GIT/TIPE/target/classes:/home/user/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell
else
  java -Dfile.encoding=UTF-8 \
       -classpath /media/USER_DATA_PART/DOCS/SCHOOL/MPI/TIPE/GIT/TIPE/target/classes:/home/user/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell
fi