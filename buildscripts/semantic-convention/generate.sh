#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ROOT_DIR="${SCRIPT_DIR}/../../"
# freeze the spec version to make SemanticAttributes generation reproducible
# this hash was obtained by calling `git rev-parse master` on 1st Dec 2020
# we can't use a version tag here because otel-spec releases are very rare
SPEC_VERSION=181a1afdb201f1334fb032960bb1672cf5a0272f

cd ${SCRIPT_DIR}

rm -rf opentelemetry-specification || true
mkdir opentelemetry-specification
cd opentelemetry-specification

git init
git remote add origin https://github.com/open-telemetry/opentelemetry-specification.git
git fetch origin "$SPEC_VERSION"
git reset --hard FETCH_HEAD
cd ${SCRIPT_DIR}

docker run --rm \
  -v ${SCRIPT_DIR}/opentelemetry-specification/semantic_conventions:/source \
  -v ${SCRIPT_DIR}/templates:/templates \
  -v ${ROOT_DIR}/api/trace/src/main/java/io/opentelemetry/api/trace/attributes/:/output \
  otel/semconvgen \
  -f /source code \
  --template /templates/SemanticAttributes.java.j2 \
  --output /output/SemanticAttributes.java \
  -Dclass=SemanticAttributes \
  -Dpkg=io.opentelemetry.api.trace.attributes

cd "$ROOT_DIR"
./gradlew spotlessApply
