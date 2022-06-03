# SokoShell

Version 0.1

### How to debug and keep autocompletion?

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `sh sokoshell-in-terminal.sh debug`
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.

sokoshell-in-terminal.sh:

```shell
mvn compile

if [ "$1" = "debug" ]
then
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Dfile.encoding=UTF-8 -classpath PATH_TO_PROJECT/target/classes:HOME_FOLDER/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell
else
  java -Dfile.encoding=UTF-8 -classpath PATH_TO_PROJECT/target/classes:HOME_FOLDER/.m2/repository/org/jline/jline/3.21.0/jline-3.21.0.jar fr.valax.sokoshell.SokoShell
fi
```