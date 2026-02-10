FROM python:3.14.3@sha256:4b827abf32c14b7df9a0dc5199c2f0bc46e2c9862cd5d77eddae8a2cd8460f60 AS build

# Main branch SHA as of April-1-2021
ARG TRACECONTEXT_GIT_TAG="dcd3ad9b7d6ac36f70ff3739874b73c11b0302a1"

WORKDIR /workspace

ADD https://github.com/w3c/trace-context/archive/${TRACECONTEXT_GIT_TAG}.zip /workspace/trace-context.zip
# Unzips to folder
RUN unzip trace-context.zip
RUN rm trace-context.zip
RUN mv trace-context-${TRACECONTEXT_GIT_TAG}/test /tracecontext-testsuite

FROM python:3.14.3-slim@sha256:486b8092bfb12997e10d4920897213a06563449c951c5506c2a2cfaf591c599f

RUN pip install aiohttp

WORKDIR /tracecontext-testsuite
COPY --from=build /tracecontext-testsuite /tracecontext-testsuite

ENTRYPOINT ["python", "test.py"]
