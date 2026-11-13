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

# CRML diagrams

[diagram-generator](diagram-generator) is the submodule
[OpenModelica/CRML-diagram](https://github.com/OpenModelica/CRML-diagram), which draws the
class-object diagram of a CRML file in draw.io format, with a parser generated from the grammar of
this compiler; OMEdit uses it to show the diagram of a CRML file. Check it out with
`git submodule update --init` and see its [README](https://github.com/OpenModelica/CRML-diagram#readme)
to build it.

# CRML editor
## VSCode plugin

[project repository](https://github.com/lenaRB/crml-vscode)

## Notepad plugin

[project resources](resources/notepad_profile)

# CRML specification

The documentation for CRML can be found [HERE](documentation/crml_specification)

# License

The CRML compiler is under the OSMC Public License (OSMC-PL) version 1.8 or the GNU AGPL version 3, as for
[OpenModelica](https://github.com/OpenModelica/OpenModelica), see [OSMC-License.txt](OSMC-License.txt).
The libraries are under a different license.
