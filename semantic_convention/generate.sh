#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf opentelemetry-specification || true
mkdir opentelemetry-specification
cd opentelemetry-specification

git init
git remote add origin https://github.com/open-telemetry/opentelemetry-specification.git
# https://github.com/open-telemetry/opentelemetry-specification/pull/1027
git fetch origin 662baae949f01a8ecc950426bf09283be1b657de
git reset --hard FETCH_HEAD
cd ${DIR}

docker run --rm \
  -v $(pwd)/opentelemetry-specification/semantic_conventions:/source \
  -v $(pwd)/templates:/templates \
  -v $(pwd)/../api/src/main/java/io/opentelemetry/trace/attributes/:/output \
  otel/semconvgen \
  -f /source code \
  --template /templates/SemanticAttributes.java.j2 \
  --output /output/SemanticAttributes.java \
  -Dclass=SemanticAttributes \
  -Dpkg=io.opentelemetry.trace.attributes

cd ..
./gradlew spotlessApply

cd ${DIR}
