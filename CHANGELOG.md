# Changelog

## Unreleased:

### API

#### Breaking changes:

- The SPI interfaces have moved to a package (not a module) separate from the API packages, and now live in `io.opentelemetry.spi.*` package namespace.
- Builder classes have been moved to the top level, rather than being inner classes. 
For example, rather than `io.opentelemetry.api.trace.Span.Builder`, the builder is now in its own top-level class: `io.opentelemetry.api.trace.SpanBuilder`.
Methods to create the builders remain in the same place as they were before.
- SpanBuilder.setStartTimestamp, Span.end, and Span.addEvent methods which accept a timestamp now accept a timestamp with a TimeUnit instead of requiring a nanos timestamp
   
#### Enhancements:

- Versions of SpanBuilder.setStartTimestamp, Span.end, and Span.addEvent added which accept Instant timestamps
- Setting the value of the `io.opentelemetry.context.contextStorageProvider` System property to `default` will enforce that
the default (thread local) ContextStorage will be used for the Context implementation, regardless of what SPI implementations are
available.

#### Miscellaneous:

- Invalid W3C `TraceState` entries will now be silently dropped, rather than causing the invalidation of the entire `TraceState`.

### SDK

#### Breaking Changes:

- The builder class for the `OpenTelemetrySdk` now strictly requires its components to be SDK implementations. 
You can only build an `OpenTelemetrySdk` with `TracerSdkProvider` and `MeterSdkProvider` instances.

#### Miscellaneous:

- The `SpanProcessor` interface now includes default method implementations for the `shutdown()` and `forceFlush()` methods.
- The BatchRecorder implementation has been updated to actually batch the recordings, rather than simply passing them through.

### Extensions

#### Breaking Changes:

- The `@WithSpan` annotation has been moved to the `io.opentelemetry.extension.annotations` package in the `opentelemetry-extension-annotations` module 

#### Bugfixes:

- The memory pool metrics provided by the MemoryPools class in the `opentelemetry-extension-runtime-metrics` module
have been fixed to properly report the committed memory values.
 
#### Enhancements:

- A new module has been added to assist with propagating the OTel context in kotlin co-routines. 
See the `opentelemetry-extension-kotlin` module for details. 

-----

## Version 0.10.0 - 2010-11-06

### API

#### Enhancements

- The W3C Baggage Propagator is now available.
- The B3 Propagator now handles both single and multi-header formats.
- The B3 Propagator defaults to injecting the single B3 header, rather than the multi-header format.
- Mutating a method on `Span` now returns the `Span` to enable call-chaining.

#### Bug fixes

- The `package-info` file was removed from the `io.otel.context` package because it made the project incompatible with JPMS.

#### Breaking changes

- There have been many updates to the semantic conventions constants. The constants are now auto-generated from the YAML specification files, so the names will now be consistent across languages. For more information, see the [YAML Model for Semantic Conventions](https://github.com/open-telemetry/opentelemetry-specification/tree/master/semantic_conventions).
- All API classes have been moved into the `io.opentelemetry.api.` prefix to support JPMS users.
- The API no longer uses the `grpc-context` as the context implementation. It now uses `io.opentelemetry.context.Context`. This is published in the `opentelemetry-context` artifact. Interactions with the context were mostly moved to static methods in the `Span` and `Baggage` interfaces.
- The Baggage API has been reworked to more closely match the specification. This includes the removal of the `BaggageManager`. Baggage is fully functional within the API, without needing to install an SDK.
- `TracingContextUtils` and `BaggageUtils` were removed from the public API. Instead, use the appropriate static methods on the `Span` and `Baggage` classes, or use methods on the `Context` itself.
- The context propagation APIs have moved into the new `opentelemetry-context` context module.
- `DefaultSpan` was removed from the public API. Instead, use `Span.wrap(spanContext)` if you need a non-functional span that propagates the trace context.
- `DefaultMeter`, `DefaultMeterProvider`, `DefaultTracer` and `DefaultTracerProvider` were removed from the public API. You can access the same functionality with `getDefault()` methods on the `Meter`, `MeterProvider, `Tracer`, and `TracerProvider` classes, respectively.
- Some functionality from the `Tracer` interface is now available either on the `Span` interface or `Context` interface.
- The `OpenTelemetry` class is now an interface, with implementations. Methods on this interface have changed their names to reflect this change. For more information, see [OpenTelemetry.java](/api/src/main/java/io/opentelemetry/api/OpenTelemetry.java).
- All builder-creation methods have been renamed to `.builder()`.
- `StatusCanonicalCode` has been renamed to `StatusCode`.
- `Span.getContext()` has been renamed to `Span.getSpanContext()`.
- `AttributesBuilder` now uses `put` instead of `add` as the method name for adding attributes.
- All parameters are now marked as non-nullable by default.
- `TextMapPropagators` could receive a null carrier passed to the extract method.
- The `TextMapPropagator.Getter` interface has added a method to return the keys that the propagator uses.

### SDK

#### Enhancements

- A new `MetricData` gauge metric type is now available. 
- A new `opentelemetry-sdk-testing` module with a JUnit 5 extension to assist with testing is now available.
- The Prometheus metric exporter now consumes `gauge` metrics.
- The Jaeger gRPC exporter now maps `Resource` entries to process tags.
- The OTLP protobuf definitions were updated to the latest released version: 0.6.0. Both the `Span` and `Metric` exporters were updated to match.
- The `Sampler` interface now allows a `Sampler` implementation to update the `TraceState` that is applied to the `SpanContext` for the resulting span.

#### Breaking changes

- `TraceConfig` configuration option names (environment variables and system properties) were renamed to match the OpenTelemetery Specification. For more information, see [TraceConfig](./QUICKSTART.md#TraceConfig).
- The Jaeger gRPC exporter was updated to match the OpenTelemetry Specification. The `message` log entry attribute has been renamed to `event` and a new `dropped attributes count` attribute was added. For more information, see the [Overview](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md).
- The `SpanData.getHasRemoteParent()` and `SpanData.getHasEnded()` methods were renamed to `hasRemoteParent()` and `hasEnded()`, respectively.
- The `IdsGenerator` interface has been renamed to `IdGenerator`, and all implementations and relevant factory methods were similarly renamed.
- The `RandomIdGenerator` is now accessible via a factory method on the `IdGenerator` class, rather than being exposed itself. Use `IdGenerator.random()` to acquire an instance.
- The OTLP metric exporter now emits `gauge` metrics where appropriate.
- `ValueObserver` instruments now generate gauge metrics, rather than non-monotonic counter-style metrics.
- `ValueObserver` instruments now use the `LastValue` aggregation instead of `MinMaxSumCount`.
- The `SpanData.*` implementation classes were removed from the public SDK, but the interfaces are still available.
- `SpanProcessor.onStart` now takes a `Context` as its first parameter.
- The `Sampler` interface now takes a parent `Context` rather than a `SpanContext`.
- Each `Sampler` has been reorganized into their own classes and moved into the `io.opentelemetry.sdk.trace.samplers` package. Factory methods that used to be on the `Samplers` class were moved to the `Sampler` interface.

### Extensions

#### Enhancements

- A new JUnit5 extension was added for writing tests. For more information, see [OpenTelemetryExtension.java](sdk/testing/src/main/java/io/opentelemetry/sdk/testing/junit5/OpenTelemetryExtension.java). 
- A Jaeger `SpanExporter` which exports via the `thrift-over-http protocol` is now available.
- A Jaeger Propagator is now available.

#### Breaking changes

- The in-memory exporter(s) have been moved to the `opentelemetry-sdk-testing` artifact.
- The OpenTracing shim factory class has been renamed from `TraceShim` to `OpenTracingShim`. The factory methods have changed because `BaggageManager` was removed and non-global `OpenTelemetry` instances are now available.
- The 's' was removed from the word "exporters" for every exporter artifact. For example, `opentelemetry-exporters-logging` was renamed to `opentelemetry-exporter-logging`. 
- The 's' was removed from the word "extensions" for the package for every SDK extension. For example, `io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils` was renamed to `io.opentelemetry.sdk.extension.otproto.TraceProtoUtils`.


### Thanks
Many thanks to everyone who made this release possible!

@anuraaga @bogdandrutu @Oberon00 @thisthat @HaloFour @jkwatson @kenfinnigan @MariusVolkhart @malafeev @trask  @tylerbenson @XiXiaPdx @dengliming @hengyunabc @jarebudev @brianashby-sfx 

## 0.9.1 - 2020-10-07

- API
    - BREAKING CHANGE: SpanId, TraceId and TraceFlags are no longer used as instances, but only contain helper methods for managing conversion between Strings, bytes and other formats. SpanId and TraceId are now natively String-typed, and the TraceFlags is a single byte.
    - BREAKING CHANGE: Propagators now only expose a singleton instance.
    - BREAKING CHANGE: The LabelConsumer and AttributeConsumer are now first-class interfaces, and the underlying consumer interface has had the key made additionally generic. Please prefer using the specific interfaces, rather than the underlying `ReadableKeyValuePairs.KeyValueConsumer`.
    - BREAKING CHANGE: Minimum JDK version has been updated to 8, with Android API level 24.
    - BREAKING CHANGE: Metric Instrument names are now case-insensitive.
    - BREAKING CHANGE: The type-safety on Attributes has been moved to a new AttributeKey, and the AttributeValue wrappers have been removed. This impacts all the semantic attribute definitions, and the various APIs that use Attributes.
    - BREAKING CHANGE: The obsolete HTTP_STATUS_TEXT semantic attribute has been removed.
    - BREAKING CHANGE: The type of the REDIS_DATABASE_INDEX semantic attribute has been changed to be numeric.
    - BREAKING CHANGE: Constant Labels have been removed from metric Instrument definitions.
    - BREAKING CHANGE: The number of available Span Status options has been greatly reduced (from 16 to 3).
    - BREAKING CHANGE: Constant labels have been removed from metric Instrument definitions.
    - BREAKING CHANGE: The only way to specify span parenting is via a parent Context
    - BREAKING CHANGE: The default TextMapPropagator is now a no-op in the API
    - BREAKING CHANGE: CorrelationContext has been renamed to Baggage
    - BREAKING CHANGE: Null-valued span attribute behavior has been changed to being "unspecified".
    - BREAKING CHANGE: Link and Event interfaces have been removed from the API
    - BREAKING CHANGE: The Status object has been removed from the API, in favor of StatusCanonicalCode
    - BUGFIX: the `noParent` option on a Span was being ignored if it was set after setting an explicit parent.
    - BUGFIX: Attributes and Labels now preserve the latest added entry when an existing key has been used.
    - BUGFIX: Updated some of the W3C traceparent validation logic to better match the spec.
    - FaaS semantic attributes have been added
    - Semantic attribute for "exception.escaped" added

- SDK
    - BREAKING CHANGE: The names of the Sampler.Decision enum values, returned by the Sampler interface, have changed.
    - `OpenTelemetrySdk.forceFlush()` now returns a CompletableResultCode
    - BREAKING CHANGE: The `ProbabilitySampler` has been renamed to `TraceIdRatioBased`
    - BREAKING CHANGE: The environment variables/system properties for specifying exporter and span processor configuration have been updated to match the specification.
    - BREAKING CHANGE: Exported zipkin attributes have been changed to match the specification.
    - BREAKING CHANGE: Metric Descriptor attributes have been flattened into the MetricData for export.
    - BREAKING CHANGE: The OpenTelemetrySdk class now returns a TraceSdkManagement interface, rather than the concrete TracerSdkProvider.
    - BUGFIX: Zipkin span durations are now rounded up to 1 microsecond, if less than 1.
    - BUGFIX: The insecure option for OTLP export now does the correct thing. 
    - Added a configuration option for disabling SPI-provided ResourceProviders
    - New incubator module with helper classes for working with SpanData
    - AWS resources now include the `cloud.provider` attribute.

- Extensions
    - BREAKING CHANGE: Propagators now only expose a singleton instance.
    - The auto-config extension has been moved to the instrumentation project.
    - New incubator module with some utilities for mutating SpanData instances.
    - The AWS Resource extension will now pull in EKS Resource attributes.
    - New pre-release extension for handling logging natively.
 
### Thanks    
Many thanks to all who made this release possible:
   
@bogdandrutu @Oberon00 @jkwatson @thisthat @anuraaga @jarebudev @malafeev @quijote @JasonXZLiu @zoercai @eunice98k @dengliming @breedx-nr @iNikem @wangzlei @imavroukakis 

## 0.8.0 - 2020-09-01

- Extensions:
    - Updated metrics generated by the runtime_metrics module to match the proposed semantic conventions.
- API:
    - BREAKING CHANGE: Renamed HttpTextFormat to TextMapPropagator
    - Added a toBuilder method to the Attributes class
    - Added method to create an Attributes Builder from ReadableAttributes
    - Updates to the Attributes' null-handling to conform to the specification
    - TraceState validations were updated to match the W3C specification
    - recordException Span API now has an additional overload to support additional attributes
- SDK:
    - BUGFIX: Bound instruments with no recordings no longer generate data points.
    - BREAKING CHANGE: The Exporter interfaces have changed to be async-friendly.
    - BREAKING CHANGE: The parent context passed to the Sampler will no longer be nullable, but instead an invalid context will be passed.
    - BREAKING CHANGE: The SpanProcessor now takes a ReadWriteSpan for the onStart method
    - BREAKING CHANGE: ResourceConstants changed to ResourceAttributes
    - BREAKING CHANGE: ParentOrElse Sampler changed to be called ParentBased
    - Default Resource include the SDK attributes
    - ResourceProvider SPI to enable custom Resource providers
    - The individual pieces of the SDK are not published as individual components, in addition to the whole SDK artifact.
    - Zipkin and Jaeger exporters now include the InstrumentationLibraryInfo attributes.
    - The OTLP protobufs were updated to version 0.5.0 and the OTLP exporters were updated accordingly.

- Many thanks for contributions from @anuraaga, @dengliming, @iNikem, @huntc, @jarebudev, @MitchellDumovic, @wtyanan, @williamhu99, @Oberon00, @thisthat, @malafeev, @mateuszrzeszutek, @kenfinnigan


## 0.7.1 - 2020-08-14

- BUGFIX: OTLP Span Exporter: fix splitting metadata key-value substring with more than one '=' sign

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
