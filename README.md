# Dminor in Formulog

This repository contains a Formulog-based implementation of a type checker for
Dminor, a data processing language with refinement types and type checks [1]. It
also includes a tool for turning a Dminor program into a database of Formulog
facts that can be used as input for the type checker.

## Dependencies

* [Formulog](https://github.com/HarvardPL/formulog) (v0.3.\_)

To build the fact-extractor tool from source, you will also need:

* Maven
* JDK 1.8+

## Extracting Formulog facts from Dminor programs

To type check a Dminor program, you first need to turn it into a database of
Formulog facts. We've provided a tool to do so. You can download it from the
GitHub repo, or build it from source using the command `mvn package`. This
command will create an executable JAR in the target directory called something
like `dminor-in-formulog-X.Y.Z-SNAPSHOT-jar-with-dependencies.jar`.

To run the tool on a Dminor file, use

```
java -jar dminor-in-formulog.jar [file]
```

where `dminor-in-formulog.jar` is the JAR you downloaded or built. This will
create a directory `[file]_facts/` in the same directory as the input file;
this directory contains CSV files of Formulog input facts.

## Running the type checker

Once you've extracted facts from a Dminor program, to run the Formulog-based
type checker on them, use

```
java -DfactDirs=[fact_dir] -jar formulog.jar src/main/formulog/dminor.flg
```

where `formulog.jar` is the JAR for the Formulog runtime. A bunch of derived
facts will be printed out. This output will include the fact `prog_ok` if type
checking succeeds. If it fails, there will be at least one `func_not_ok` or
`type_not_ok` fact generated.

## Third-party libraries

This project uses third-party libraries. You can generate a list of these
libraries and download their associated licenses with this command:

```
mvn license:download-licenses
```

The generated content can be found in the `target/generated-resources/`
directory.

## References

[1] Gavin M. Bierman, Andrew D. Gordon, Cătălin Hriţcu, and David Langworthy. 2012. Semantic subtyping with an SMT solver. _Journal of Functional Programming_ 22, 1 (2012), 31–105.
