# Changelog

## Unreleased

## 0.7.0 - 2020-08-02

NOTE: This release contains non-backward-compatible breaking SDK changes

- Added an InMemoryMetricExporter
- Added a toBuilder method to Labels
- Added some semantic attribute constants
- New ZPages extension module with TraceZ and TraceConfigZ pages implemented
- Some overloads added for setting the parent Context
- Some performance improvements in HttpTraceContext implementation
- Removed null checks from the Trace APIs
- The bare API will no longer generate Trace and Span IDs when there is no parent trace context.
- Null Strings are no longer valid keys for Attributes
- BREAKING CHANGE: The Sampler API was changed
- Default endpoint is now set for the OTLP exporters
- BREAKING CHANGE: Jaeger exporter env vars/system properties were updated
- Resource attributes may now be set with a System Property as well as the environment variable.
- Added a propagator for Lightstep OpenTracing propagator
- The ZipkinSpanExporter now defaults to using the OkHttpSender
- The default Sampler in the SDK is now the ParentOrElse sampler
- BUGFIX: SpanWrapper SpanData implementation is now truly Immutable
- Added some simple logging for failed export calls
- Quieted the noisy B3 propagator
- Public constants were added for exporter configuration options
- Added a new configuration option to limit the size of Span attributes
- Many thanks for contributions from @anuraaga, @dengliming, @iNikem, @wtyanan, @williamhu99, @trask, @Oberon00, @MitchellDumovic, @FrankSpitulski, @heyams, @ptravers, @thisthat, @albertteoh, @evantorrie, @neeraj97,

## 0.6.0 - 2020-07-01

NOTE: This release contains non-backward-compatible breaking API and SDK changes

- Introduction of immutable Attributes for SpansEvents, Links and Resources
- Introduction of immutable Labels for Metric Instruments and Recordings
- BUGFIX: make sure null Points are not propagated to metric exporters
- Added a propagator for AWS X-Ray
- BUGFIX: IntervalMetricReader now handles exceptions thrown by metric exporters
- Renamed contrib modules to "extensions" (Note: this changes the published artifact names, as well)
- Converted CorrelationContext entry keys and values to simple Strings
- Enhanced OTLP exporter configuration options
- Added new SDK Telemetry Resource populator
- Introduced an new MultiTracePropagator to handle multiple propagation formats
- Added new AWS Resource populators
- Added an extension to populate span data into log4j2 log formats.
- Changed the MinMaxSumCount aggregations for ValueRecorders to always aggregate deltas, rather than cumulative
- Updated the OTLP protobuf and exporter to version 0.4.0 of the OTLP protobufs.

## 0.5.0 - 2020-06-04

TODO: fill this out

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