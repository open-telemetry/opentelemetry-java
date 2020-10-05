.DEFAULT_GOAL := test

.PHONY: test
test:
	./gradlew clean assemble check --stacktrace

.PHONY: test-with-docker
test-with-docker:
	./gradlew -Penable.docker.tests=true clean assemble check --stacktrace

.PHONY: benchmark
benchmark:
	./gradlew compileJmhJava

.PHONY: init-git-submodules
init-git-submodules:
	git submodule init
	git submodule update

.PHONY: verify-format
verify-format:
	./gradlew spotlessCheck

.PHONY: publish-snapshots
publish-snapshots:
ifeq ($(CIRCLE_BRANCH),master)
	./gradlew artifactoryPublish
endif

.PHONY: publish-release-artifacts
publish-release-artifacts:
	./gradlew bintrayUpload
