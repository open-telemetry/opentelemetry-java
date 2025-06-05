FROM python:3.13.4@sha256:eb120d016adcbc8bac194e15826bbb4f1d1569d298d8817bb5049ed5e59f41d9 AS build

# Main branch SHA as of April-1-2021
ARG TRACECONTEXT_GIT_TAG="dcd3ad9b7d6ac36f70ff3739874b73c11b0302a1"

WORKDIR /workspace

ADD https://github.com/w3c/trace-context/archive/${TRACECONTEXT_GIT_TAG}.zip /workspace/trace-context.zip
# Unzips to folder
RUN unzip trace-context.zip
RUN rm trace-context.zip
RUN mv trace-context-${TRACECONTEXT_GIT_TAG}/test /tracecontext-testsuite

FROM python:3.13.4-slim@sha256:d97b595c5f4ac718102e5a5a91adaf04b22e852961a698411637c718d45867c8

RUN pip install aiohttp

WORKDIR /tracecontext-testsuite
COPY --from=build /tracecontext-testsuite /tracecontext-testsuite

ENTRYPOINT ["python", "test.py"]
