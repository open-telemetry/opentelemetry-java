#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf opentelemetry-specification || true
git clone https://github.com/open-telemetry/opentelemetry-specification.git --depth 1

docker run --rm \
  -v $(pwd)/opentelemetry-specification/semantic_conventions:/source \
  -v $(pwd)/templates:/templates \
  -v $(pwd)/../api/src/main/java/io/opentelemetry/trace/attributes/:/output \
  otel/semconvgen \
  -f /source code \
  --template /templates/SemanticAttributesV2.java.j2 \
  --output /output/SemanticAttributesV2.java \
  -Dclass=SemanticAttributesV2 \
  -Dpkg=io.opentelemetry.trace.attributes

cd ..
./gradlew spotlessApply

cd ${DIR}
