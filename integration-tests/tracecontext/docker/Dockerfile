FROM python:3.13.7@sha256:c1dab8c06c4fe756fd4316f33a2ba4497be09fa106d2cd9782c33ee411193f9c AS build

# Main branch SHA as of April-1-2021
ARG TRACECONTEXT_GIT_TAG="dcd3ad9b7d6ac36f70ff3739874b73c11b0302a1"

WORKDIR /workspace

ADD https://github.com/w3c/trace-context/archive/${TRACECONTEXT_GIT_TAG}.zip /workspace/trace-context.zip
# Unzips to folder
RUN unzip trace-context.zip
RUN rm trace-context.zip
RUN mv trace-context-${TRACECONTEXT_GIT_TAG}/test /tracecontext-testsuite

FROM python:3.13.7-slim@sha256:58c30f5bfaa718b5803a53393190b9c68bd517c44c6c94c1b6c8c172bcfad040

RUN pip install aiohttp

WORKDIR /tracecontext-testsuite
COPY --from=build /tracecontext-testsuite /tracecontext-testsuite

ENTRYPOINT ["python", "test.py"]
