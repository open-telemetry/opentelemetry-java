# Changelog

## Unreleased

- Add helper API to get Tracer/Meter

## 0.4.0 - 2020-05-04
- Initial implementation of the Zipkin exporter.
- **Breaking change:** Move B3 propagator to a contrib package
- Add support for Jaeger propagator
- Start implementing support for configuring exporters using Config pattern with support to load from environment variables and system properties.
- Add support to flush the entire SDK processors and exporter pipelines.
- Mark all threads/pools as daemon.
- Add support for Jaeger remote sampler.

## 0.3.0 - 2020-03-27
- Initial Java API and SDK for context, trace, metrics, resource.
- Initial implementation of the Jaeger exporter.
- Initial implementation of the OTLP exporters for trace and metrics.