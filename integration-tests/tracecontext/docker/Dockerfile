FROM python:3@sha256:7be9b757ea8fa90b43efd604dd5ef2923364230f00564b2ea7f0982d24b374c9 AS build

# Main branch SHA as of April-1-2021
ARG TRACECONTEXT_GIT_TAG="dcd3ad9b7d6ac36f70ff3739874b73c11b0302a1"

WORKDIR /workspace

ADD https://github.com/w3c/trace-context/archive/${TRACECONTEXT_GIT_TAG}.zip /workspace/trace-context.zip
# Unzips to folder
RUN unzip trace-context.zip
RUN rm trace-context.zip
RUN mv trace-context-${TRACECONTEXT_GIT_TAG}/test /tracecontext-testsuite

FROM python:3-slim@sha256:031ebf3cde9f3719d2db385233bcb18df5162038e9cda20e64e08f49f4b47a2f

RUN pip install aiohttp

WORKDIR /tracecontext-testsuite
COPY --from=build /tracecontext-testsuite /tracecontext-testsuite

ENTRYPOINT ["python", "test.py"]
