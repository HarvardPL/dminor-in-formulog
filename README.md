# dminor-to-formulog
A tool for turning a Dminor program into a database of Formulog facts.

To build, use `mvn package`.

To run it on a Dminor file, use
```
java -jar target/dminor-to-formulog-0.0.1-SNAPSHOT-jar-with-dependencies.jar [file]
```
This will create a directory `[file]_facts/` in the same directory as the input
file; this directory contains CSV files of Formulog input facts.
