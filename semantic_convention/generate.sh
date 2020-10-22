#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf opentelemetry-specification || true
git clone https://github.com/open-telemetry/opentelemetry-specification.git --depth 1
# https://github.com/open-telemetry/opentelemetry-specification/pull/1027
git checkout 662baae949f01a8ecc950426bf09283be1b657de

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
