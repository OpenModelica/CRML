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

test-omcv2:
	$(GRADLE_CMD) :compiler:clean :compiler:test --tests "crml.compiler.omcv2.specification.*"

test-etl:
	$(GRADLE_CMD) test --tests "ctests.ETLTests*"

test-forml:
	$(GRADLE_CMD) test --tests "ctests.FORMLTests*"

build:
	$(GRADLE_CMD) build

cleanall: clean
clean:
	rm -rf build bin generated

# beware, this will remove *everything* that is was not added in the git repo
gitclean:
	git clean -fdx

distribution: all
	$(GRADLE_CMD) shadowJar
	