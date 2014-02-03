java-task-killer
================

The aim of this tiny tool is to be able to kill java based programs.

## Usage

Start your java task in the way that the command contains a special part.
A good trick is to use a system property that is not used by the programs
but identifies the process.

E.g.: java -DmyDummyProp=XXX-XXX-XXX -jar myApp.jar

You can kill this task by calling:

java -jar java-task-killer.jar --startCommandPart=XXX-XXX-XXX

The tool uses the jps tool of java. In case the bin directory of the Java
installation is not on the path, the tool will not work.

## Supported operating systems

On windows the following command is used:

taskkill /F /T /PID

On every other operating systems the following command is used:

kill -2
