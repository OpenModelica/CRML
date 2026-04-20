# CRML compiler

A POC compiler from CRML to Modelica. For questions contact lena.buffoni at liu.com


## Download it via github actions

Go to: [![Makefile CI](https://github.com/OpenModelica/CRML/actions/workflows/makefile.yml/badge.svg)](https://github.com/OpenModelica/CRML/actions/workflows/makefile.yml) then click on the last green action and download the artifact.

Unzip it to some place and run:
```
java -jar crml-compiler-all.jar --help
```
To see all the help flags and configurations

To run the test suite:
```
java -jar crml-compiler-all.jar --testsuite
```
A directory generated/ will be created and for each crml 
test in the directory above a Modelica file will be created 
in the generated/ directory.

## Build 

You can use gradle to build the project from scratch.

# CRML editor
## VSCode plugin

[project repository](https://github.com/lenaRB/crml-vscode)

## Notepad plugin

[project resources](resources/notepad_profile)

# CRML specification

The documentation for CRML can be found [HERE](documentation/crml_specification)
