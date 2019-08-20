.DEFAULT_GOAL := test

.PHONY: test
test:
	 ./gradlew clean assemble check --stacktrace

.PHONY: init
init:
	git submodule init
	git submodule update
