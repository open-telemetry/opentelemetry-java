.DEFAULT_GOAL := test

.PHONY: test
test:
	./gradlew clean assemble check --stacktrace

.PHONY: init-git-submodules
init-git-submodules:
	git submodule init
	git submodule update

.PHONY: verify-format
verify-format:
	./gradlew verGJF
