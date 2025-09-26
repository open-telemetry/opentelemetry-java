#!/bin/bash
set -e

VERSION="1.33.3"
CURR_DIR="$(pwd)"

cd "$(mktemp -d)"

# Clone Armeria repository
git clone --depth 1 --branch "armeria-${VERSION}" https://github.com/line/armeria.git

cd armeria

# Apply patch to remove Unsafe usage
git apply "${CURR_DIR}/.github/unsafe/armeria-remove-JCTools.patch"
git apply "${CURR_DIR}/.github/unsafe/armeria-update-Caffeine.patch"

# Build the core module (shaded JAR includes all dependencies)
./gradlew :core:shadedJar -x javadoc -x :docs-client:nodeSetup -x :docs-client:npmSetup -x :docs-client:npmInstall -x :docs-client:eslint -x :docs-client:lint -x :docs-client:buildWeb -x :docs-client:copyWeb

# Download the original POM file
curl -sL -o "armeria-${VERSION}.pom" "https://repo1.maven.org/maven2/com/linecorp/armeria/armeria/${VERSION}/armeria-${VERSION}.pom"

# Install core JAR to Maven local repository with POM
mvn install:install-file -q \
    -Dfile="core/build/libs/armeria-untrimmed-${VERSION}.jar" \
    -DpomFile="armeria-${VERSION}.pom"
