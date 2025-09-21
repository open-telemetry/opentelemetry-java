#!/bin/bash
set -e

VERSION="4.32.1"

cd "$(mktemp -d)"

# Download original artifact and extract
curl -sL -o "protobuf-java-${VERSION}.jar" "https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/${VERSION}/protobuf-java-${VERSION}.jar"

# Extract JAR
mkdir classes
cd classes
jar -xf "../protobuf-java-${VERSION}.jar"
cd ..

# Clone protobuf repository
git clone --depth 1 --branch "v32.1" https://github.com/protocolbuffers/protobuf.git

# Apply patch to remove Unsafe usage
sed -i 's/private static final sun\.misc\.Unsafe UNSAFE = getUnsafe();/private static final sun.misc.Unsafe UNSAFE = null;/' protobuf/java/core/src/main/java/com/google/protobuf/UnsafeUtil.java

# Compile modified classes
javac -cp protobuf-java-${VERSION}.jar -d classes protobuf/java/core/src/main/java/com/google/protobuf/UnsafeUtil.java

# Create new JAR with modified classes
jar -cf "protobuf-java-${VERSION}.jar" -C classes .

# Download the original POM file
curl -sL -o "protobuf-java-${VERSION}.pom" "https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/${VERSION}/protobuf-java-${VERSION}.pom"

# Install to Maven local repository with POM
mvn install:install-file -q \
    -Dfile="protobuf-java-${VERSION}.jar" \
    -DpomFile="protobuf-java-${VERSION}.pom"
