# Changelog

## Unreleased:

### General

Note: In an effort to accelerate our work toward a 1.0.0 release, we have skipped the deprecation phase
on a number of breaking changes. We apologize for the inconvenience this may have caused. We are very
aware that these changes will impact users. If you need assistance in migrating from previous releases,
please open a [discussion topic](https://github.com/opentelemetry/opentelemetry-java/discussions) at 
[https://github.com/opentelemetry/opentelemetry-java/discussions](https://github.com/opentelemetry/opentelemetry-java/discussions).

#### Breaking Changes

- Methods and classes deprecated in 0.15.0 have been removed.

### API

#### Breaking Changes

- The `Span.Kind` enum has been moved to the top level, and named `SpanKind`.
- `DefaultOpenTelemetry` is no longer a public class. If you need the functionality previously
  provided by this implementation, it can be accessed via new static methods on the `OpenTelemetry`
  interface itself.
- The `Labels` interface and related classes have been moved into the alpha metrics modules and
  repackaged.
- `BaggageEntry.getEntryMetadata()` has been renamed to `BaggageEntry.getMetadata()`
- `BaggageConsumer` has been removed in favor of a standard 
  `java.util.function.BiConsumer<String, BaggageEntry>`
- The `TraceFlags` interface has been re-introduced. This is now used, rather than a bare `byte`
  wherever trace flags is used. In particular, `SpanContext.create()`,
  `SpanContext.createFromRemoteParent()` now require a `TraceFlags` instance,
  and `SpanContext.getTraceFlags()` returns a `TraceFlags` instance.
- The names of static methods on `TraceFlags` have been normalized to match other similar classes,
  and now return `TraceFlags` instead of `byte` where appropriate.
- `TraceId.copyHexInto(byte[] traceId, char[] dest, int destOffset)` has been removed.
- `SpanContext.getTraceIdAsHexString()` has been renamed to `SpanContext.getTraceIdHex()`
- `SpanContext.getSpanIdAsHexString()` has been renamed to `SpanContext.getSpanIdHex()`
- `TraceFlags.isSampledFromHex(CharSequence src, int srcOffset)` has been removed.
- `SpanId` and `TraceId` methods that had a `String` parameter now accept `CharSequence`
  and assume the id starts at the beginning.
- `SpanId.getSize()` and `TraceId.getSize()` have been removed.
- `SpanId.getHexLength()` has been renamed to `SpanId.getLength()`
- `SpanId.bytesFromHex()` has been removed
- `SpanId.bytesToHex()` has been removed
- `TraceId.getHexLength()` has been renamed to `TraceId.getLength()`
- `TraceId.bytesFromHex()` has been removed
- `TraceId.bytesToHex()` has been removed
- `TraceId.traceIdLowBytesAsLong()` has been removed
- `TraceId.traceIdHighBytesAsLong()` has been removed

#### Enhancements

- The `W3CTraceContextPropagator` class now directly implements the `TextMapPropagator` interface.
- The `OpenTelemetry` interface now has a `getDefault()` method which will return a completely no-op implementation.
- The `OpenTelmmetry` interface now has a `getPropagating(ContextPropagators propagators)` method which will
return an implementation that contains propagators, but is otherwise no-op. 

#### Misc Notes

- The internal `StringUtils` class has had metrics-related methods removed from it. But, you weren't using
internal classes, were you?

### SDK

#### Breaking Changes

- `TraceConfig` has been renamed to `SpanLimits` and relocated to the `io.opentelemetry.sdk.tracing` package.
All related method names have been renamed to match.

#### Enhancements

- `SpanData` now directly exposes the underlying `SpanContext` instance.

### SDK Extensions

#### Breaking Changes

- In the `opentelemetry-autoconfigure` module, three environment variables/system properties 
have been renamed to match the spec:
  * `OTEL_TRACE_EXPORTER`/`otel.trace.exporter` has been replaced with `OTEL_TRACES_EXPORTER`/`otel.traces.exporter`
  * `OTEL_TRACE_SAMPLER`/`otel.trace.sampler` has been replaced with `OTEL_TRACES_SAMPLER`/`otel_traces_sampler`
  * `OTEL_TRACE_SAMPLER_ARG`/`otel.trace.sampler.arg` has been replaced with `OTEL_TRACES_SAMPLER_ARG`/`otel.traces.sampler.arg` 

#### Enhancements

- The `opentelemetry-autoconfigure` module now supports using non-millisecond values for duration & 
interval configuration options. See the javadoc on the `io.opentelemetry.sdk.autoconfigure.ConfigProperties.getDuration(String)` 
method for details on supported formats.
- The `opentelemetry-autoconfigure` module now provides automatic SPI-based parsing of the `OTEL_RESOURCE_ATTRIBUTES` env var
(and the corresponding `otel.resource.attributes` system property). If you include this module on your
classpath, it will automatically update the `Resource.getDefault()` instance with that configuration.

### Metrics (alpha)

#### API

- The `Labels` interface has been moved into the metrics API module and repackaged into the 
`io.opentelemetry.api.metrics.common` package.

## Version 0.15.0 - 2021-01-29

### General

#### Breaking Changes

- Methods and classes deprecated in 0.14.x have been removed.

### Semantic Conventions

The `opentelemetry-semconv` module has been marked as `-alpha` and removed from the bom. This was done because the OpenTelemetry
project has not decided on a specification for stability of semantic conventions or the specific telemetry produced by
instrumentation.

#### Deprecations

- The items in the `io.opentelemetry.semconv.trace.attributes.SemanticAttributes` which were previously
generated form the Resource semantic conventions have been deprecated. Please use the ones in the new
`io.opentelemetry.semconv.resource.attributes.ResourceAttributes` class.

#### Enhancements

- A new `io.opentelemetry.semconv.resource.attributes.ResourceAttributes` has been introduced to hold the 
generated semantic attributes to be used in creating `Resource`s. 

### SDK

#### Breaking Changes

- `SamplingResult.Decision` has been removed in favor of the `io.opentelemetry.sdk.trace.samplers.SamplingDecision` top-level class.
- `Resource.merge(Resource)` now will resolve conflicts by preferring the `Resource` passed in, rather than the original.
- The default Resource (accessible via `Resource.getDefault()`) now includes a fallback `service.name` attribute. The implication
of this is that exporters that have configured fallback service names will only use them if the SDK is intentionally 
configured with a Resource that does not utilize the default Resource for its underlying Resource data.
- The `Sampler` is now specified when building the SdkTracerProvider directly, rather than being a part of the TraceConfig. 

#### Bugfixes

- The Jaeger exporters will now properly populate the process service name from the Resource service.name attribute. 

#### Deprecations

- Going forward, OTLP exporter endpoint specifications must include a scheme, either `http://` or `https://`.
We will support endpoints without schemes until the next release, at which point not providing a scheme will generate
an error when trying to use them. This applies to the use of system properties, environment variables, or programmatic
specifications of the endpoints.
- The `exportOnlySampled` configuration of the `BatchSpanProcessor` has been deprecated and will be removed in the next
release.
- The `io.opentelemetry.sdk.resources.ResourceAttributes` has been deprecated and will be removed in the next release.
Please use the new `io.opentelemetry.semconv.resource.attributes.ResourceAttributes` class in the `opentelemetry-semconv`
module.
- The serviceName configuration option for the Jaeger and Zipkin exporters has been deprecated. In the next release, those
configuration options will be removed, and the fallback `service.name` will always be pulled from the default Resource.
  
#### Enhancements

- `Resource.getDefault()` now includes a fallback `service.name` attribute. Exporters that require a `service.name`
should acquire the fallback from the default resource, rather than having it configured in.
  
### SDK Extensions

#### Breaking Changes

- The `otel.bsp.schedule.delay.millis` env var/system property configuration option for the batch span processor has been renamed to 
`otel.bsp.schedule.delay` to match the specification.
- The `otel.bsp.export.timeout.millis` env var/system property configuration option for the batch span processor has been renamed to 
`otel.bsp.export.timeout` to match the specification.

#### Enhancements

- The `opentelemetry-sdk-extension-autoconfigure` module will now additionally register the auto-configured
SDK as the instance of `GlobalOpenTelemetry` when used.
- The `opentelemetry-sdk-extension-autoconfigure` module now supports the `otel.exporter.otlp.certificate` configuration
property for specifying a path to a trusted certificate for the OTLP exporters.

---

## Version 0.14.1 - 2021-01-14

### General

- Several more modules have been updated to have `-alpha` appended on their versions:
    - `opentelemetry-sdk-extension-jfr-events`
    - `opentelemetry-sdk-extension-async-processor`
    - `opentelemetry-sdk-extension-logging`
    - `opentelemetry-sdk-extension-zpages`
    - `opentelemetry-sdk-exporter-prometheus`
    - `opentelemetry-sdk-exporter-tracing-incubator`
    - `opentelemetry-opentracing-shim`
    - `opentelemetry-opencensus-shim`

### API

#### Breaking Changes

- Code that was deprecated in `0.13.0` has been removed from the project.
    - Metrics interfaces are no longer available as a part of the `opentelemetry-pom` or from the `opentelemetry-api` modules. 
      To access the alpha metrics APIs, you will need to explicitly add them as a dependency.
    - `OpenTelemetry.setPropagators()` has been removed.  You should instead create your
      `OpenTelemetry` implementations with the Propagators preset, via the various builder options. For example, use
      `DefaultOpenTelemetry.builder().setPropagators(propagators).build()` to configure your no-sdk implementation.
    - The `OpenTelemetry.builder()` and the `OpenTelemetryBuilder` interface have been removed.
      The builder functionality is now only present on individual implementations of OpenTelemetry. For instance, the
      `DefaultOpenTelemetry` class has a builder available.
      
#### Deprecations

- The SemanticAttributes class has been moved to a new module: `opentelemetry-semconv` and repackaged into a new package:
`io.opentelemetry.semconv.trace.attributes`. The old `SemanticAttributes` class will be removed in the next release.
- The SPI interfaces for OpenTelemetry have been deprecated. We are moving to a new auto-configuration approach with the 
new SDK auto-configuration module: `io.opentelemetry.sdk.autoconfigure`. This module should be considered the officially
supported auto-configuration library moving forward.

#### Enhancements

- The SemanticAttributes have been updated to the latest version of the specification, as of January 7th, 2021.

### SDK

#### Bugfixes

- Environment variables/system properties that are used to set extra headers for the OTLP exporters have been fixed to now 
split on commas, rather than semicolons. This has been brought in line with the specification for these environment
variables. This includes `otel.exporter.otlp.span.headers`, `otel.exporter.otlp.metric.headers`, and `otel.exporter.otlp.headers`.
- Passing a null span name when creating a span will no longer cause a NullPointerException. Instead, a default span name will be 
provided in place of the missing name.

#### Breaking Changes

- The deprecated `SpanData.Link.getContext()` method has been removed in favor of `SpanData.Link.getSpanContext()`.
- The `TracerProviderFactorySdk` SPI class has been renamed to `SdkTracerProviderFactory`.
- The `OpenTelemetrySdkBuilder.build()` method has been renamed to `OpenTelemetrySdkBuilder.buildAndRegisterGlobal()`. 
The `build()` method still exists, but no longer sets the instance on the `GlobalOpenTelemetry` when invoked.
- The `SdkTracerManagement.shutdown()` method now returns `CompletableResultCode` which can be used to wait 
asynchronously for shutdown to complete.
- The `sampling.probability` sampling attribute previously generated by the `TraceIdRatioBasedSampler` is no longer
generated, as it was not conformant with the specifications.
- The `SpanData` inner classes have been moved to the top level, so `SpanData.Link` -> `LinkData`, `SpanData.Event` -> `EventData`
and `SpanData.Status` -> `StatusData`.

#### Deprecations

- `SdkTracerProvider.updateActiveTraceConfig()` and `SdkTracerProvider.addSpanProcessor()` have been deprecated. The methods
will be removed in the next release.
- All existing auto-configuration mechanisms have been deprecated in favor of using the new `io.opentelemetry.sdk.autoconfigure`
module. The existing ones will be removed in the next release.
- The methods with the term "deadline" has been deprecated in the configuration of the grpc-based exporters (OTLP and Jaeger) in favor
  of the word "timeout". The deadline-named methods will be removed in the next release.
- The `StringUtils` class in the `opentelemetry-extension-trace-propagators` extension module has been deprecated 
and will be made non-public in the next release.
- The `StatusData.isUnset()` and `StatusData.isOk()` methods have been deprecated. They will be removed in the next release.

#### Enhancements

- The `OtlpGrpcSpanExporter` now supports setting trusted TLS certificates for secure communication with the collector.
- A new module for supporting auto-configuration of the SDK has been added. The new module, `io.opentelemetry.sdk.autoconfigure` will
be the new path for auto-configuration of the SDK, including via SPI, environment variables and system properties.
- The `TraceConfig` class now exposes a `builder()` method directly, so you don't need to get the default then call `toBuilder()` on it.
- The OTLP protobuf definitions were updated to the latest released version: `0.7.0`. 
Both the `Span` and (alpha) `Metric` exporters were updated to match.
- Timeouts in the exporters can now be specified with `java.util.concurrent.TimeUnit` and `java.time.Duration` based configurations,
rather than requiring milliseconds.

### SDK Extensions

#### Breaking Changes

- The ZPages extension now exposes its SpanProcessor implementation. To use it, you will need to add it to your
SDK implementation directly, rather than it adding itself to the global SDK instance.
- The JaegerRemoteSampler builder patterns have been changed and updated to more closely match the rest
of the builders in the project.

#### Deprecations
- The `AwsXrayIdGenerator` constructor has been deprecated in favor of using a simple `getInstance()` singleton, since
it has no state.
- The `TraceProtoUtils` class in the `opentelemetry-sdk-extension-otproto` module has been deprecated and 
will be removed in the next release.
  
#### Bugfixes

- The JaegerRemoteSampler now uses the ParentBased sampler as the basis for any sampling that is done.

### Metrics (alpha)

#### SDK:

- The `InstrumentSelector.newBuilder()` method has been renamed to `InstrumentSelector.builder()` and
the methods on the Builder have changed to use the same naming patterns as the rest of the project.
- The `MeterProviderFactorySdk` class has been renamed to `SdkMeterProviderFactory`.
- The `SdkMeterProvicer.Builder` has been moved to the top level `SdkMeterProviderBuilder`.
- The `InstrumentSelector` now requires an instrument type to be provided, and defaults the name regex to `.*`.

-----

## Version 0.13.0 - 2020-12-17

### General

- Starting with 0.13.0, all unstable modules (the 2 metrics modules for now) will have a `-alpha` appended to their
  base version numbers to make it clear they are not production ready, and will not be when we get to releasing 1.0. 
  See our [Rationale](docs/rationale.md) document for details.

### API

#### Breaking Changes

- The `Labels.ArrayBackedLabelsBuilder` class has been made non-public. 
You can still access the `LabelsBuilder` functionality via the `Labels.builder()` method.
- Methods deprecated in the 0.12.0 release have been removed or made non-public:
    - The `HttpTraceContext` class has been removed.
    - The `toBuilder()` method on the OpenTelemetry interface has been removed.
    - The `Attributes.builder(Attributes)` method has been removed in favor of `Attributes.toBuilder(Attributes)`.
    - The `DefaultContextPropagators` class has made non-public.
    - The `TraceMultiPropagator` builder has been removed in favor of a simple factory method. 
    - The `value()` method on the `StatusCode` enum has been removed.
    - The Baggage `EntryMetadata` class has been removed in favor of the `BaggageEntryMetadata` interface.
    - The `setCallback()` method on the asynchronous metric instruments has been removed.
- Several public classes have been made `final`.

#### Enhancements

- An `asMap` method has been added to the `Labels` interface, to expose them as a `java.util.Map`.
- You can now enable strict Context verification via a system property (`-Dio.opentelemetry.context.enableStrictContext=true`)
  Enabling this mode will make sure that all `Scope`s that are created are closed, and generate log messages if they 
  are not closed before being garbage collected. This mode of operation is CPU intensive, so be careful before
  enabling it in high-throughput environments that do not need this strict verification. See the javadoc on the 
`io.opentelemetry.context.Context` interface for details.
- Several of the methods on the `Span` interface have been given default implementations.
- The Semantic Attributes constants have been updated to the version in the yaml spec as of Dec 14, 2020.

#### Miscellaneous

- The Metrics API has been deprecated in the `opentelemetry-api` module, in preparation for releasing a fully-stable 1.0
  version of that module. The Metrics API will be removed from the module in the next release.
- The API has been broken into separate modules, in preparation for the 1.0 release of the tracing API.
  If you depend on the `opentelemetry-api` module, you should get the rest of the API modules as transitive dependencies.
- The `OpenTelemetry.builder()` and the `OpenTelemetryBuilder` interface have been deprecated and will be removed in the next release.
  The builder functionality is now only present on individual implementations of OpenTelemetry. For instance, the
  `DefaultOpenTelemetry` class has a builder available.
- The `OpenTelemetry.setPropagators()` has been deprecated and will be removed in the next release. You should instead create your
  `OpenTelemetry` implementations with the Propagators preset, via the various builder options. For example, use 
  `DefaultOpenTelemetry.builder().setPropagators(propagators).build()` to configure your no-sdk implementation.

### SDK

#### Miscellaneous

- The `SpanData.Link.getContext()` method has been deprecated in favor of a new `SpanData.Link.getSpanContext()`. 
  The deprecated method will be removed in the next release of the SDK.
- The internals of the (alpha) Metrics SDK have been significantly updated.
- OTLP adapter classes have been moved into the `opentelemetry-sdk-extension-otproto` module so they can be shared across OTLP usages.
- The zipkin exporter has been updated to have its error code handling match the spec.
- The logging exporter's format has changed to something slightly more human-readable.

#### Breaking Changes

- Many SDK classes have been renamed to be prefixed with `Sdk` rather than having `Sdk` being embedded in the middle of the name.
  For example, `TracerSdk` has been renamed to `SdkTracer` and `TracerSdkManagement` has been renamed to `SdkTracerManagement`.
- The `ResourcesConfig.builder()` method has been made non-public.
- The `TraceConfig.Builder` class has been moved to the top-level `TraceConfigBuilder` class.
- The built-in exporter `Builder` classes have been moved to the top level, rather than inner classes. Access to the builders
  is still available via `builder()` methods on the exporter classes.
- The built-in SpanProcessor `Builder` classes have been moved to the top level, rather than inner classes. Access to the builders
  is still available via `builder()` methods on the SpanProcessor implementation classes.
- The built-in ParentBasedSampler `Builder` class has been moved to the top level, rather than inner classes. Access to the builder
  is still available via methods on the Sampler interface.
- The `DaemonThreadFactory` class has been moved to an internal module and should not be used outside of this repository.
- The builder class for the `OpenTelemetrySdk` class has been slimmed down. The configurable details have been moved into 
  the specific provider builders, where they apply more specifically and obviously.
- Many public classes have been made `final`.
- The MetricExporter interface's `shutdown()` method now returns `CompletableResultCode` rather than void.
- The `OpenTelemetrySdk`'s builder class has been moved to the top level, rather than being an inner class. It has been renamed to 
  `OpenTelemetrySdkBuilder` as a part of that change.
- The OTLP exporters have been split into two separate modules, and the metrics exporter has been tagged with the `-alpha` version.
  If you continue to depend on the `opentelemetry-exporters-otlp` module, you will only get the trace exporter as a transitive dependency.

### Extensions

#### Bugfixes

- The `opentelemetry-extension-annotations` module now includes the api module as an `api` dependency, rather than just `implementation`.

#### Breaking Changes

- The deprecated `opentelemetry-extension-runtime-metrics` module has been removed. The functionality is available in the 
  opentelemetry-java-instrumentation project under a different module name.
- The deprecated `trace-utils` module has been removed.
- Several public classes have been made `final`.

#### Enhancements

- Some common OTLP adapter utilities have been moved into the `opentelemetry-sdk-extension-otproto` module so they can 
  be shared across OTLP exporters.

-----

## Version 0.12.0 - 2020-12-04

### API

#### Bugfixes

- Usages of tracers and meters on all `OpenTelemetry` instances were being delegated to the global Meter and Tracer.
This has been corrected, and all instances should have independent Tracer and Meter instances.

#### Breaking Changes

- The `AttributesBuilder` no long accepts null values for array-valued attributes with numeric or boolean types.
- The `TextMapPropagator.fields()` method now returns a `Collection` rather than a `List`.
- `Labels` has been converted to an interface, from an abstract class. Its API has otherwise remained the same.
- `TraceState` has been converted to an interface, from an abstract class. Its API has otherwise remained the same.
- `Attributes` has been converted to an interface, from an abstract class. Its API has otherwise remained the same.
- The `ReadableAttributes` interface has been removed, as it was redundant with the `Attributes` interface. All APIs that
used or returned `ReadableAttributes` should accept or return standard `Attributes` implementations.
- `SpanContext` has been converted to an interface, from an abstract class. Its API has otherwise remained the same.
- The functional `AttributeConsumer` interface has been removed and replaced with a standard `java.util.function.BiConsumer`.
- The signature of the `BaggageBuilder.put(String, String, EntryMetadata entryMetadata)` 
method has been changed to `put(String, String, BaggageEntryMetadata)`

#### Enhancements

- A `builder()` method has been added to the OpenTelemetry interface to facilitate constructing implementations.
- An `asMap()` method has been added to the `Attributes` interface to enable conversion to a standard `java.util.Map`.
- An `asMap()` method has been added to the `Baggage` interface to enable conversion to a standard `java.util.Map`.
- An `asMap()` method has been added to the `TraceState` interface to enable conversion to a standard `java.util.Map`.
- The Semantic Attributes constants have been updated to the version in the yaml spec as of Dec 1, 2020.

#### Miscellaneous

- The `HttpTraceContext` class has been deprecated in favor of `W3CTraceContextPropagator`. `HttpTraceContext` will be removed in 0.13.0.
- The `toBuilder()` method on the OpenTelemetry interface has been deprecated and will be removed in 0.13.0.
- The `DefaultContextPropagators` class has been deprecated. Access to it will be removed in 0.13.0.
- The `TraceMultiPropagator` builder has been deprecated in favor of a simple factory method. The builder will be removed in 0.13.0.
You can access the same functionality via static methods on the `ContextPropagators` interface. 
- The `setCallback()` method on the asynchronous metric instruments has been deprecated and will be removed in 0.13.0. 
Instead, use the `setCallback()` method on the builder for the instruments.
- The `value()` method on the `StatusCode` enum has been deprecated and will be removed in 0.13.0.
- The Baggage `EntryMetadata` class has been deprecated in favor of the `BaggageEntryMetadata` interface. The class will be made non-public in 0.13.0.

### Extensions

- The `opentelemetry-extension-runtime-metrics` module has been deprecated. The functionality is available in the 
opentelemetry-java-instrumentation project under a different module name. The module here will be removed in 0.13.0.
- The `trace-utils` module has been deprecated. If you need this module, please let us know! The module will be removed in 0.13.0.
 
### SDK

#### Breaking Changes

- The `opentelemetry-sdk-tracing` module has been renamed to `opentelemetry-sdk-trace`.
- The default port the OTLP exporters use has been changed to `4317`.
- The deprecated `SpanData.getCanonicalCode()` method has been removed, along with the implementations.

#### Enhancements

- The OpenTelemetrySdk builder now supports the addition of `SpanProcessor`s to the resulting SDK.
- The OpenTelemetrySdk builder now supports the assignment of an `IdGenerator` to the resulting SDK.
- The `ReadableSpan` interface now exposes the `Span.Kind` of the span.
- The SDK no longer depends on the guava library.
- The parent SpanContext is now exposed on the `SpanData` interface.

#### Miscellaneous

- The `toBuilder()` method on the OpenTelemetrySdk class has been deprecated and will be removed in 0.13.0.
- The MultiSpanProcessor and MultiSpanExporter have been deprecated. You can access the same functionality via
the `SpanProcessor.composite` and `SpanExporter.composite` methods. The classes will be made non-public in 0.13.0.
- The `SpanData.hasRemoteParent()` method has been deprecated and will be removed in 0.13.0. If you need this information,
you can now call `SpanData.getParentSpanContext().isRemote()`.
- The default timeouts for the 2 OTLP exporters and the Jaeger exporter have been changed to 10s from 1s.

### Extensions

#### Breaking Changes

- The `opentelemetry-sdk-extension-aws-v1-support` module has been renamed to `opentelemetry-sdk-extension-aws` 
and the classes in it have been repackaged into the `io.opentelemetry.sdk.extension.aws.*` packages.

#### Bugfixes:

- The OpenTracing `TracerShim` now properly handles keys for context extraction in a case-insensitive manner.

#### Enhancements

- The `opentelemetry-sdk-extension-resources` now includes resource attributes for the process runtime via the `ProcessRuntimeResource` class.
This is included in the Resource SPI implementation that the module provides.
- The `opentelemetry-sdk-extension-aws` extension now will auto-detect AWS Lambda resource attributes.

-----

## Version 0.11.0 - 2020-11-18

### API

#### Breaking changes:

- The SPI interfaces have moved to a package (not a module) separate from the API packages, and now live in `io.opentelemetry.spi.*` package namespace.
- Builder classes have been moved to the top level, rather than being inner classes. 
For example, rather than `io.opentelemetry.api.trace.Span.Builder`, the builder is now in its own top-level class: `io.opentelemetry.api.trace.SpanBuilder`.
Methods to create the builders remain in the same place as they were before.
- SpanBuilder.setStartTimestamp, Span.end, and Span.addEvent methods which accept a timestamp now accept a timestamp with a TimeUnit instead of requiring a nanos timestamp.

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

#### Enhancements:

- An API has been added to the SDK's MeterProvider implementation (`MeterSdkProvider`) that allows the end-user to configure
how various metrics will be aggregated. This API should be considered a precursor to a full "Views" API, and will most likely
evolve over the coming months before the metrics implementation is complete. See the javadoc for `MeterSdkProvider.registerView()` for details.

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

## Version 0.10.0 - 2020-11-06

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
