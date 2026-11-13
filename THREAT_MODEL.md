# Threat model

See the [Security Policy](https://github.com/open-telemetry/.github/blob/main/SECURITY.md) for OpenTelemetry projects.

Additional context specific to this project can be found below.

## What this project does and where untrusted input enters

- This project provides the Java OpenTelemetry API and SDK for recording, processing, and exporting traces, metrics, logs, and profiles.
- Untrusted input enters through telemetry recorded by applications and instrumentation, and through headers used for baggage and trace propagation.
- Additional network inputs include exporter responses, Prometheus scrape requests, and Jaeger remote sampling strategies.
- SDK configuration is normally controlled by the application operator.

## Components that matter most / least

The following components matter most:

- `api/all`, `api/incubator`, and `extensions/trace-propagators`: core primitives for recording telemetry and propagating context within and across processes.
- `sdk/trace`, `sdk/metrics`, `sdk/logs`, `sdk/profiles`, and `sdk/common`: telemetry processing, aggregation, and resource limits.
- `exporters/otlp`, `exporters/common`, and `exporters/sender`: telemetry serialization and HTTP and gRPC transport.
- `exporters/prometheus`: Prometheus HTTP scrape endpoint and metric serialization.
- `sdk-extensions/autoconfigure`, `sdk-extensions/autoconfigure-spi`, and `sdk-extensions/declarative-config`: SDK configuration through environment variables, system properties, YAML files, and service providers.
- `sdk-extensions/jaeger-remote-sampler`: sampling configuration received from a remote gRPC endpoint.

The following components have lower priority:

- `exporters/logging` and `exporters/logging-otlp`: exporters primarily used for local development and diagnostics.
- `sdk/testing` and `testing-internal`: testing utilities.
- `opencensus-shim` and `opentracing-shim`: deprecated compatibility shims.

## How to exercise it

- Unit tests and fuzz tests are colocated with their modules. Cross-component tests live under `integration-tests`.
- See [build guidance](docs/knowledge/build.md) for requirements and commands.

## How you rate severity

Follow the OpenTelemetry Security Policy, including its availability assessment and severity guidance. No additional Java-specific severity rules apply.

## Anything to leave alone

Ignore tests, test utilities, benchmarks, and other code not intended for production use.
