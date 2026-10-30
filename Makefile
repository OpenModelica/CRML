## just a sample Makefile with graddle commands, will be improved

detected_OS ?= $(shell uname -s)

GRADLE_CMD=gradlew.bat

ifeq ($(detected_OS),Darwin)
	GRADLE_CMD := ./gradlew
else ifeq (MINGW,$(findstring MINGW,$(detected_OS)))
	GRADLE_CMD := ./gradlew
else
	GRADLE_CMD := ./gradlew
endif

all: build

test: tests

tests:
	$(GRADLE_CMD) test

test-dom:
	$(GRADLE_CMD) :language:clean :language:test --tests "crml.language.dom.specification.*"

test-crmlcv2:
	$(GRADLE_CMD) :compiler:clean :compiler:test --tests "crml.compiler.crmlcv2.specification.*"

# Simulates the generated Modelica with omc and compares it with the reference
# results. Picks up every class in crml.compiler.crmlcv2.verification named
# Verification<Suite>; SUITE narrows it, e.g. make test-verification SUITE=ETL.
# Report: submodules/compiler/build/verification_report.html
test-verification:
	$(GRADLE_CMD) :compiler:verificationTest --tests "crml.compiler.crmlcv2.verification.Verification$(SUITE)*"

test-etl:
	$(GRADLE_CMD) test --tests "ctests.ETLTests*"

test-forml:
	$(GRADLE_CMD) test --tests "ctests.FORMLTests*"

build:
	$(GRADLE_CMD) build

# Output: submodules/<module>/build/docs/javadoc/index.html
javadoc:
	$(GRADLE_CMD) javadoc

# All the projects in one site, build/docs/javadoc/index.html
javadoc-all:
	$(GRADLE_CMD) javadocAll

# Serves the javadoc-all site with the JDK's HTTP server until Ctrl+C,
# on http://localhost:8001/ unless PORT is given, e.g. make serve-javadoc PORT=9000
serve-javadoc:
	$(GRADLE_CMD) serveJavadoc $(if $(PORT),-Pport=$(PORT))

cleanall: clean
clean:
	rm -rf build bin generated

# beware, this will remove *everything* that is was not added in the git repo
gitclean:
	git clean -fdx

distribution: all
	$(GRADLE_CMD) shadowJar
	