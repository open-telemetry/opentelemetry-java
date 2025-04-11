FROM python:3.13.3@sha256:9819e5616923079cc16af4a93d4be92c0c487c6e02fd9027220381f3e125d64a AS build

# Main branch SHA as of April-1-2021
ARG TRACECONTEXT_GIT_TAG="dcd3ad9b7d6ac36f70ff3739874b73c11b0302a1"

WORKDIR /workspace

ADD https://github.com/w3c/trace-context/archive/${TRACECONTEXT_GIT_TAG}.zip /workspace/trace-context.zip
# Unzips to folder
RUN unzip trace-context.zip
RUN rm trace-context.zip
RUN mv trace-context-${TRACECONTEXT_GIT_TAG}/test /tracecontext-testsuite

FROM python:3.13.3-slim@sha256:21e39cf1815802d4c6f89a0d3a166cc67ce58f95b6d1639e68a394c99310d2e5

RUN pip install aiohttp

WORKDIR /tracecontext-testsuite
COPY --from=build /tracecontext-testsuite /tracecontext-testsuite

ENTRYPOINT ["python", "test.py"]
