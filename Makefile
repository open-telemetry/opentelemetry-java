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
	# TODO(anuraaga): Remove version specificer after next release creates a tag on master.
	./gradlew artifactoryPublish -Prelease.version=0.10.0-SNAPSHOT
endif

.PHONY: publish-release-artifacts
publish-release-artifacts:
	./gradlew bintrayUpload
