# OpenTelemetry SDK Autoconfigure

This artifact implements environment-based autoconfiguration of the OpenTelemetry SDK. This can be
an alternative to programmatic configuration using the normal SDK builders.

All options support being passed as Java system properties, e.g., `-Dotel.traces.exporter=zipkin` or
environment variables, e.g., `OTEL_TRACES_EXPORTER=zipkin`.

The full documentation on the available configuration options has been moved to
[opentelemetry.io](https://opentelemetry.io/docs/languages/java/configuration/)
