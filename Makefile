.DEFAULT_GOAL := test

.PHONY: test
test:
	./gradlew clean assemble check --stacktrace --info

.PHONY: init-git-submodules
init-git-submodules:
	git submodule init
	git submodule update

.PHONY: verify-format
verify-format:
	./gradlew verGJF

.PHONY: publish-snapshots
publish-snapshots:
ifeq ($(CIRCLE_BRANCH),master)
	./gradlew artifactoryPublish
endif
