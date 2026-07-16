#!/bin/bash -e

version=$1
versionWithSnapshot="$version-SNAPSHOT"

sed -Ei "s/var ver = \"[^\"]*\"/var ver = \"$version\"/" version.gradle.kts

sed -Ei "1 s/(Comparing source compatibility of [a-z-]+)-[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?.jar/\1-$versionWithSnapshot.jar/" docs/apidiffs/current_vs_latest/*.txt
