# Changelog

## Unreleased

## Version 1.55.0 (2025-10-13)

### API

#### Common

* Improve GraalVM native image compatibility
  ([#7160](https://github.com/open-telemetry/opentelemetry-java/pull/7160))

#### Traces

* Fix `TraceState` key validation limits to match W3C specification
  ([#7575](https://github.com/open-telemetry/opentelemetry-java/pull/7575))

#### Incubator

* Add `ExtendedOpenTelemetry` API
  ([#7496](https://github.com/open-telemetry/opentelemetry-java/pull/7496))
* Add incubator implementation of composite sampling specification
  ([#7626](https://github.com/open-telemetry/opentelemetry-java/pull/7626))

### SDK

#### Traces

* Proactively avoid `Unsafe` on Java 23+ to avoid triggering JVM warning message
  ([#7691](https://github.com/open-telemetry/opentelemetry-java/pull/7691))

#### Metrics

* Add `setMeterConfigurator()` support to `MeterProvider` (incubating API)
  ([#7346](https://github.com/open-telemetry/opentelemetry-java/pull/7346))

#### Exporters

* OTLP: Configure metric exporter to use SDK's `MeterProvider` for internal metrics
  ([#7541](https://github.com/open-telemetry/opentelemetry-java/pull/7541))
* OTLP: Suppress logging of `InterruptedException` from managed OkHttp threads
  ([#7565](https://github.com/open-telemetry/opentelemetry-java/pull/7565))
* OTLP: Update dependency from `okhttp-jvm` back to `okhttp` for Gradle users,
  preserving `okhttp-jvm` for Maven users
  ([#7681](https://github.com/open-telemetry/opentelemetry-java/pull/7681))
* Prometheus: Remove separate `otel_scope_info` metric and always add scope labels to data points
  ([#7398](https://github.com/open-telemetry/opentelemetry-java/pull/7398))
* Prometheus: Update exporter dependencies to use protobuf-free formats
  ([#7664](https://github.com/open-telemetry/opentelemetry-java/pull/7664))

#### Profiling

* Update profiles exporter to support proto v1.8.0-alpha changes
  ([#7638](https://github.com/open-telemetry/opentelemetry-java/pull/7638))
* Add abstractions to assist with dictionary table assembly
  ([#7717](https://github.com/open-telemetry/opentelemetry-java/pull/7717))
* Add abstractions to assist with sample composition
  ([#7727](https://github.com/open-telemetry/opentelemetry-java/pull/7727))

#### Extensions

* Autoconfigure: Improve exception logging when running in Maven
  ([#7336](https://github.com/open-telemetry/opentelemetry-java/pull/7336))
* Declarative configuration: Return `Resource`
  ([#7639](https://github.com/open-telemetry/opentelemetry-java/pull/7639))
* Declarative configuration: Invoke auto-configure listeners
  ([#7654](https://github.com/open-telemetry/opentelemetry-java/pull/7654))
* Declarative configuration: Add logging when incompatible types are found
  ([#7693](https://github.com/open-telemetry/opentelemetry-java/pull/7693))

### Shims

#### OpenTracing Shim

* Improve log levels in error scenarios
  ([#6832](https://github.com/open-telemetry/opentelemetry-java/pull/6832))

### Project tooling

* Respect `testJavaVersion` property when running JMH benchmarks
  ([#7697](https://github.com/open-telemetry/opentelemetry-java/pull/7697))

## Version 1.54.1 (2025-09-18)

### SDK

#### Exporters

* Prometheus: Fix regression in protobuf format export
  ([#7664](https://github.com/open-telemetry/opentelemetry-java/pull/7664))

## Version 1.54.0 (2025-09-05)

### API

#### Baggage

* Fix guard against ArrayIndexOutOfBoundsException in BaggageCodec
  ([#7239](https://github.com/open-telemetry/opentelemetry-java/pull/7239))

### SDK

#### Metrics

* Fix MetricData.getDoubleSumData() ClassCastException with custom implementations
  ([#7597](https://github.com/open-telemetry/opentelemetry-java/pull/7597))

#### Exporters

* Fix HttpExporterBuilder.copy() and GrpcExporterBuilder.copy() to preserve component loader
  ([#7596](https://github.com/open-telemetry/opentelemetry-java/pull/7596))

#### Extensions

* Autoconfigure: Remove support for old EnvironmentResourceProvider package name
  ([#7622](https://github.com/open-telemetry/opentelemetry-java/pull/7622))
* Declarative config: Add DeclarativeConfigurationProvider SPI
  ([#7472](https://github.com/open-telemetry/opentelemetry-java/pull/7472))
* Declarative config: Pass meter provider to avoid using the global OpenTelemetry instance
  ([#7475](https://github.com/open-telemetry/opentelemetry-java/pull/7475))

### Project tooling

* Update to Gradle v9
  ([#7590](https://github.com/open-telemetry/opentelemetry-java/pull/7590))

## Version 1.53.0 (2025-08-08)

### SDK

* File based config will log the wrong file format, when applicable.
  ([#7498](https://github.com/open-telemetry/opentelemetry-java/pull/7498))

#### Exporters

* Change `okhttp` dependency to `okhttp-jvm`, which fixes missing class failures in
  transitive dependencies.
  ([#7517](https://github.com/open-telemetry/opentelemetry-java/pull/7517))

## Version 1.52.0 (2025-07-11)

### API

#### Common

* Promote `ComponentLoader` to new `opentelemetry-common` artifact,
  standardize SPI loading
  ([#7446](https://github.com/open-telemetry/opentelemetry-java/pull/7446))

#### Context

* LazyStorage passes its ClassLoader when loading ContextStorageProvider SPI
  ([#7424](https://github.com/open-telemetry/opentelemetry-java/pull/7424))

#### Incubator

* Add context and severity params to ExtendedLogger#isEnabled
  ([#7268](https://github.com/open-telemetry/opentelemetry-java/pull/7268))
* Add new convenience methods for converting DeclarativeConfigProperties to config model
  ([#7453](https://github.com/open-telemetry/opentelemetry-java/pull/7453))

### SDK

* Add custom stacktrace renderer which is length limit aware
  ([#7281](https://github.com/open-telemetry/opentelemetry-java/pull/7281))

#### Metrics

* Propagate flush to PeriodicMetricReader's metricExporter.
  ([#7410](https://github.com/open-telemetry/opentelemetry-java/pull/7410))

#### Exporters

* OTLP - JdkHttpSender: ensure proper closure of HttpClient in shutdown method
  ([#7390](https://github.com/open-telemetry/opentelemetry-java/pull/7390))
* OTLP: profile exporters fix and test improvements
  ([#7442](https://github.com/open-telemetry/opentelemetry-java/pull/7442))
* OTLP: Loading Compressor SPI via ComponentLoader configured through setComponentLoader
  ([#7428](https://github.com/open-telemetry/opentelemetry-java/pull/7428))
* Prometheus: add scope schema URL and attributes
  ([#7356](https://github.com/open-telemetry/opentelemetry-java/pull/7356))
* Prometheus: extend prometheus declarative config support to include without_scope_info,
  with_resource_constant_labels
  ([#6840](https://github.com/open-telemetry/opentelemetry-java/pull/6840))

#### Extensions

* Autoconfigure: fix race condition of `GlobalOpenTelemetry` initialization with
  `AutoConfiguredOpenTelemetrySdkBuilder`
  ([#7365](https://github.com/open-telemetry/opentelemetry-java/pull/7365))
* Declarative config: update to declarative config 1.0-rc.1
  ([#7436](https://github.com/open-telemetry/opentelemetry-java/pull/7436))
* Declarative config: resolve environment variable substitution for mixed quotes
  ([#7433](https://github.com/open-telemetry/opentelemetry-java/pull/7433))

## Version 1.51.0 (2025-06-06)

### API

#### Context

* Fix context storage provider property name in log message
  ([#7342](https://github.com/open-telemetry/opentelemetry-java/pull/7342))

### SDK

* Experimental configurable exception.* attribute resolution for SdkTracerProvider,
  SdkLoggerProvider
  ([#7266](https://github.com/open-telemetry/opentelemetry-java/pull/7266))

#### Exporters

* All exporters: implement new SemConv exporter health metrics, with configuration API for selecting
  schema version
  ([#7265](https://github.com/open-telemetry/opentelemetry-java/pull/7265))
* OTLP: Add gRPC export for profiles signal type.
  ([#7301](https://github.com/open-telemetry/opentelemetry-java/pull/7301))
* OTLP: Run JDK HTTP sender on non-daemon threads.
  ([#7322](https://github.com/open-telemetry/opentelemetry-java/pull/7322))
* Prometheus: fix serialization of arrays
  ([#7291](https://github.com/open-telemetry/opentelemetry-java/pull/7291))
* OTLP: exporter tolerates instances of LogRecordData when incubator is present
  ([#7393](https://github.com/open-telemetry/opentelemetry-java/pull/7393))

#### Extensions

* Declarative config: Handle instrumentation node changes in yaml config file format 0.4
  ([#7357](https://github.com/open-telemetry/opentelemetry-java/pull/7357))

## Version 1.50.0 (2025-05-09)

### API

* Clarify that AttributesBuilder.put allows nulls
  ([#7271](https://github.com/open-telemetry/opentelemetry-java/pull/7271))
* Stabilize log record event name
  ([#7277](https://github.com/open-telemetry/opentelemetry-java/pull/7277))

#### Context

* Fix duplicated ExecutorService wrap
  ([#7245](https://github.com/open-telemetry/opentelemetry-java/pull/7245))
* Promote getAll to TextMapGetter stable API
  ([#7267](https://github.com/open-telemetry/opentelemetry-java/pull/7267))

#### Incubator

* Add ExtendedLogRecordBuilder#setException
  ([#7182](https://github.com/open-telemetry/opentelemetry-java/pull/7182))
* Add experimental support for log extended attributes
  ([#7123](https://github.com/open-telemetry/opentelemetry-java/pull/7123))

### SDK

* Remove Java9VersionSpecific clock implementation
  ([#7221](https://github.com/open-telemetry/opentelemetry-java/pull/7221))
* Add addProcessorFirst to SdkTracerProviderBuilder, SdkLoggerProviderBuilder
  ([#7243](https://github.com/open-telemetry/opentelemetry-java/pull/7243))

#### Logs

* Add `setLoggerConfigurator` support to `LoggerProvider`
  ([#7332](https://github.com/open-telemetry/opentelemetry-java/pull/7332))

#### Metrics

* Add DelegatingMetricData
  ([#7229](https://github.com/open-telemetry/opentelemetry-java/pull/7229))
* Spatial aggregation for async instruments with filtering views
  ([#7264](https://github.com/open-telemetry/opentelemetry-java/pull/7264))

#### Exporters

* Prometheus: Add Authenticator support for PrometheusHttpServer
  ([#7225](https://github.com/open-telemetry/opentelemetry-java/pull/7225))
* OTLP: Fix OTLP metric exporter toBuilder() loosing temporality
  ([#7280](https://github.com/open-telemetry/opentelemetry-java/pull/7280))
* OTLP: Allow Otlp*MetricExporter's to publish export stats
  ([#7255](https://github.com/open-telemetry/opentelemetry-java/pull/7255))

#### Extensions

* Declarative config: Add support for escaping env var substitution
  ([#7033](https://github.com/open-telemetry/opentelemetry-java/pull/7033))
* Declarative config: update to opentelemetry-configuration 0.4
  ([#7064](https://github.com/open-telemetry/opentelemetry-java/pull/7064))
* Declarativeconfig: Refactor internals to add DeclarativeConfigContext
  ([#7293](https://github.com/open-telemetry/opentelemetry-java/pull/7293))

### Project tooling

* Kotlin extension: Update min kotlin version to 1.8
  ([#7155](https://github.com/open-telemetry/opentelemetry-java/pull/7155))
* Add javadoc site crawler
  ([#7300](https://github.com/open-telemetry/opentelemetry-java/pull/7300),
  [#7316](https://github.com/open-telemetry/opentelemetry-java/pull/7316))

## Version 1.49.0 (2025-04-04)

### SDK

#### Trace

* Avoid linear queue.size() calls in span producers by storing queue size separately
  ([#7141](https://github.com/open-telemetry/opentelemetry-java/pull/7141))

#### Exporters

* OTLP: Add support for setting exporter executor service
  ([#7152](https://github.com/open-telemetry/opentelemetry-java/pull/7152))
* OTLP: Refine delay jitter for exponential backoff
  ([#7206](https://github.com/open-telemetry/opentelemetry-java/pull/7206))

#### Extensions

* Autoconfigure: Remove support for otel.experimental.exporter.otlp.retry.enabled
  ([#7200](https://github.com/open-telemetry/opentelemetry-java/pull/7200))
* Autoconfigure: Add stable cardinality limit property otel.java.metrics.cardinality.limit
  ([#7199](https://github.com/open-telemetry/opentelemetry-java/pull/7199))
* Incubator: Add declarative config model customizer SPI
  ([#7118](https://github.com/open-telemetry/opentelemetry-java/pull/7118))

## Version 1.48.0 (2025-03-07)

### API

* Add some helpful logging attribute methods to `LogRecordBuilder`
  ([#7089](https://github.com/open-telemetry/opentelemetry-java/pull/7089))

#### Incubator

* Introduce ConfigProvider API. Rename `StructuredConfigProperties` to `DeclarativeConfigProperties`
  and move to `opentelemetry-api-incubator`. Rename `FileConfiguration`
  to `DeclarativeConfiguration`.
  ([#6549](https://github.com/open-telemetry/opentelemetry-java/pull/6549))

### SDK

* Log warning and adjust when BatchLogRecordProcessor, BatchSpanProcessor `maxExportBatchSize`
  exceeds `maxQueueSize`.
  ([#7045](https://github.com/open-telemetry/opentelemetry-java/pull/7045),
  [#7148](https://github.com/open-telemetry/opentelemetry-java/pull/7148))
* Fix bug causing `ThrottlingLogger` to log more than once per minute
  ([#7156](https://github.com/open-telemetry/opentelemetry-java/pull/7156))

#### Metrics

* Remove obsolete `SdkMeterProviderUtil#setCardinalitylimit` API
  ([#7169](https://github.com/open-telemetry/opentelemetry-java/pull/7169))

#### Traces

* Fix bug preventing accurate reporting of span event dropped attribute count
  ([#7142](https://github.com/open-telemetry/opentelemetry-java/pull/7142))

#### Exporters

* OTLP: remove support for `otel.java.experimental.exporter.memory_mode`
  which was previously replaced by `otel.java.exporter.memory_mode`
  ([#7127](https://github.com/open-telemetry/opentelemetry-java/pull/7127))
* OTLP: Extract sender parameters to config carrier class
  (incubating API)
  ([#7151](https://github.com/open-telemetry/opentelemetry-java/pull/7151))
* OTLP: Add support for setting OTLP exporter service class loader
  ([#7150](https://github.com/open-telemetry/opentelemetry-java/pull/7150))

### Tooling

* Update android animalsniffer min API version to 23
  ([#7153](https://github.com/open-telemetry/opentelemetry-java/pull/7153))

## Version 1.47.0 (2025-02-07)

### API

#### Incubator

* Make `ExtendedTracer` easier to use
  ([#6943](https://github.com/open-telemetry/opentelemetry-java/pull/6943))
* Add `ExtendedLogRecordBuilder#setEventName` and corresponding SDK and OTLP serialization
  ([#7012](https://github.com/open-telemetry/opentelemetry-java/pull/7012))
* BREAKING: Drop event API / SDK
  ([#7053](https://github.com/open-telemetry/opentelemetry-java/pull/7053))

### SDK

* Remove -alpha artifacts from runtime classpath of stable components
  ([#6944](https://github.com/open-telemetry/opentelemetry-java/pull/6944))

#### Traces

* Bugfix: Follow spec on span limits, batch processors
  ([#7030](https://github.com/open-telemetry/opentelemetry-java/pull/7030))
* Add experimental `SdkTracerProvider.setScopeConfigurator(ScopeConfigurator)` for
  updating `TracerConfig` at runtime
  ([#7021](https://github.com/open-telemetry/opentelemetry-java/pull/7021))

#### Profiles

* Add AttributeKeyValue abstraction to common otlp exporters
  ([#7026](https://github.com/open-telemetry/opentelemetry-java/pull/7026))
* Improve profiles attribute table handling
  ([#7031](https://github.com/open-telemetry/opentelemetry-java/pull/7031))

#### Exporters

* Interpret timeout zero value as no limit
  ([#7023](https://github.com/open-telemetry/opentelemetry-java/pull/7023))
* Bugfix - OTLP: Fix concurrent span reusable data marshaler
  ([#7041](https://github.com/open-telemetry/opentelemetry-java/pull/7041))
* OTLP: Add ability to customize retry exception predicate
  ([#6991](https://github.com/open-telemetry/opentelemetry-java/pull/6991))
* OTLP: Expand default OkHttp sender retry exception predicate
  ([#7047](https://github.com/open-telemetry/opentelemetry-java/pull/7047),
  [#7057](https://github.com/open-telemetry/opentelemetry-java/pull/7057))

#### Extensions

* Autoconfigure: Consistent application of exporter customizers when otel.{signal}.exporter=none
  ([#7017](https://github.com/open-telemetry/opentelemetry-java/pull/7017))
* Autoconfigure: Promote EnvironmentResourceProvider to public API
  ([#7052](https://github.com/open-telemetry/opentelemetry-java/pull/7052))
* Autoconfigure: Ensure `OTEL_PROPAGATORS` still works when `OTEL_SDK_DISABLED=true`.
  ([#7062](https://github.com/open-telemetry/opentelemetry-java/pull/7062))%

#### Testing

* Add W3CBaggagePropagator to `OpenTelemetryRule`, `OpenTelemetryExtension`.
  ([#7056](https://github.com/open-telemetry/opentelemetry-java/pull/7056))

## Version 1.46.0 (2025-01-10)

### SDK

* Remove unused dependencies, cleanup code after stabilizing Value
  ([#6948](https://github.com/open-telemetry/opentelemetry-java/pull/6948))
* Explicitly allow null into CompletableResultCode.failExceptionally()
  ([#6963](https://github.com/open-telemetry/opentelemetry-java/pull/6963))

#### Traces

* Fix span setStatus
  ([#6990](https://github.com/open-telemetry/opentelemetry-java/pull/6990))

#### Logs

* Add getters/accessors for readable fields in ReadWriteLogRecord.
  ([#6924](https://github.com/open-telemetry/opentelemetry-java/pull/6924))

#### Exporters

* OTLP: Update to opentelemetry-proto 1.5
  ([#6999](https://github.com/open-telemetry/opentelemetry-java/pull/6999))
* Bugfix - OTLP: Ensure Serializer runtime exceptions are rethrown as IOException
  ([#6969](https://github.com/open-telemetry/opentelemetry-java/pull/6969))
* BREAKING - OTLP: Delete experimental OTLP authenticator concept.
  See [OTLP authentication docs](https://opentelemetry.io/docs/languages/java/sdk/#authentication)
  for supported solutions.
  ([#6984](https://github.com/open-telemetry/opentelemetry-java/pull/6984))

#### Extensions

* BREAKING - Autoconfigure: Remove support for deprecated otel.experimental.resource.disabled.keys
  ([#6931](https://github.com/open-telemetry/opentelemetry-java/pull/6931))

## Version 1.45.0 (2024-12-06)

### API

* Add convenience method `setAttribute(Attribute<Long>, int)` to SpanBuilder (matching the existing
  convenience method in Span)
  ([#6884](https://github.com/open-telemetry/opentelemetry-java/pull/6884))
* Extends TextMapGetter with experimental GetAll() method, implement usage in W3CBaggagePropagator
  ([#6852](https://github.com/open-telemetry/opentelemetry-java/pull/6852))

### SDK

#### Traces

* Add synchronization to SimpleSpanProcessor to ensure thread-safe export of spans
  ([#6885](https://github.com/open-telemetry/opentelemetry-java/pull/6885))

#### Metrics

* Lazily initialize ReservoirCells
  ([#6851](https://github.com/open-telemetry/opentelemetry-java/pull/6851))

#### Logs

* Add synchronization to SimpleLogRecordProcessor to ensure thread-safe export of logs
  ([#6885](https://github.com/open-telemetry/opentelemetry-java/pull/6885))

#### Exporters

* OTLP: Update opentelementry-proto to 1.4
  ([#6906](https://github.com/open-telemetry/opentelemetry-java/pull/6906))
* OTLP: Rename internal Marshaler#writeJsonToGenerator method to allow jackson runtimeOnly dependency
  ([#6896](https://github.com/open-telemetry/opentelemetry-java/pull/6896))
* OTLP: Fix repeated string serialization for JSON.
  ([#6888](https://github.com/open-telemetry/opentelemetry-java/pull/6888))
* OTLP: Fix missing unsafe available check
  ([#6920](https://github.com/open-telemetry/opentelemetry-java/pull/6920))

#### Extensions

* Declarative config: Don't require empty objects when referencing custom components
  ([#6891](https://github.com/open-telemetry/opentelemetry-java/pull/6891))

### Tooling

* Add javadoc boilerplate internal comment v2 for experimental classes
  ([#6886](https://github.com/open-telemetry/opentelemetry-java/pull/6886))
* Update develocity configuration
  ([#6903](https://github.com/open-telemetry/opentelemetry-java/pull/6903))

## Version 1.44.1 (2024-11-10)

### SDK

#### Traces

* Fix regression in event attributes
  ([#6865](https://github.com/open-telemetry/opentelemetry-java/pull/6865))

## Version 1.44.0 (2024-11-08)

### API

* Fix ConfigUtil#getString ConcurrentModificationException
  ([#6841](https://github.com/open-telemetry/opentelemetry-java/pull/6841))

### SDK

#### Traces

* Stabilize ExceptionEventData
  ([#6795](https://github.com/open-telemetry/opentelemetry-java/pull/6795))

#### Metrics

* Stabilize metric cardinality limits
  ([#6794](https://github.com/open-telemetry/opentelemetry-java/pull/6794))
* Refactor metrics internals to remove MeterSharedState
  ([#6845](https://github.com/open-telemetry/opentelemetry-java/pull/6845))

#### Exporters

* Add memory mode option to stdout exporters
  ([#6774](https://github.com/open-telemetry/opentelemetry-java/pull/6774))
* Log a warning if OTLP endpoint port is likely incorrect given the protocol
  ([#6813](https://github.com/open-telemetry/opentelemetry-java/pull/6813))
* Fix OTLP gRPC retry mechanism for unsuccessful HTTP responses
  ([#6829](https://github.com/open-telemetry/opentelemetry-java/pull/6829))
* Add ByteBuffer field type marshaling support
  ([#6686](https://github.com/open-telemetry/opentelemetry-java/pull/6686))
* Fix stdout exporter format by adding newline after each export
  ([#6848](https://github.com/open-telemetry/opentelemetry-java/pull/6848))
* Enable `reusuable_data` memory mode by default for `OtlpGrpc{Signal}Exporter`,
  `OtlpHttp{Signal}Exporter`, `OtlpStdout{Signal}Exporter`, and `PrometheusHttpServer`
  ([#6799](https://github.com/open-telemetry/opentelemetry-java/pull/6799))

#### Extension

* Rebrand file configuration to declarative configuration in documentation
  ([#6812](https://github.com/open-telemetry/opentelemetry-java/pull/6812))
* Fix declarative config `file_format` validation
  ([#6786](https://github.com/open-telemetry/opentelemetry-java/pull/6786))
* Fix declarative config env substitution by disallowing '}' in default value
  ([#6793](https://github.com/open-telemetry/opentelemetry-java/pull/6793))
* Set declarative config default OTLP protocol to http/protobuf
  ([#6800](https://github.com/open-telemetry/opentelemetry-java/pull/6800))
* Stabilize autoconfigure disabling of resource keys via `otel.resource.disabled.keys`
  ([#6809](https://github.com/open-telemetry/opentelemetry-java/pull/6809))

### Tooling

* Run tests on Java 23
  ([#6825](https://github.com/open-telemetry/opentelemetry-java/pull/6825))
* Test Windows in CI
  ([#6824](https://github.com/open-telemetry/opentelemetry-java/pull/6824))
* Add error prone checks for internal javadoc and private constructors
  ([#6844](https://github.com/open-telemetry/opentelemetry-java/pull/6844))

## Version 1.43.0 (2024-10-11)

### API

* Add helper class to capture context using ScheduledExecutorService
  ([#6712](https://github.com/open-telemetry/opentelemetry-java/pull/6712))
* Adds Baggage.getEntry(String key)
  ([#6765](https://github.com/open-telemetry/opentelemetry-java/pull/6765))

#### Extensions

* Fix ottracepropagation for short span ids
  ([#6734](https://github.com/open-telemetry/opentelemetry-java/pull/6734))

### SDK

#### Metrics

* Optimize advice with FilteredAttributes
  ([#6633](https://github.com/open-telemetry/opentelemetry-java/pull/6633))

#### Exporters

* Add experimental stdout log, metric, trace exporters for printing records to stdout in standard
  OTLP JSON format.
  ([#6675](https://github.com/open-telemetry/opentelemetry-java/pull/6675), [#6750](https://github.com/open-telemetry/opentelemetry-java/pull/6750))
* Add Marshalers for profiling signal type
  ([#6680](https://github.com/open-telemetry/opentelemetry-java/pull/6680))

#### Extensions

* Add `*Model` suffix to declarative config generated classes.
  ([#6721](https://github.com/open-telemetry/opentelemetry-java/pull/6721))
* Use autoconfigured ClassLoader to load declarative config
  ([#6725](https://github.com/open-telemetry/opentelemetry-java/pull/6725))
* Update declarative config to use opentelemetry-configuration v0.3.0
  ([#6733](https://github.com/open-telemetry/opentelemetry-java/pull/6733))
* Add `StructuredConfigProperties#getStructured` default method,
  add `StructuredConfigProperties.empty()`
  ([#6759](https://github.com/open-telemetry/opentelemetry-java/pull/6759))

#### Testing

* Add context info about wrong span or trace.
  ([#6703](https://github.com/open-telemetry/opentelemetry-java/pull/6703))

## Version 1.42.1 (2024-09-10)

### API

* Revert `java-test-fixtures` plugin to remove test dependencies from `pom.xml`.
  ([#6695](https://github.com/open-telemetry/opentelemetry-java/pull/6695))

## Version 1.42.0 (2024-09-06)

### API

* BREAKING: Stabilize log support for AnyValue bodies. Rename `AnyValue` to `Value`, promote
  from `opentelemetry-api-incubator` to `opentelemetry-api`, change package
  from `io.opentelemetry.api.incubator.logs` to `io.opentelemetry.api.common`.
  ([#6591](https://github.com/open-telemetry/opentelemetry-java/pull/6591))
* Noop implementations detect when `opentelemetry-api-incubator` is present and return extended noop
  implementations.
  ([#6617](https://github.com/open-telemetry/opentelemetry-java/pull/6617))%

### SDK

#### Traces

* Added experimental support for SpanProcessor OnEnding callback
  ([#6367](https://github.com/open-telemetry/opentelemetry-java/pull/6367))
* Remove final modifier from SdkTracer.tracerEnabled
  ([#6687](https://github.com/open-telemetry/opentelemetry-java/pull/6687))

#### Exporters

* Suppress zipkin exporter instrumentation
  ([#6552](https://github.com/open-telemetry/opentelemetry-java/pull/6552))
* OTLP exporters return status code exceptions via CompletableResultCode in GrpcExporter and
  HttpExporter.
  ([#6645](https://github.com/open-telemetry/opentelemetry-java/pull/6645))
* Align GrpcSender contract with HttpSender
  ([#6658](https://github.com/open-telemetry/opentelemetry-java/pull/6658))

#### Extensions

* Add autoconfigure support for ns and us durations
  ([#6654](https://github.com/open-telemetry/opentelemetry-java/pull/6654))
* Add declarative configuration ComponentProvider support for resources
  ([#6625](https://github.com/open-telemetry/opentelemetry-java/pull/6625))
* Add declarative configuration ComponentProvider support for processors
  ([#6623](https://github.com/open-telemetry/opentelemetry-java/pull/6623))
* Add declarative configuration ComponentProvider support for samplers
  ([#6494](https://github.com/open-telemetry/opentelemetry-java/pull/6494))
* Add declarative configuration ComponentProvider support for propagators
  ([#6624](https://github.com/open-telemetry/opentelemetry-java/pull/6624))
* Add declarative configuration missing pieces
  ([#6677](https://github.com/open-telemetry/opentelemetry-java/pull/6677))
* Change jaeger remote sampler autoconfigure property from `pollingInterval` to `pollingIntervalMs`
  to match spec.
  ([#6672](https://github.com/open-telemetry/opentelemetry-java/pull/6672))

#### Testing

* Add asserts for log record body fields
  ([#6509](https://github.com/open-telemetry/opentelemetry-java/pull/6509))

## Version 1.41.0 (2024-08-09)

### API

* Move experimental suppress instrumentation context key to api internal package
  ([#6546](https://github.com/open-telemetry/opentelemetry-java/pull/6546))

#### Incubator

* Fix bug in `ExtendedContextPropagators` preventing context extraction when case is incorrect.
  ([#6569](https://github.com/open-telemetry/opentelemetry-java/pull/6569))

### SDK

* Extend `CompletableResultCode` with `failExceptionally(Throwable)`.
  ([#6348](https://github.com/open-telemetry/opentelemetry-java/pull/6348))

#### Metrics

* Avoid allocations when experimental advice doesn't remove any attributes.
  ([#6629](https://github.com/open-telemetry/opentelemetry-java/pull/6629))

#### Exporter

* Enable retry by default for OTLP exporters.
  ([#6588](https://github.com/open-telemetry/opentelemetry-java/pull/6588))
* Retry ConnectException, add retry logging.
  ([#6614](https://github.com/open-telemetry/opentelemetry-java/pull/6614))
* Extend `PrometheusHttpServer` with ability to configure default aggregation as function of
  instrument kind, including experimental env var support.
  ([#6541](https://github.com/open-telemetry/opentelemetry-java/pull/6541))
* Add exporter data model impl for profiling signal type.
  ([#6498](https://github.com/open-telemetry/opentelemetry-java/pull/6498))
* Add Marshalers for profiling signal type.
  ([#6565](https://github.com/open-telemetry/opentelemetry-java/pull/6565))
* Use generateCertificates() of CertificateFactory to process certificates.
  ([#6579](https://github.com/open-telemetry/opentelemetry-java/pull/6579))

#### Extensions

* Add file configuration ComponentProvider support for exporters.
  ([#6493](https://github.com/open-telemetry/opentelemetry-java/pull/6493))
* Remove nullable from file config Factory contract.
  ([#6612](https://github.com/open-telemetry/opentelemetry-java/pull/6612))

## Version 1.40.0 (2024-07-05)

### API

#### Incubator

* Narrow ExtendedSpanBuilder return types for chaining
  ([#6514](https://github.com/open-telemetry/opentelemetry-java/pull/6514))
* Add APIs to determine if tracer, logger, instruments are enabled
  ([#6502](https://github.com/open-telemetry/opentelemetry-java/pull/6502))

### SDK

#### Extensions

* Move autoconfigure docs to opentelemetry.io
  ([#6491](https://github.com/open-telemetry/opentelemetry-java/pull/6491))

## Version 1.39.0 (2024-06-07)

### API

#### Incubator

* BREAKING: Refactor ExtendedTracer, ExtendedSpanBuilder to reflect incubating API conventions
  ([#6497](https://github.com/open-telemetry/opentelemetry-java/pull/6497))

### SDK

#### Exporter

* BREAKING: Serve prometheus metrics only on `/metrics` by default. To restore the previous behavior
  and serve metrics on all paths, override the default handler
  as [demonstrated here](https://github.com/open-telemetry/opentelemetry-java/blob/main/exporters/prometheus/src/test/java/io/opentelemetry/exporter/prometheus/PrometheusHttpServerTest.java#L251-L259).
  ([#6476](https://github.com/open-telemetry/opentelemetry-java/pull/6476))
* Make OTLP exporter memory mode API public
  ([#6469](https://github.com/open-telemetry/opentelemetry-java/pull/6469))
* Speed up OTLP string marshaling using sun.misc.Unsafe
  ([#6433](https://github.com/open-telemetry/opentelemetry-java/pull/6433))
* Add exporter data classes for experimental profiling signal type.
  ([#6374](https://github.com/open-telemetry/opentelemetry-java/pull/6374))
* Start prometheus http server with daemon thread
  ([#6472](https://github.com/open-telemetry/opentelemetry-java/pull/6472))
* Update the Prometheus metrics library and improve how units are included in metric names.
  ([#6473](https://github.com/open-telemetry/opentelemetry-java/pull/6473))
* Remove android animalsniffer check from prometheus exporter
  ([#6478](https://github.com/open-telemetry/opentelemetry-java/pull/6478))

#### Extensions

* Load file config YAML using core schema, ensure that env var substitution retains string types.
  ([#6436](https://github.com/open-telemetry/opentelemetry-java/pull/6436))
* Define dedicated file configuration SPI ComponentProvider
  ([#6457](https://github.com/open-telemetry/opentelemetry-java/pull/6457))

### Tooling

* Normalize timestamps and file ordering in jars, making the outputs reproducible
  ([#6471](https://github.com/open-telemetry/opentelemetry-java/pull/6471))
* GHA for generating the post-release pull request
  ([#6449](https://github.com/open-telemetry/opentelemetry-java/pull/6449))

## Version 1.38.0 (2024-05-10)

### API

* Stabilize synchronous gauge
  ([#6419](https://github.com/open-telemetry/opentelemetry-java/pull/6419))

#### Incubator

* Add put(AttributeKey<T>, T) overload to EventBuilder
  ([#6331](https://github.com/open-telemetry/opentelemetry-java/pull/6331))

#### Baggage

* Baggage filters space-only keys
  ([#6431](https://github.com/open-telemetry/opentelemetry-java/pull/6431))

### SDK

* Add experimental scope config to enable / disable scopes (i.e. meter, logger, tracer)
  ([#6375](https://github.com/open-telemetry/opentelemetry-java/pull/6375))

#### Traces

* Add ReadableSpan#getAttributes
  ([#6382](https://github.com/open-telemetry/opentelemetry-java/pull/6382))
* Use standard ArrayList size rather than max number of links for initial span links allocation
  ([#6252](https://github.com/open-telemetry/opentelemetry-java/pull/6252))

#### Metrics

* Use low precision Clock#now when computing timestamp for exemplars
  ([#6417](https://github.com/open-telemetry/opentelemetry-java/pull/6417))
* Update invalid instrument name log message now that forward slash `/` is valid
  ([#6343](https://github.com/open-telemetry/opentelemetry-java/pull/6343))

#### Exporters

* Introduce low allocation OTLP marshalers. If using autoconfigure, opt in
  via `OTEL_JAVA_EXPERIMENTAL_EXPORTER_MEMORY_MODE=REUSABLE_DATA`.
  * Low allocation OTLP logs marshaler
    ([#6429](https://github.com/open-telemetry/opentelemetry-java/pull/6429))
  * Low allocation OTLP metrics marshaler
    ([#6422](https://github.com/open-telemetry/opentelemetry-java/pull/6422))
  * Low allocation OTLP trace marshaler
    ([#6410](https://github.com/open-telemetry/opentelemetry-java/pull/6410))
  * Add memory mode support to OTLP exporters
    ([#6430](https://github.com/open-telemetry/opentelemetry-java/pull/6430))
  * Marshal span status description without allocation
    ([#6423](https://github.com/open-telemetry/opentelemetry-java/pull/6423))
  * Add private constructors for stateless marshalers
    ([#6434](https://github.com/open-telemetry/opentelemetry-java/pull/6434))
* Mark opentelemetry-exporter-sender-jdk stable
  ([#6357](https://github.com/open-telemetry/opentelemetry-java/pull/6357))
* PrometheusHttpServer prevent concurrent reads when reusable memory mode
  ([#6371](https://github.com/open-telemetry/opentelemetry-java/pull/6371))
* Ignore TLS components (SSLContext, TrustManager, KeyManager) if plain HTTP protocol is used for
  exporting
  ([#6329](https://github.com/open-telemetry/opentelemetry-java/pull/6329))
* Add is_remote_parent span flags to OTLP exported Spans and SpanLinks
  ([#6388](https://github.com/open-telemetry/opentelemetry-java/pull/6388))
* Add missing fields to OTLP metric exporters `toString()`
  ([#6402](https://github.com/open-telemetry/opentelemetry-java/pull/6402))

#### Extensions

* Rename otel.config.file to otel.experimental.config.file for autoconfigure
  ([#6396](https://github.com/open-telemetry/opentelemetry-java/pull/6396))

### OpenCensus Shim

* Fix opencensus shim spanBuilderWithRemoteParent behavior
  ([#6415](https://github.com/open-telemetry/opentelemetry-java/pull/6415))

### Tooling

* Add additional API incubator docs
  ([#6356](https://github.com/open-telemetry/opentelemetry-java/pull/6356))
* Run build on java 21
  ([#6370](https://github.com/open-telemetry/opentelemetry-java/pull/6370))
* Fix running tests with java 8 on macos
  ([#6411](https://github.com/open-telemetry/opentelemetry-java/pull/6411))
* Move away from deprecated gradle enterprise APIs
  ([#6363](https://github.com/open-telemetry/opentelemetry-java/pull/6363))

## Version 1.37.0 (2024-04-05)

**NOTICE:** This release contains a significant restructuring of the experimental event API and the API incubator artifact. Please read the notes in the `API -> Incubator` section carefully.

### API

* Promote `Span#addLink` to stable API
  ([#6317](https://github.com/open-telemetry/opentelemetry-java/pull/6317))

#### Incubator

* BREAKING: Rename `opentelemetry-extension-incubator` to `opentelemetry-api-incubator`,
  merge `opentelemetry-api-events` into `opentelemetry-api-incubator`.
  ([#6289](https://github.com/open-telemetry/opentelemetry-java/pull/6289))
* BREAKING: Remove domain from event api. `EventEmitterProvider#setEventDomain` has been removed.
  The `event.name` field should now be namespaced to avoid collisions.
  See [Semantic Conventions for Event Attributes](https://opentelemetry.io/docs/specs/semconv/general/events/)
  for more details.
  ([#6253](https://github.com/open-telemetry/opentelemetry-java/pull/6253))
* BREAKING: Rename `EventEmitter` and related classes to `EventLogger`.
  ([#6316](https://github.com/open-telemetry/opentelemetry-java/pull/6316))
* BREAKING: Refactor Event API to reflect spec changes. Restructure API to put fields in
  the `AnyValue` log record body. Add setters for timestamp, context, and severity. Set default
  severity to `INFO=9`.
  ([#6318](https://github.com/open-telemetry/opentelemetry-java/pull/6318))

### SDK

* Add `get{Signal}Exporter` methods to `Simple{Signal}Processor`, `Batch{Signal}Processor`.
  ([#6078](https://github.com/open-telemetry/opentelemetry-java/pull/6078))

#### Metrics

* Use synchronized instead of reentrant lock in explicit bucket histogram
  ([#6309](https://github.com/open-telemetry/opentelemetry-java/pull/6309))

#### Exporters

* Fix typo in OTLP javadoc
  ([#6311](https://github.com/open-telemetry/opentelemetry-java/pull/6311))
* Add `PrometheusHttpServer#toBuilder()`
  ([#6333](https://github.com/open-telemetry/opentelemetry-java/pull/6333))
* Bugfix: Use `getPrometheusName` for Otel2PrometheusConverter map keys to avoid metric name
  conflicts
  ([#6308](https://github.com/open-telemetry/opentelemetry-java/pull/6308))

#### Extensions

* Add Metric exporter REUSABLE_DATA memory mode configuration options, including autoconfigure
  support via env var `OTEL_JAVA_EXPERIMENTAL_EXPORTER_MEMORY_MODE=REUSABLE_DATA`.
  ([#6304](https://github.com/open-telemetry/opentelemetry-java/pull/6304))
* Add autoconfigure console alias for logging exporter
  ([#6027](https://github.com/open-telemetry/opentelemetry-java/pull/6027))
* Update jaeger autoconfigure docs to point to OTLP
  ([#6307](https://github.com/open-telemetry/opentelemetry-java/pull/6307))
* Add `ServiceInstanceIdResourceProvider` implementation for generating `service.instance.id` UUID
  if not already provided by user. Included in `opentelemetry-sdk-extension-incubator`.
  ([#6226](https://github.com/open-telemetry/opentelemetry-java/pull/6226))
* Add GCP resource detector to list of resource providers in autoconfigure docs
  ([#6336](https://github.com/open-telemetry/opentelemetry-java/pull/6336))

### Tooling

* Check for Java 17 toolchain and fail if not found
  ([#6303](https://github.com/open-telemetry/opentelemetry-java/pull/6303))

## Version 1.36.0 (2024-03-08)

### SDK

#### Traces

* Lazily initialize the container for events in the SDK Span implementation
  ([#6244](https://github.com/open-telemetry/opentelemetry-java/pull/6244))

#### Exporters

* Add basic proxy configuration to OtlpHttp{Signal}Exporters
  ([#6270](https://github.com/open-telemetry/opentelemetry-java/pull/6270))
* Add connectTimeout configuration option OtlpGrpc{Signal}Exporters
  ([#6079](https://github.com/open-telemetry/opentelemetry-java/pull/6079))

#### Extensions

* Add ComponentLoader to autoconfigure support more scenarios
  ([#6217](https://github.com/open-telemetry/opentelemetry-java/pull/6217))
* Added MetricReader customizer for AutoConfiguredOpenTelemetrySdkBuilder
  ([#6231](https://github.com/open-telemetry/opentelemetry-java/pull/6231))
* Return AutoConfiguredOpenTelemetrySdkBuilder instead of the base type
  ([#6248](https://github.com/open-telemetry/opentelemetry-java/pull/6248))

### Tooling

* Add note about draft PRs to CONTRIBUTING.md
  ([#6247](https://github.com/open-telemetry/opentelemetry-java/pull/6247))

## Version 1.35.0 (2024-02-09)

**NOTE:** The `opentelemetry-exporter-jaeger` and `opentelemetry-exporter-jaeger-thift` artifacts
have stopped being published. Jaeger
has [native support for OTLP](https://opentelemetry.io/blog/2022/jaeger-native-otlp/), and users
should export to jaeger
using OTLP
instead.

### API

#### Incubator

* Add Span#addLink, for adding a link after span start
  ([#6084](https://github.com/open-telemetry/opentelemetry-java/pull/6084))

### SDK

#### Traces

* Bugfix: Ensure span status cannot be updated after set to StatusCode.OK
  ([#6209](https://github.com/open-telemetry/opentelemetry-java/pull/6209)

#### Metrics

* Reusable memory Mode: Adding support for exponential histogram aggregation
  ([#6058](https://github.com/open-telemetry/opentelemetry-java/pull/6058),
   [#6136](https://github.com/open-telemetry/opentelemetry-java/pull/6136))
* Reusable memory mode: Adding support for explicit histogram aggregation
  ([#6153](https://github.com/open-telemetry/opentelemetry-java/pull/6153))
* Reusable memory mode: Adding support for sum aggregation
  ([#6182](https://github.com/open-telemetry/opentelemetry-java/pull/6182))
* Reusable memory mode: Adding support for last value aggregation
  ([#6196](https://github.com/open-telemetry/opentelemetry-java/pull/6196))

#### Exporters

* Recreate / fix graal issue detecting RetryPolicy class
  ([#6139](https://github.com/open-telemetry/opentelemetry-java/pull/6139),
   [#6134](https://github.com/open-telemetry/opentelemetry-java/pull/6134))
* Restore prometheus metric name mapper tests, fix regressions
  ([#6138](https://github.com/open-telemetry/opentelemetry-java/pull/6138))
* WARNING: Remove jaeger exporters
  ([#6119](https://github.com/open-telemetry/opentelemetry-java/pull/6119))
* Update dependency `io.zipkin.reporter2:zipkin-reporter-bom` to 3.2.1.
  Note: `ZipkinSpanExporterBuilder#setEncoder(zipkin2.codec.BytesEncoder)` has been deprecated in
  favor of `ZipkinSpanExporterBuilder#setEncoder(zipkin2.reporter.BytesEncoder)`.
  `ZipkinSpanExporterBuilder#setSender(zipkin2.reporter.Sender)` has been deprecated in favor
  of `ZipkinSpanExporterBuilder#setSender(zipkin2.reporter.BytesMessageSender)`.
  ([#6129](https://github.com/open-telemetry/opentelemetry-java/pull/6129),
  [#6151](https://github.com/open-telemetry/opentelemetry-java/pull/6151))
* Include trace flags in otlp marshaller
  ([#6167](https://github.com/open-telemetry/opentelemetry-java/pull/6167))
* Add Compressor SPI support to OtlpGrpc{Signal}Exporters
  ([#6103](https://github.com/open-telemetry/opentelemetry-java/pull/6103))
* Allow Prometheus exporter to add resource attributes to metric attributes
  ([#6179](https://github.com/open-telemetry/opentelemetry-java/pull/6179))

#### Extension

* Autoconfigure accepts encoded header values for OTLP exporters
  ([#6164](https://github.com/open-telemetry/opentelemetry-java/pull/6164))
* Return implementation type from `AutoConfiguredOpenTelemetrySdkBuilder.addLogRecordProcessorCustomizer`
  ([#6248](https://github.com/open-telemetry/opentelemetry-java/pull/6248))

#### Incubator

* Align file configuration with latest changes to spec
  ([#6088](https://github.com/open-telemetry/opentelemetry-java/pull/6088))

### Tooling

* Stop including old artifacts in bom
  ([#6157](https://github.com/open-telemetry/opentelemetry-java/pull/6157))
* Define CODECOV token
  ([#6186](https://github.com/open-telemetry/opentelemetry-java/pull/6186))

## Version 1.34.1 (2024-01-11)

* Fix prometheus exporter regressions
  ([#6138](https://github.com/open-telemetry/opentelemetry-java/pull/6138))
* Fix native image regression
  ([#6134](https://github.com/open-telemetry/opentelemetry-java/pull/6134))

## Version 1.34.0 (2024-01-05)

**NOTE:** This is the LAST release for `opentelemetry-exporter-jaeger`
and `opentelemetry-exporter-jaeger-thift`. Jaeger
has [native support for OTLP](https://opentelemetry.io/blog/2022/jaeger-native-otlp/), and users
should export to jaeger
using OTLP
instead.

### API

* Ability to access version.properties API file with GraalVM native
  ([#6095](https://github.com/open-telemetry/opentelemetry-java/pull/6095))

### SDK

#### Traces

* Only call SpanProcessor onStart / onEnd if required
  ([#6112](https://github.com/open-telemetry/opentelemetry-java/pull/6112))
* Add option to export unsampled spans from span processors
  ([#6057](https://github.com/open-telemetry/opentelemetry-java/pull/6057))

#### Metrics

* Memory Mode: Adding first part support for synchronous instruments - storage
  ([#5998](https://github.com/open-telemetry/opentelemetry-java/pull/5998))
* Base2ExponentialHistogramAggregation maxBuckets must be >= 2
  ([#6093](https://github.com/open-telemetry/opentelemetry-java/pull/6093))
* Convert histogram measurements to double before passing recording exemplar reservoir
  ([#6024](https://github.com/open-telemetry/opentelemetry-java/pull/6024))

#### Exporters

* Add compressor SPI to support additional compression algos
  ([#5990](https://github.com/open-telemetry/opentelemetry-java/pull/5990))
* Test OTLP exporters with different OkHttp versions
  ([#6045](https://github.com/open-telemetry/opentelemetry-java/pull/6045))
* Refactor prometheus exporter to use `io.prometheus:prometheus-metrics-exporter-httpserver`, add
  exponential Histogram support
  ([#6015](https://github.com/open-telemetry/opentelemetry-java/pull/6015))
* UpstreamGrpcSenderProvider uses minimal fallback managed channel when none is specified
  ([#6110](https://github.com/open-telemetry/opentelemetry-java/pull/6110))
* OTLP exporters propagate serialization IOException instead of rethrowing as runtime
  ([#6082](https://github.com/open-telemetry/opentelemetry-java/pull/6082))

#### Extensions

* Autoconfigure reads normalized otel.config.file property
  ([#6105](https://github.com/open-telemetry/opentelemetry-java/pull/6105))

## Version 1.33.0 (2023-12-08)

### API

* Fix issue where wrapping "invalid" SpanContexts in Span does not preserve SpanContext
  ([#6044](https://github.com/open-telemetry/opentelemetry-java/pull/6044))

#### Incubator

* Refactor and add to ExtendedTracer, add ExtendedContextPropagators
  ([#6017](https://github.com/open-telemetry/opentelemetry-java/pull/6017))
* Base64 encode AnyValue bytes in string representation
  ([#6003](https://github.com/open-telemetry/opentelemetry-java/pull/6003))

### SDK

#### Exporters

* Add connectTimeout configuration option OtlpHttp{Signal}Exporters
  ([#5941](https://github.com/open-telemetry/opentelemetry-java/pull/5941))
* Add ability for Otlp{Protocol}LogRecordExporter to serialize log body any value
  ([#5938](https://github.com/open-telemetry/opentelemetry-java/pull/5938))
* Android environments can now handle base64 encoded PEM keys, remove exception handling in
  TlsUtil#decodePem
  ([#6034](https://github.com/open-telemetry/opentelemetry-java/pull/6034))
* Add header supplier configuration option to OTLP exporters
  ([#6004](https://github.com/open-telemetry/opentelemetry-java/pull/6004))


#### Extensions

* Add autoconfigure option for customizing SpanProcessor, LogRecordProcessor
  ([#5986](https://github.com/open-telemetry/opentelemetry-java/pull/5986))
* Incubator allows for simpler creation of start-only and end-only SpanProcessors.
  ([#5923](https://github.com/open-telemetry/opentelemetry-java/pull/5923))

#### Testing

* Add hasAttributesSatisfying overload to AbstractPointAssert
  ([#6048](https://github.com/open-telemetry/opentelemetry-java/pull/6048))

### Project Tooling

* Building animal sniffer signatures directly from android corelib
  ([#5973](https://github.com/open-telemetry/opentelemetry-java/pull/5973))
* Target kotlin 1.6 in kotlin extension
  ([#5910](https://github.com/open-telemetry/opentelemetry-java/pull/5910))
* Define language version compatibility requirements
  ([#5983](https://github.com/open-telemetry/opentelemetry-java/pull/5983))

## Version 1.32.0 (2023-11-13)

### API

* Stabilize explicit bucket boundaries advice API
  ([#5897](https://github.com/open-telemetry/opentelemetry-java/pull/5897))
* Allow events to be emitted with timestamp
  ([#5928](https://github.com/open-telemetry/opentelemetry-java/pull/5928))

#### Context

* Add null check to StrictContextStorage
  ([#5954](https://github.com/open-telemetry/opentelemetry-java/pull/5954))

#### Incubator

* Experimental support for Log AnyValue body
  ([#5880](https://github.com/open-telemetry/opentelemetry-java/pull/5880))

### SDK

#### Metrics

* Dismantle AbstractInstrumentBuilder inheritance hierarchy
  ([#5820](https://github.com/open-telemetry/opentelemetry-java/pull/5820))
* Fix delta metric storage concurrency bug that allows for lost writes when record operations occur
  during collection. The fix introduces additional work on record threads to ensure correctness. The
  additional overhead is non-blocking and should be small according to performance testing. Still,
  there may be an opportunity for further optimization.
  ([#5932](https://github.com/open-telemetry/opentelemetry-java/pull/5932),
  [#5976](https://github.com/open-telemetry/opentelemetry-java/pull/5976))


#### Exporters

* Prometheus exporter: omit empty otel_scope_info and otel_target_info metrics
  ([#5887](https://github.com/open-telemetry/opentelemetry-java/pull/5887))
* JdkHttpSender should retry on connect exceptions
  ([#5867](https://github.com/open-telemetry/opentelemetry-java/pull/5867))
* Expand the set of retryable exceptions in JdkHttpSender
  ([#5942](https://github.com/open-telemetry/opentelemetry-java/pull/5942))
* Identify OTLP export calls with context key used for instrumentation suppression
  ([#5918](https://github.com/open-telemetry/opentelemetry-java/pull/5918))

#### Testing

* Add log support to junit extensions
  ([#5966](https://github.com/open-telemetry/opentelemetry-java/pull/5966))

#### SDK Extensions

* Add file configuration to autoconfigure
  ([#5831](https://github.com/open-telemetry/opentelemetry-java/pull/5831))
* Update to file configuration to use opentelemetry-configuration v0.1.0
  ([#5899](https://github.com/open-telemetry/opentelemetry-java/pull/5899))
* Add env var substitution support to file configuration
  ([#5914](https://github.com/open-telemetry/opentelemetry-java/pull/5914))
* Stop setting Resource schemaUrl in autoconfigure
  ([#5911](https://github.com/open-telemetry/opentelemetry-java/pull/5911))
* Add AutoConfigureListener to provide components with autoconfigured SDK
  ([#5931](https://github.com/open-telemetry/opentelemetry-java/pull/5931))

### OpenCensus  Shim

* Clean up OpenCensus shim
  ([#5858](https://github.com/open-telemetry/opentelemetry-java/pull/5858))

### OpenTracing Shim

* Fix OpenTracing header name issue
  ([#5840](https://github.com/open-telemetry/opentelemetry-java/pull/5840))

## Version 1.31.0 (2023-10-06)

### API

#### Incubator

* Refactor advice API to simplify usage
  ([#5848](https://github.com/open-telemetry/opentelemetry-java/pull/5848))

### SDK

* BatchLogRecordProcessor and BatchSpanProcessor unify `queueSize` metric
  description and attribute name for `processorType`
  ([#5836](https://github.com/open-telemetry/opentelemetry-java/pull/5836))

#### Metrics

* Allow instrument names to contain a forward slash
  ([#5824](https://github.com/open-telemetry/opentelemetry-java/pull/5824))
* Memory Mode support: Adding memory mode, and implementing it for Asynchronous Instruments
  ([#5709](https://github.com/open-telemetry/opentelemetry-java/pull/5709),
  [#5855](https://github.com/open-telemetry/opentelemetry-java/pull/5855))
* Stabilize MetricProducer, allow custom MetricReaders
  ([#5835](https://github.com/open-telemetry/opentelemetry-java/pull/5835))
* Drop NaN measurements to metric instruments
  ([#5859](https://github.com/open-telemetry/opentelemetry-java/pull/5859))
* Fix flaky MetricExporterConfigurationTest
  ([#5877](https://github.com/open-telemetry/opentelemetry-java/pull/5877))

#### Logs

* Add addAllAttributes() to ReadWriteLogRecord.
  ([#5825](https://github.com/open-telemetry/opentelemetry-java/pull/5825))

#### Exporters

* Prometheus exporter: handle colliding metric attribute keys
  ([#5717](https://github.com/open-telemetry/opentelemetry-java/pull/5717))

#### SDK Extensions

* File configuration ConfigurationReader handles null values as empty
  ([#5829](https://github.com/open-telemetry/opentelemetry-java/pull/5829))

#### Semantic conventions

* BREAKING: Stop publishing `io.opentelemetry:opentelemetry-semconv`. Please use
  `io.opentelemetry.semconv:opentelemetry-semconv:1.21.0-alpha` instead, which is published
  from [open-telemetry/semantic-conventions-java](https://github.com/open-telemetry/semantic-conventions-java).
  The new repository is published in lockstep
  with [open-telemetry/semantic-conventions](https://github.com/open-telemetry/semantic-conventions).
  ([#5807](https://github.com/open-telemetry/opentelemetry-java/pull/5807))

### Project Tooling

* Add Benchmark workflows
  ([#5842](https://github.com/open-telemetry/opentelemetry-java/pull/5842),
  [#5874](https://github.com/open-telemetry/opentelemetry-java/pull/5874))
* Add clearer docs around coroutine support with an example
  ([#5799](https://github.com/open-telemetry/opentelemetry-java/pull/5799))

## Version 1.30.1 (2023-09-11)

* Fix autoconfigure bug creating multiple `PrometheusHttpServer` instances with same port
  ([#5811](https://github.com/open-telemetry/opentelemetry-java/pull/5811))

## Version 1.30.0 (2023-09-08)

### API

#### Incubator

* Add experimental synchronous gauge
  ([#5506](https://github.com/open-telemetry/opentelemetry-java/pull/5506))

### SDK

#### Metrics

* Add attributes advice API
  ([#5677](https://github.com/open-telemetry/opentelemetry-java/pull/5677),
  [#5722](https://github.com/open-telemetry/opentelemetry-java/pull/5722))
* Add AttributesProcessor toString, add attribute filter helper
  ([#5765](https://github.com/open-telemetry/opentelemetry-java/pull/5765))
* Increase metric name maximum length from 63 to 255 characters
  ([#5697](https://github.com/open-telemetry/opentelemetry-java/pull/5697))

#### Exporter

* Prometheus exporter: remove non-ucum units from conversion
  ([#5719](https://github.com/open-telemetry/opentelemetry-java/pull/5719))
* Prometheus exporter: add units to metric names in TYPE and HELP comments
  ([#5718](https://github.com/open-telemetry/opentelemetry-java/pull/5718))

#### SDK Extensions

* Add support for file based configuration to incubator
  ([#5687](https://github.com/open-telemetry/opentelemetry-java/pull/5687),
  [#5751](https://github.com/open-telemetry/opentelemetry-java/pull/5751),
  [#5758](https://github.com/open-telemetry/opentelemetry-java/pull/5758),
  [#5757](https://github.com/open-telemetry/opentelemetry-java/pull/5757),
  [#5755](https://github.com/open-telemetry/opentelemetry-java/pull/5755),
  [#5763](https://github.com/open-telemetry/opentelemetry-java/pull/5763),
  [#5766](https://github.com/open-telemetry/opentelemetry-java/pull/5766),
  [#5773](https://github.com/open-telemetry/opentelemetry-java/pull/5773),
  [#5771](https://github.com/open-telemetry/opentelemetry-java/pull/5771),
  [#5779](https://github.com/open-telemetry/opentelemetry-java/pull/5779))
* Autoconfigure ConfigProperties#getMap filters entries with blank values instead of throwing
  ([#5784](https://github.com/open-telemetry/opentelemetry-java/pull/5784))

### Semantic conventions

* DEPRECATION: `io.opentelemetry:opentelemetry-semconv` is deprecated for removal. Please use
  `io.opentelemetry.semconv:opentelemetry-semconv:1.21.0-alpha` instead, which is published
  from [open-telemetry/semantic-conventions-java](https://github.com/open-telemetry/semantic-conventions-java).
  The new repository is published in lockstep
  with [open-telemetry/semantic-conventions](https://github.com/open-telemetry/semantic-conventions).
  ([#5786](https://github.com/open-telemetry/opentelemetry-java/pull/5786))

### Project Tooling

* Update Gradle Wrapper from 8.2.1 to 8.3
  ([#5728](https://github.com/open-telemetry/opentelemetry-java/pull/5728))
* Remove dependabot and `update-gradle-wrapper` task in favor of renovate
  ([#5746](https://github.com/open-telemetry/opentelemetry-java/pull/5746))

## Version 1.29.0 (2023-08-11)

### API

* Update Span javadoc to allow null/empty attr values
  ([#5616](https://github.com/open-telemetry/opentelemetry-java/pull/5616))

### SDK

* Add Sdk{Signal}ProviderBuilder#addResource(Resource) method to merge Resource
  with current
  ([#5619](https://github.com/open-telemetry/opentelemetry-java/pull/5619))

#### Metrics

* Add LongHistogramAdviceConfigurer to improve api surface types
  ([#5689](https://github.com/open-telemetry/opentelemetry-java/pull/5689))
* Instruments with names which are case-insensitive equal contribute to same
  metric, advice is not part of instrument identity.
  ([#5701](https://github.com/open-telemetry/opentelemetry-java/pull/5701))

#### Exporter

* Add OtlpHttp{Signal}Exporter#toBuilder() methods
  ([#5652](https://github.com/open-telemetry/opentelemetry-java/pull/5652))
* Add OtlpGrpc{Signal}Exporter#toBuilder() methods
  ([#5680](https://github.com/open-telemetry/opentelemetry-java/pull/5680))
* Add #toString to OTLP exporters
  ([#5686](https://github.com/open-telemetry/opentelemetry-java/pull/5686))
* Break out GrpcSender, GrpcSenderProvider
  ([#5617](https://github.com/open-telemetry/opentelemetry-java/pull/5617))

#### SDK Extensions

* BREAKING: Delete zpages
  ([#5611](https://github.com/open-telemetry/opentelemetry-java/pull/5611))
* Initialize file configuration with generated model classes and parse method
  ([#5399](https://github.com/open-telemetry/opentelemetry-java/pull/5399))
* Refactor SpiUtil to improve mocking
  ([#5679](https://github.com/open-telemetry/opentelemetry-java/pull/5679))
* Switch from snakeyaml to snakeyaml engine
  ([#5691](https://github.com/open-telemetry/opentelemetry-java/pull/5691))
* Add experimental autoconfigure support for customizing cardinality limit
  ([#5659](https://github.com/open-telemetry/opentelemetry-java/pull/5659))
* Reorganize autoconfigure docs by signal
  ([#5665](https://github.com/open-telemetry/opentelemetry-java/pull/5665))

#### Testing

* Add hasResourceSatisfying to LogRecordDataAssert
  ([#5690](https://github.com/open-telemetry/opentelemetry-java/pull/5690))

### OpenCensus Shim

* Change OpenCensus shim default sampling to defer to OpenTelemetry
  ([#5604](https://github.com/open-telemetry/opentelemetry-java/pull/5604))

### Project Tooling

* Update Gradle Wrapper from 8.2 to 8.2.1
  ([#5618](https://github.com/open-telemetry/opentelemetry-java/pull/5618))
* Fix gradle java version requirement warning
  ([#5624](https://github.com/open-telemetry/opentelemetry-java/pull/5624))
* Refer to Adoptium/Temurin instead of AdoptOpenJDK
  ([#5636](https://github.com/open-telemetry/opentelemetry-java/pull/5636))
* Use OtelVersionClassPlugin instead of reading version from resource
  ([#5622](https://github.com/open-telemetry/opentelemetry-java/pull/5622))
* Enforce build-graal success in required status check
  ([#5696](https://github.com/open-telemetry/opentelemetry-java/pull/5696))

## Version 1.28.0 (2023-07-07)

[opentelemetry-sdk-extension-autoconfigure](./sdk-extensions/autoconfigure) is now stable! See "SDK
Extension" notes below for changes made prior to stabilization.

### SDK

#### Metrics

* Make Advice proper immutable class
  ([#5532](https://github.com/open-telemetry/opentelemetry-java/pull/5532))
* Show attributes when async instruments record duplicate measurements
  ([#5542](https://github.com/open-telemetry/opentelemetry-java/pull/5542))
* After cardinality limit exceeded record measurements to overflow series
  ([#5560](https://github.com/open-telemetry/opentelemetry-java/pull/5560))

#### Exporter

* Add HttpSender abstraction with OkHttp implementation
  ([#5505](https://github.com/open-telemetry/opentelemetry-java/pull/5505))
* Add HttpSenderProvider SPI
  ([#5533](https://github.com/open-telemetry/opentelemetry-java/pull/5533))
* Add JDK 11+ HttpClient HttpSender implementation
  ([#5557](https://github.com/open-telemetry/opentelemetry-java/pull/5557))
* Remove unnecessary :exporter:otlp:common dependencies
  ([#5535](https://github.com/open-telemetry/opentelemetry-java/pull/5535))
* Fix OTLP exporter artifact name in an error message
  ([#5541](https://github.com/open-telemetry/opentelemetry-java/pull/5541))
* Stabilize RetryPolicy API for OTLP exporters
  ([#5524](https://github.com/open-telemetry/opentelemetry-java/pull/5524))
* DEPRECATION: `opentelemetry-exporter-jaeger` and `opentelemetry-exporter-jaeger-thrift` are now
  deprecated with the last release planned for 1.34.0 (January 2024)
  ([#5190](https://github.com/open-telemetry/opentelemetry-java/pull/5190))

#### SDK Extensions

* Mark opentelemetry-sdk-extension-autoconfigure as stable
  ([#5577](https://github.com/open-telemetry/opentelemetry-java/pull/5577))
* Refactor autoconfigure registerShutdownHook(boolean) to disableShutdownHook()
  ([#5565](https://github.com/open-telemetry/opentelemetry-java/pull/5565))
* AutoConfiguredOpenTelemetrySdkBuilder does not set GlobalOpenTelemetry by default
  ([#5564](https://github.com/open-telemetry/opentelemetry-java/pull/5564))
* Add public API to autoconfigure to access environment resource
  ([#5554](https://github.com/open-telemetry/opentelemetry-java/pull/5554))
* Move autoconfigure getConfig to internal, remove getResource
  ([#5467](https://github.com/open-telemetry/opentelemetry-java/pull/5467))
* Add autoconfigure support for low memory metric temporality setting
  ([#5558](https://github.com/open-telemetry/opentelemetry-java/pull/5558))
* DEPRECATION: zpages extension from opentelemetry-sdk-extension-incubator
  is now deprecated
  ([#5578](https://github.com/open-telemetry/opentelemetry-java/pull/5578))

### Project tooling

* Publish build scans to ge.opentelemetry.io
  ([#5510](https://github.com/open-telemetry/opentelemetry-java/pull/5510))
* Update Gradle Wrapper from 8.0.1 to 8.1.1
  ([#5531](https://github.com/open-telemetry/opentelemetry-java/pull/5531))
* Add action to auto update gradle wrapper
  ([#5511](https://github.com/open-telemetry/opentelemetry-java/pull/5511))

## Version 1.27.0 (2023-06-09)

The log bridge API / SDK are now stable! Some important notes:

* The contents of `opentelemetry-api-logs` have been merged into `opentelemetry-api`.
* The contents of `opentelemetry-exporter-otlp-logs` have been merged
  into `opentelemetry-exporter-otlp`.
* The contents of `opentelemetry-sdk-logs-testing` have been merged into `opentelemetry-sdk-testing`.
* The `opentelemetry-sdk-logs` artifact has been marked stable.
* `opentelemetry-sdk-extension-autoconfigure` has changed the default value
  of `otel.logs.exporter` from `none` to `otlp`.

NOTE: reminder that
the [Logs Bridge API](https://github.com/open-telemetry/opentelemetry-specification/blob/v1.21.0/specification/logs/bridge-api.md)
is _not_ meant for end users. Log appenders use the API to bridge logs from existing log
frameworks (e.g. JUL, Log4j, SLf4J, Logback) into OpenTelemetry. Users configure the Log SDK to
dictate how logs are processed and exported.
See [opentelemetry.io](https://opentelemetry.io/docs/languages/java/api/#loggerprovider) for
documentation on usage.

### API

* Promote log API to stable
  ([#5341](https://github.com/open-telemetry/opentelemetry-java/pull/5341))
* fix doc for OpenTelemetry class
  ([#5454](https://github.com/open-telemetry/opentelemetry-java/pull/5454))

### SDK

* Ensure correct compiled output and sources are included in multi version jar
  ([#5487](https://github.com/open-telemetry/opentelemetry-java/pull/5487))

#### Logs

* Fix broken link
  ([#5451](https://github.com/open-telemetry/opentelemetry-java/pull/5451))
* Add meaningful `.toString` to `NoopLogRecordProcessor` and `DefaultOpenTelemetry`
  ([#5493](https://github.com/open-telemetry/opentelemetry-java/pull/5493))
* Promote log SDK to stable
  ([#5341](https://github.com/open-telemetry/opentelemetry-java/pull/5341))

#### Metrics

* Reset exponential aggregator scale after collection
  ([#5496](https://github.com/open-telemetry/opentelemetry-java/pull/5496))
* Experimental metric reader and view cardinality limits
  ([#5494](https://github.com/open-telemetry/opentelemetry-java/pull/5494))

#### Exporter

* Merge otlp logs
  ([#5432](https://github.com/open-telemetry/opentelemetry-java/pull/5432))
* Append unit to prometheus metric names
  ([#5400](https://github.com/open-telemetry/opentelemetry-java/pull/5400))

#### Testing

* Merge sdk logs testing
  ([#5431](https://github.com/open-telemetry/opentelemetry-java/pull/5431))
* Add a `hasBucketBoundaries()` variant that allows specifying precision
  ([#5457](https://github.com/open-telemetry/opentelemetry-java/pull/5457))

### SDK Extensions

* Enable otlp logs by default in autoconfigure
  ([#5433](https://github.com/open-telemetry/opentelemetry-java/pull/5433))

### Semantic Conventions

* Update to semconv 1.20.0
  ([#5497](https://github.com/open-telemetry/opentelemetry-java/pull/5497))

## Version 1.26.0 (2023-05-05)

This release represents the release candidate ("RC") release for the Logs Bridge API / SDK. In the
next release (1.27.0), `opentelemetry-api-logs` will be merged into `opentelemetry-api`,
`opentelemetry-sdk-logs` will be marked as stable, `opentelemetry-exporter-otlp-logs` will be
merged into `opentelemetry-exporter-otlp`, `opentelemetry-sdk-logs-testing` will be merged
into `opentelemetry-sdk-testing`, `opentelemetry-sdk-extension-autoconfigure` will enable `otlp`
log exporter by default (i.e. `otel.logs.exporter=otlp`). For more details, see tracking
issue [#5340](https://github.com/open-telemetry/opentelemetry-java/issues/5340). NOTE: reminder that
the [Logs Bridge API](https://github.com/open-telemetry/opentelemetry-specification/blob/v1.21.0/specification/logs/bridge-api.md)
is _not_ meant for end users. Log appenders use the API to bridge logs from existing log
frameworks (e.g. JUL, Log4j, SLf4J, Logback) into OpenTelemetry. Users configure the Log SDK to
dictate how logs are processed and exported.

`opentelemetry-opentracing-shim` is now stable!

### SDK

* Create OtelVersion class at build time which is used to resolve artifact version in `Resource`.
  ([#5365](https://github.com/open-telemetry/opentelemetry-java/pull/5365))

#### Metrics

* Add prototype histogram advice API (i.e. Hints).
  ([#5217](https://github.com/open-telemetry/opentelemetry-java/pull/5217))

#### Logs

* Add LogRecord observed timestamp field.
  ([#5370](https://github.com/open-telemetry/opentelemetry-java/pull/5370))
* Remove log record timestamp default.
  ([#5374](https://github.com/open-telemetry/opentelemetry-java/pull/5374))
* Align BatchLogRecordProcessor defaults with specification.
  ([#5364](https://github.com/open-telemetry/opentelemetry-java/pull/5364))
* Rename setEpoch to setTimestamp.
  ([#5368](https://github.com/open-telemetry/opentelemetry-java/pull/5368))
* Log SDK cleanup. Move `InMemoryLogRecordExporter` to `opentelemetry-sdk-logs-testing`.
  Rename `InMemoryLogRecordExporter#getFinishedLogItems` to `getFinishedLogRecordItems`.
  Move `SdkEventEmitterProvder` to internal package.
  ([#5368](https://github.com/open-telemetry/opentelemetry-java/pull/5368))

### Exporters

* Add scaffolding for low level exporter TLS API.
  ([#5362](https://github.com/open-telemetry/opentelemetry-java/pull/5362))
* Add new low level TLS APIs on OTLP and Jaeger gRPC exporter builders.
  ([#5280](https://github.com/open-telemetry/opentelemetry-java/pull/5280),
   [#5422](https://github.com/open-telemetry/opentelemetry-java/pull/5422))
* OTLP LogRecord exporters serialize observed timestamp.
  ([#5382](https://github.com/open-telemetry/opentelemetry-java/pull/5382))
* Update prometheus test to reflect new collector behavior.
  ([#5417](https://github.com/open-telemetry/opentelemetry-java/pull/5417))
* Prometheus exporter checks metrics name to prevent add duplicated _total suffix.
  ([#5308](https://github.com/open-telemetry/opentelemetry-java/pull/5308))
* Add additional OTLP test for authenticator.
  ([#5391](https://github.com/open-telemetry/opentelemetry-java/pull/5391))

### OpenTracing Shim

* Mark opentracing-shim as stable.
  ([#5371](https://github.com/open-telemetry/opentelemetry-java/pull/5371))

### SDK Extensions

* Fixes jaeger remote sampler service strategies bug resolving service name.
  ([#5418](https://github.com/open-telemetry/opentelemetry-java/pull/5418))
* Fix flaky JaegerRemoteSamplerGrpcNettyTest.
  ([#5385](https://github.com/open-telemetry/opentelemetry-java/pull/5385))
* Add new log level TLS APIs on JaegerRemoteSamplerBuilder.
  ([#5422](https://github.com/open-telemetry/opentelemetry-java/pull/5422))
* Fix a parameter name typo in autoconfigure-spi module.
  ([#5409](https://github.com/open-telemetry/opentelemetry-java/pull/5409))

### Semantic Conventinos

* Add missing links to deprecated constants in SemanticAttributes.
  ([#5406](https://github.com/open-telemetry/opentelemetry-java/pull/5406))

### Project Tooling

* Update stale workflow.
  ([#5381](https://github.com/open-telemetry/opentelemetry-java/pull/5381))
* Skip OWASP dependencyCheck on test modules.
  ([#5383](https://github.com/open-telemetry/opentelemetry-java/pull/5383))
* Skip OWASP dependencyCheck on jmh tasks.
  ([#5384](https://github.com/open-telemetry/opentelemetry-java/pull/5384))
* Drop create website pull request release step
  ([#5361](https://github.com/open-telemetry/opentelemetry-java/pull/5361))

## Version 1.25.0 (2023-04-07)

### API

* Cache ImmutableKeyValuePairs#hashCode
  ([#5307](https://github.com/open-telemetry/opentelemetry-java/pull/5307))

#### Propagators

* Remove streams from B3Propagator
  ([#5326](https://github.com/open-telemetry/opentelemetry-java/pull/5326))

### SDK

#### Metrics

* Stop validating instrument unit
  ([#5279](https://github.com/open-telemetry/opentelemetry-java/pull/5279))
* Make the Executor for PrometheusHttpServer configurable
  ([#5296](https://github.com/open-telemetry/opentelemetry-java/pull/5296))

#### Exporter

* Fix marshaler self suppression error
  ([#5318](https://github.com/open-telemetry/opentelemetry-java/pull/5318))
* Add sdk dependency to Logging OTLP exporter
  ([#5291](https://github.com/open-telemetry/opentelemetry-java/pull/5291))

#### Testing

* Fixing up javadoc to reflect how to create a junit4 OpenTelemetryRule
  ([#5299](https://github.com/open-telemetry/opentelemetry-java/pull/5299))

### SDK Extensions

* BREAKING: Autoconfigure drops support
  for `otel.exporter.otlp.metrics.default.histogram.aggregation=EXPONENTIAL_BUCKET_HISTOGRAM`.
  Use `BASE2_EXPONENTIAL_BUCKET_HISTOGRAM` instead.
  ([#5290](https://github.com/open-telemetry/opentelemetry-java/pull/5290))
* JaegerRemoteSampler use upstream grpc implementation if ManagedChannel is set
  ([#5287](https://github.com/open-telemetry/opentelemetry-java/pull/5287))

### OpenTracing Shim

* Adds version to otel tracer instrumentation scope
  ([#5336](https://github.com/open-telemetry/opentelemetry-java/pull/5336))

### OpenCensus Shim

* Adds version to otel tracer instrumentation scope
  ([#5336](https://github.com/open-telemetry/opentelemetry-java/pull/5336))

### Semantic Conventions

* Update semconv to 1.19.0 and related build tool changes
  ([#5311](https://github.com/open-telemetry/opentelemetry-java/pull/5311))

## Version 1.24.0 (2023-03-10)

### SDK

#### Metrics

* Optimize DefaultSynchronousMetricStorage iteration to reduce allocations
  ([#5183](https://github.com/open-telemetry/opentelemetry-java/pull/5183))
* Avoid exemplar allocations if there are no measurements
  ([#5182](https://github.com/open-telemetry/opentelemetry-java/pull/5182))
* Remove boxed primitives from aggregations to reduce allocations
  ([#5184](https://github.com/open-telemetry/opentelemetry-java/pull/5184))
* Stop ignoring long measurements in HistogramExemplarReservoir
  ([#5216](https://github.com/open-telemetry/opentelemetry-java/pull/5216))
* Remove validations for noop instrument names and units
  ([#5146](https://github.com/open-telemetry/opentelemetry-java/pull/5146))
* Allow views to select on instrument unit
  ([#5255](https://github.com/open-telemetry/opentelemetry-java/pull/5255))

#### Exporter

* Add (internal) TlsConfigHelper for additional TLS configurability
  ([#5246](https://github.com/open-telemetry/opentelemetry-java/pull/5246))

#### SDK Extensions

* Introduce mTLS support for JaegerRemoteSamplerBuilder (#5209)
  ([#5248](https://github.com/open-telemetry/opentelemetry-java/pull/5248))

### OpenTracing Shim

* OpenTracing Shim: Update Tracer.close()
  ([#5151](https://github.com/open-telemetry/opentelemetry-java/pull/5151))

* Update version to 1.24.0
  ([#5198](https://github.com/open-telemetry/opentelemetry-java/pull/5198))
* Post release 1.23.0
  ([#5202](https://github.com/open-telemetry/opentelemetry-java/pull/5202))

### OpenCensus Shim

* Addresses opencensus-shim trace issues under otel javaagent
  ([#4900](https://github.com/open-telemetry/opentelemetry-java/pull/4900))

### Project tooling

* Cleanup readmes
  ([#5263](https://github.com/open-telemetry/opentelemetry-java/pull/5263))
* Upgrade to gradle 8.0.1
  ([#5256](https://github.com/open-telemetry/opentelemetry-java/pull/5256))
* Fixed example resource provider classname.
  ([#5235](https://github.com/open-telemetry/opentelemetry-java/pull/5235))
* Fix case of bug label in open issue workflow
  ([#5268](https://github.com/open-telemetry/opentelemetry-java/pull/5268))

### Semantic Conventions

* Update semconv to 1.19.0
  ([#5311](https://github.com/open-telemetry/opentelemetry-java/pull/5311))

## Version 1.23.1 (2023-02-15)

* Fix bug that broke `AutoConfiguredOpenTelemetrySdk`'s shutdown hook.
  ([#5221](https://github.com/open-telemetry/opentelemetry-java/pull/5221))

## Version 1.23.0 (2023-02-10)

This release is a notable release for metrics:

* The base2 exponential histogram aggregation has been marked as stable. To use, configure
  your `MetricExporter` with a default aggregation
  of `Aggregation.base2ExponentialBucketHistogram()` for histogram instruments. If using OTLP
  exporter with autoconfigure,
  set `OTEL_EXPORTER_OTLP_METRICS_DEFAULT_HISTOGRAM_AGGREGATION=BASE2_EXPONENTIAL_BUCKET_HISTOGRAM`.
  If using configuring OTLP programatically,
  use `Otlp*MetricExporterBuilder#setDefaultAggregationSelector(DefaultAggregationSelector)`.
* The metrics SDK undergone significant internal refactoring which results in reduced complexity and
  significantly reduced memory allocation during metric collection / export.

### API

#### Logs

* WARNING: Split out Event API from Log API. Event API is now published in `opentelemetry-api-events`.
  API / SDK usage has also changed - see PR for details.
  ([#5049](https://github.com/open-telemetry/opentelemetry-java/pull/5049))

### SDK

* Add shutdown / close to `OpenTelemetrySdk`
  ([#5100](https://github.com/open-telemetry/opentelemetry-java/pull/5100))

#### Metrics

* Base2 exponential histogram aggregations are now stable. Add `base2ExponentialBucketHistogram()`
  to `Aggregation`.
  ([#5143](https://github.com/open-telemetry/opentelemetry-java/pull/5143))
* Promote exponential histogram data interfaces to stable package
  ([#5120](https://github.com/open-telemetry/opentelemetry-java/pull/5120))
* Add Base2 prefix to internal exponential histogram classes
  ([#5179](https://github.com/open-telemetry/opentelemetry-java/pull/5179))
* Add MaxScale config parameter to `Base2ExponentialBucketHistogram`
  ([#5044](https://github.com/open-telemetry/opentelemetry-java/pull/5044))
* Add close method to `MetricReader`
  ([#5109](https://github.com/open-telemetry/opentelemetry-java/pull/5109))
* Reuse `AggregatorHandle` with cumulative temporality to reduce allocations
  ([#5142](https://github.com/open-telemetry/opentelemetry-java/pull/5142))
* Delete notion of accumulation to reduce allocations
  ([#5154](https://github.com/open-telemetry/opentelemetry-java/pull/5154))
* Delete bound instruments
  ([#5157](https://github.com/open-telemetry/opentelemetry-java/pull/5157))
* Reuse aggregation handles for delta temporality to reduce allocations
  ([#5176](https://github.com/open-telemetry/opentelemetry-java/pull/5176))

#### Exporter

* Add proper shutdown implementations for all exporters
  ([#5113](https://github.com/open-telemetry/opentelemetry-java/pull/5113))

#### SDK Extensions

* WARNING: Remove deprecated autoconfigure exemplar filter names. Previous names `none`, `all`
  , `with_sampled_trace` have been removed. Use `always_off`, `always_on`, `trace_based` instead.
  ([#5098](https://github.com/open-telemetry/opentelemetry-java/pull/5098))
* Add autoconfigure support for "none" option for propagator value
  ([#5121](https://github.com/open-telemetry/opentelemetry-java/pull/5121))
* Add autoconfigure support for `parentbased_jaeger_remote` sampler
  ([#5123](https://github.com/open-telemetry/opentelemetry-java/pull/5123))
* Autoconfigure closes up autoconfigured resources in case of exception
  ([#5117](https://github.com/open-telemetry/opentelemetry-java/pull/5117))
* View file based config has switched from snakeyaml to snakeyaml-engine for YAML parsing.
  ([#5138](https://github.com/open-telemetry/opentelemetry-java/pull/5138))
* Fix autoconfigure resource providers property docs
  ([#5135](https://github.com/open-telemetry/opentelemetry-java/pull/5135))

#### Testing

* WARNING: Merge `opentelemetry-sdk-metrics-testing` into `opentelemetry-sdk-testing`. Stop
  publishing `opentelemetry-sdk-metrics-testing`.
  ([#5144](https://github.com/open-telemetry/opentelemetry-java/pull/5144))
* Sort spans by start time (parents before children as tiebreaker) to avoid common causes for flaky
  tests
  ([#5026](https://github.com/open-telemetry/opentelemetry-java/pull/5026))
* Add resource assertion methods to `SpanDataAssert` and `MetricAssert`
  ([#5160](https://github.com/open-telemetry/opentelemetry-java/pull/5160))


### Semantic Conventions

* Update semconv to 1.18.0
  ([#5188](https://github.com/open-telemetry/opentelemetry-java/pull/5188))
  ([#5134](https://github.com/open-telemetry/opentelemetry-java/pull/5134))

### OpenTracing Shim

* Refactor to remove internal objects `BaseShimObject` and `TelemetryInfo`
  ([#5087](https://github.com/open-telemetry/opentelemetry-java/pull/5087))
* WARNING: Minimize public surface area of OpenTracingShim. Remove `createTracerShim()`
  , `createTracerShim(Tracer)`, `createTracerShim(Tracer, OpenTracingPropagators)`.
  Add `createTracerShim(TracerProvder,TextMapPropagator,TextMapPropagator)`.
  ([#5110](https://github.com/open-telemetry/opentelemetry-java/pull/5110))

### Project tooling

* Add OWASP dependency check
  ([#5177](https://github.com/open-telemetry/opentelemetry-java/pull/5177))

## Version 1.22.0 (2023-01-06)

### API

* WARNING: GlobalOpenTelemetry trigger of autoconfiguration is now opt-in.
  Previously, `GlobalOpenTelemetry.get` triggered autoconfiguration
  if `opentelemetry-sdk-extension-autoconfigure` was detected on the classpath. That behavior is now
  opt-in by setting environment variable `OTEL_JAVA_GLOBAL_AUTOCONFIGURE_ENABLED=true`.
  ([#5010](https://github.com/open-telemetry/opentelemetry-java/pull/5010))
* Update LoggerBuilder, MeterBuilder, TracerBuilder javadoc
  ([#5050](https://github.com/open-telemetry/opentelemetry-java/pull/5050))

#### Context

* Make closing scope idempotent and non-operational when corresponding context is not current.
  [(#5061)](https://github.com/open-telemetry/opentelemetry-java/pull/5061)

### SDK

* Standardize internal usage of `ConfigUtil` for reading environment variables and system properties
  ([#5048](https://github.com/open-telemetry/opentelemetry-java/pull/5048))

#### Metrics

* Lazily initialize exponential histogram buckets
  ([#5023](https://github.com/open-telemetry/opentelemetry-java/pull/5023))
* Delete MapCounter alternative exponential histogram implementation
  ([#5047](https://github.com/open-telemetry/opentelemetry-java/pull/5047))
* Add toString to SdkMeter, SdkObservableInstrument, AbstractInstrumentBuilder
  ([#5072](https://github.com/open-telemetry/opentelemetry-java/pull/5072))

#### Exporter

* `OtlpGrpcSpanExporter`, `OtlpHttpSpanExporter`, `OtlpGrpcLogRecordExporter`,
  `OtlpHttpLogRecordExporter`, `ZipkinSpanExporter`, and `JaegerGrpcSpanExporter` are now
  instrumented with `GlobalOpenTelemetry` by default. Instrumentation initializes lazily to prevent
  ordering issue of accessing `GlobalOpenTelemetry.get` before `GlobalOpenTelemetry.set` is called.
  ([#4993](https://github.com/open-telemetry/opentelemetry-java/pull/4993))
* Add `ConfigurableSpanExporterProvider` implementation for `JaegerGrpcSpanExporter`
  ([#5002](https://github.com/open-telemetry/opentelemetry-java/pull/5002))
* Add `ConfigurableSpanExporterProvider`, `ConfigurableMetricExporterProvider`,
  `ConfigurableLogRecordExporterProvider` for `OtlpGrpc{Signal}Exporter`s
  and `OtlpHttp{SignalExporter`s
  ([#5003](https://github.com/open-telemetry/opentelemetry-java/pull/5003))
* Replace OTLP User-Agent spaces with dashes
  ([#5080](https://github.com/open-telemetry/opentelemetry-java/pull/5080))
* Add `AutoConfigurationCustomizerProvider` implementation for `PrometheusHttpServer`
  ([#5053](https://github.com/open-telemetry/opentelemetry-java/pull/5053))
* Add resource `target_info` and scope `target_info` metrics to `PrometheusHttpServer` in compliance
  with spec
  ([#5039](https://github.com/open-telemetry/opentelemetry-java/pull/5039))
* Drop delta metrics in `PrometheusHttpServer`
  ([#5062](https://github.com/open-telemetry/opentelemetry-java/pull/5062))
* PrometheusHttpServer drops metrics with same name and different type
  ([#5078](https://github.com/open-telemetry/opentelemetry-java/pull/5078))

#### SDK Extensions

* DEPRECATION: Align autoconfigure exemplar filter names with spec. Previous names `none`, `all`,
  `with_sampled_trace` are deprecated. Use `always_off`, `always_on`, `trace_based` instead.
  ([#5063](https://github.com/open-telemetry/opentelemetry-java/pull/5063))

### OpenTracing Shim

* Add createTracerShim function
  ([#4988](https://github.com/open-telemetry/opentelemetry-java/pull/4988))

## Version 1.21.0 (2022-12-09)

### API

### API Extensions

* WARNING: `opentelemetry-extension-aws` has been removed following its relocation
  to [opentelemetry-java-contrib/aws-xray-propagator](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-xray-propagator),
  which is published under
  coordinates `io.opentelemetry.contrib:opentelemetry-aws-xray-propagator:{version}`.
  We will push security patches to `1.20.x` as needed. The latest `opentelemetry-bom` will point
  to the latest published version, currently `1.20.1`.
  ([#4945](https://github.com/open-telemetry/opentelemetry-java/pull/4945))
* Add toString implementation to various propagator components
  ([#4996](https://github.com/open-telemetry/opentelemetry-java/pull/4996))

### SDK

#### Logs

* Add toString implementation to various log SDK components
  ([#4979](https://github.com/open-telemetry/opentelemetry-java/pull/4979))

#### Metrics

* Add histogram collection benchmark
  ([#4912](https://github.com/open-telemetry/opentelemetry-java/pull/4912))
* Add ExponentialHistogramIndexerBenchmark
  ([#4989](https://github.com/open-telemetry/opentelemetry-java/pull/4989))
* Stop extra copy of exponential histogram buckets
  ([#5020](https://github.com/open-telemetry/opentelemetry-java/pull/5020))

#### Exporter

* Zipkin exporter serializes EventData attributes as JSON
  ([#4934](https://github.com/open-telemetry/opentelemetry-java/pull/4934))
* Add support for EC mTLS keys (applies to `OtlpHttp{Signal}Exporter`, `OtlpGrpc{Signal}Exporter`,
  `JaegerGrpcSpanExporter`, `JaegerRemoteSampler`)
  ([#4920](https://github.com/open-telemetry/opentelemetry-java/pull/4920))
* Add `Configurable{Signal}ExporterProvider` implementations for `Logging{Signal}Exporter`s
  ([#4950](https://github.com/open-telemetry/opentelemetry-java/pull/4950))
* Add `ConfigurableSpanExporterProvider` implementation for `ZipkinSpanExporter`
  ([#4991](https://github.com/open-telemetry/opentelemetry-java/pull/4991))
* Add `Configurable{Signal}ExporterProvider` implementations for `OtlpJsonLogging{Signal}Exporter`s
  ([#4992](https://github.com/open-telemetry/opentelemetry-java/pull/4992))
* `ZipkinSpanExporter` populates remoteEndpoint
  ([#4933](https://github.com/open-telemetry/opentelemetry-java/pull/4933))

#### SDK Extensions

* BREAKING: Remove support for otel.experimental.sdk.enabled from autoconfigure
  ([#4973](https://github.com/open-telemetry/opentelemetry-java/pull/4973))
* De-singleton ZPageServer implementation
  ([#4935](https://github.com/open-telemetry/opentelemetry-java/pull/4935))
* Add auto-configure support for logging-otlp exporters
  ([#4879](https://github.com/open-telemetry/opentelemetry-java/pull/4879))
* Move DefaultConfigProperties to internal package in `opentelemetry-sdk-extension-autoconfigure-spi`
  ([#5001](https://github.com/open-telemetry/opentelemetry-java/pull/5001))

#### Testing

* Make APIs for asserting attributes consistent
  ([#4882](https://github.com/open-telemetry/opentelemetry-java/pull/4882))
* Attribute assertions error messages always contain the attr key
  ([#5027](https://github.com/open-telemetry/opentelemetry-java/pull/5027))

### Semantic Conventions

* Update semantic and resource attributes for spec v1.16.0
  ([#4938](https://github.com/open-telemetry/opentelemetry-java/pull/4938),
   [#5033](https://github.com/open-telemetry/opentelemetry-java/pull/5033))

### OpenTracing Shim

* Handle unsupported types when setting Attributes
  ([#4939](https://github.com/open-telemetry/opentelemetry-java/pull/4939))
* Properly set the status based on the error tag
  ([#4962](https://github.com/open-telemetry/opentelemetry-java/pull/4962))
* Handle `io.opentracing.noop.NoopSpan` correctly
  ([#4968](https://github.com/open-telemetry/opentelemetry-java/pull/4968))
* Log invalid arguments rather than throwing exceptions.
  ([#5012](https://github.com/open-telemetry/opentelemetry-java/pull/5012))
* Stop mapping semconv values from OpenTracing to OTel.
  ([#5016](https://github.com/open-telemetry/opentelemetry-java/pull/5016))

## Version 1.20.1 (2022-11-15)

### Bugfixes

* Fix bug in `ComponentRegistry` that produces `ConcurrentModificationException` when reading
  metrics at the same time as obtaining a meter.
  [(#4951)](https://github.com/open-telemetry/opentelemetry-java/pull/4951)

## Version 1.20.0 (2022-11-11)

### API

- Fix bug in `W3CBaggagePropagator` that caused `+` characters to be decoded as whitespace ` `.
  [(#4898)](https://github.com/open-telemetry/opentelemetry-java/pull/4898)

#### API Extensions

* DEPRECATION: the `opentelemetry-extension-aws` module containing
  various `AwsXrayPropagator` implementations has been deprecated for removal in next minor version.
  A copy of the code will instead be maintained
  in [opentelemetry-java-contrib/aws-xray-propagator](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-xray-propagator)
  and published under
  coordinates `io.opentelemetry.contrib:opentelemetry-aws-xray-propagator:{version}`.
  [(#4862)](https://github.com/open-telemetry/opentelemetry-java/pull/4862)

### SDK

#### Traces

* Add graal hints for shaded dependencies, allowing `JcTools` queue to be used in graal environment.
  [(#4832)](https://github.com/open-telemetry/opentelemetry-java/pull/4832)
* `Sampler#getDescription()` implementations are now locale independent.
  [(#4877)](https://github.com/open-telemetry/opentelemetry-java/pull/4887)
* Allow SDK to run in environments prohibiting use of `sun.misc.Unsafe`.
  [(#4902)](https://github.com/open-telemetry/opentelemetry-java/pull/4902)

#### Metrics

* Add `toString` to `AbstractInstrument`.
  [(#4833)](https://github.com/open-telemetry/opentelemetry-java/pull/4883)
* Add zero bucket boundary to default explicit bucket histogram aggregation.
  [(#4819)](https://github.com/open-telemetry/opentelemetry-java/pull/4819)

#### Logs

* Optimize log hot path, reducing allocations significantly.
  [(#4913)](https://github.com/open-telemetry/opentelemetry-java/pull/4913)
* BREAKING: Add `Context` argument to `LogRecordProcessor#onEmit`.
  [(#4889)](https://github.com/open-telemetry/opentelemetry-java/pull/4889)

#### Exporter

* `OtlpLogging{Signal}Exporter`s encode enums as numbers.
  [(#4783)](https://github.com/open-telemetry/opentelemetry-java/pull/4783)
* Add `User-Agent` header of `OTel OTLP Exporter Java/{version}` to OTLP export requests.
  [(#4784)](https://github.com/open-telemetry/opentelemetry-java/pull/4784)

#### SDK Extensions

* WARNING: `opentelemetry-sdk-extension-aws` has been removed following its relocation
  to [opentelemetry-java-contrib/instrumentation-aws-xray](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-xray),
  which is published under
  coordinates `io.opentelemetry.contrib:opentelemetry-aws-resources:{version}`.
  We will push security patches to `1.19.x` as needed. The latest `opentelemetry-bom` will point
  to the latest published version, currently `1.19.0`.
  [(#4830)](https://github.com/open-telemetry/opentelemetry-java/pull/4830)
* WARNING: `opentelemetry-sdk-extension-resources` has been removed following its relocation
  to [opentelemetry-java-instrumentation/instrumentation/resources/library](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources/library),
  which is published under
  coordinates `io.opentelemetry.instrumentation:opentelemetry-resources:{version}`.
  We will push security patches to `1.19.x` as needed. The latest `opentelemetry-bom` will point
  to the latest published version, currently `1.19.0`.
  [(#4828)](https://github.com/open-telemetry/opentelemetry-java/pull/4828)
* Add autoconfigure support for `BatchLogRecordProcessor`.
  [(#4811)](https://github.com/open-telemetry/opentelemetry-java/pull/4811)
* Autoconfigure performs percent decoding on `otel.resource.attributes` values.
  [(#4653)](https://github.com/open-telemetry/opentelemetry-java/issues/4653)
* Unify compression configuration for exporters including
  [(#4775)](https://github.com/open-telemetry/opentelemetry-java/pull/4775):
  * Fix handling of `none` in OTLP exporters.
  * Add `JaegerGrpcSpanExporterBuilder#setCompression(String)`.
  * Add `ZipkinSpanExporterBuilder#setCompression(String)`.

### Semantic Conventions

* Add migration notes to deprecated attributes
  [(#4840)](https://github.com/open-telemetry/opentelemetry-java/pull/4840)

### OpenTracing Shim

* Use `opentracing-shim` as instrumentation scope name.
  [(#4890)](https://github.com/open-telemetry/opentelemetry-java/pull/4890)
* Add full support for multiple parents.
  [(#4916)](https://github.com/open-telemetry/opentelemetry-java/pull/4916)

## Version 1.19.0 (2022-10-07)

This release contains a large number of changes to the log signal following a series of significant
changes to
the [log specification](https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/logs).
The changes include renaming key classes like `LogExporter` to `LogRecordExporter`,
and `LogProcessor` to `LogRecordProcessor`, and `LogEmitter` to `Logger`, and more. Additionally, a
log API component has been added for emitting events and for writing log appenders. Note, the log
API is not a substitute for traditional log frameworks like Log4j, JUL, SLF4J, or Logback. While the
event portion of the API is intended for instrumentation authors and end users, the API for emitting
LogRecords is not.
See [LoggerProvider](./api/all/src/main/java/io/opentelemetry/api/logs/LoggerProvider.java)
and [Logger](./api/all/src/main/java/io/opentelemetry/api/logs/Logger.java) javadoc for more
details.

### General

* Add `opentelemetry-bom` as a dependency to `opentelemetry-bom-alpha`, ensuring synchronization
  between alpha and stable artifact versions.

### API

#### API Extensions

* WARNING: `opentelemetry-extension-annotations` has been removed following its relocation
  to [opentelemetry-java-instrumentation/instrumentation-annotations](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation-annotations),
  which is published under
  coordinates `io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:{version}`.
  We will push security patches to `1.18.x` as needed. The latest `opentelemetry-bom` will point
  to the latest published version, currently `1.18.0`.

#### Logs

* Introduce log API for emitting events and writing log appenders. The artifact is available at
  coordinates `io.opentelemetry:opentelemetry-api-logs:1.19.0-alpha`.

### SDK

#### Metrics

* Change exponential histogram bucket boundaries to be lower exclusive / upper inclusive, instead of
  lower inclusive / upper exclusive.

#### Logs

* BREAKING: Rename `SdkLogEmitterProvider` to `SdkLoggerProvider`.
  `OpenTelemetrySdkBuilder#setLogEmitterProvider` has changed
  to `OpenTelemetrySdkBuilder#setLoggerProvider`. `OpenTelemetrySdk#getSdkLogEmitterProvider` has
  changed to `OpenTelemetrySdk#getSdkLoggerProvider`.
  `AutoConfigurationCustomizer#addLogEmitterProviderCustomizer` has changed
  to `AutoConfigurationCustomizer#addLoggerProviderCustomizer`.
* BREAKING: Rename `LogEmitter` to `Logger`.
* BREAKING: Rename `LogExporter` to `LogRecordExporter`. `SystemOutLogExporter` has changed
  to `SystemOutLogRecordExporter`. `OtlpJsonLoggingLogExporter` has changed
  to `OtlpJsonLoggingLogRecordExporter`. `OtlpHttpLogExporter` has changed
  to `OtlpHttpLogRecordExporter`. `OtlpGrpcLogExporter` has changed to `OtlpGrpcLogRecordExporter`.
  `InMemoryLogExporter` has changed to `InMemoryLogRecordExporter`.
  ConfigurableLogExporterProvider` has changed to `ConfigurableLogRecordExporterProvider`.
* BREAKING: Rename `LogData` to `LogRecordData`. `TestLogData` has changed to `TestLogRecordData`.
* BREAKING: Rename `LogProcessor` to `LogRecordProcessor`. `BatchLogProcessor` has changed
  to `BatchLogRecordProcessor`. `SimpleLogProcessor` has changed to `SimpleLogRecordProcessor`.

#### Exporter

* OTLP log record exporters now
  include [dropped_attributes_count](https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/logs/v1/logs.proto#L157).

#### SDK Extensions

* Autoconfigure now supports an option to disable the SDK. If `otel.sdk.disabled=true`,
  `AutoConfiguredOpenTelemetrySdk#getOpenTelemetrySdk()` returns a minimal (but not
  noop) `OpenTelemetrySdk` with noop tracing, metric and logging providers. The same minimal
  instance is set to `GlobalOpenTelemetry`. The now deprecated
  property `otel.experimental.sdk.enabled` will continue to work in the same way during a transition
  period.
* Fix `ProcessResource` directory separator to use `/` or `\` instead of `:` or `;`.
* DEPRECATION: the `opentelemetry-sdk-extension-resource` module containing
  various `ResourceProvider` implementations has been deprecated for removal in next major version.
  A copy of the code will instead be maintained
  in [opentelemetry-java-instrumentation/instrumentation/resources/library](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/resources/library)
  and published under
  coordinates `io.opentelemetry.instrumentation:opentelemetry-resources:{version}`.
* DEPRECATION: the `opentelemetry-sdk-extension-aws` module containing AWS `ResourceProvider`
  implementations has been deprecated for removal in next major version. A copy of the code will
  instead be maintained
  in [opentelemetry-java-contrib/aws-resources](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-resources)
  and published under coordinates `io.opentelemetry.contrib:opentelemetry-aws-resources:{version}`.

### OpenTracing Shim

* Fully support Baggage-only propagation.

### Semantic conventions

* The semantic conventions have been updated to 1.13.0.

## Version 1.18.0 (2022-09-09)

### SDK

* Added scope attributes to `InstrumentationScopeInfo` accessible
  via `InstrumentationScopeInfo#getAttributes()`. Will add the ability to specify scope attributes
  in Meter, Tracer, and Logger in a future version.
* DEPRECATION: The `InstrumentationScopeInfo#create(String, String, String)` method has been
  deprecated in favor of
  `InstrumentationScopeInfo#builer(String).setVersion(String).setSchemaUrl(String).build()`.
* Optimize `Resource#merge(Resource)` by returning early if the other resource is empty.

#### Logs

* Fix module name of `opentelemetry-sdk-logs` by changing
  from `io.opentelemetry.sdk.extension.logging` to `io.opentelemetry.sdk.logs`.

#### Testing

* Add methods to assert attributes do not contain keys via `AttributeAssert#doesNotContainKey()`.

#### Exporter

* Added ability to specify local IP address in `ZipkinSpanExporter`
  via `ZipkinSpanExporterBuilder#setLocalIpAddressSupplier(Supplier<InetAddress>)`.
* Upgrade to OTLP protobuf version 0.19.0.
* OTLP exporters now serialize `InstrumentationScopeInfo#getAttributes()`.
* Stop publishing `opentelemetry-exporter-jaeger-proto`. The `opentelemetry-bom` will include a
  constraint on the last published version `1.17.0`. If security issues are discovered, patches will
  be published to `1.17.x`.

#### SDK Extensions

* BREAKING: `opentelemetry-sdk-extension-metric-incubator`,
  `opentelemetry-sdk-extension-tracing-incubator`, and `opentelemetry-sdk-extension-zpages` merged
  into `opentelemetry-sdk-extension-incubator`.
* BREAKING: Move `opentelemetry-sdk-extension-jfr-events`
  to [opentelemetry-java-contrib/jfr-events](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/jfr-events).
  It will now be published under the
  coordinates `io.opentelemetry.contrib:opentelemetry-jfr-events:{version}`.
* BREAKING: Move `opentelemetry-extension-noop-api`
  to [opentelemetry-java-contrib/noop-api](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/noop-api).
  It will now be published under the
  coordinates `io.opentelemetry.contrib:opentelemetry-noop-api:{version}`.
* Improve ECS resource detection to include `aws.ecs.container.arn`, `container.image.name`,
  `container.image.tag`, `aws.ecs.container.image.id`, `aws.log.group.arns`, `aws.log.group.names`,
  `aws.log.steam.names`, `aws.ecs.task.arn`, `aws.ecs.task.family`, and `aws.ecs.task.revision`.
* Fix resource `container.id` detection when using k8s with containerd v1.5.0+.
* Add experimental `ConditionalResourceProvider` SPI, for conditionally applying resource providers
  based on current config and resource.

### Micrometer shim

* BREAKING: Move `opentelemetry-micrometer1-shim`
  to [opentelemetry-java-instrumentation/instrumentation/micrometer/micrometer-1.5/library](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/micrometer/micrometer-1.5/library).
  It will now be published under the
  coordinates `io.opentelemetry.instrumentation:opentelemetry-micrometer-1.5:{version}`.

## Version 1.17.0 (2022-08-12)

### API

#### API Extensions

* DEPRECATION: the `opentelemetry-extension-annotations` module containing `@WithSpan`
  and `@SpanAttribute` annotations has been deprecated for removal in next major version. A copy of
  the code will instead be maintained
  in [opentelemetry-java-instrumentation/instrumentation-annotations](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation-annotations)
  and published under
  coordinates `io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:{version}`.

### SDK

#### Traces

* Add default implementation for `SpanData#getInstrumentationScopeInfo()`
  and `ReadableSpan#getInstrumentationScopeInfo()`. This fixes a previous mistake where those
  interfaces were extended without default implementation, a breaking change for source
  compatibility. Stricter checks have been added to ensure this mistake is not repeated.

#### Logs

* BREAKING: delete `LogDataBuilder`. A similar implementation of `LogData` called `TestLogData` has
  been added to `opentelemetry-sdk-logs-testing`.
* BREAKING: rename `LogProcessor#emit(LogData)` to `LogProcessor#onEmit(ReadWriteLogRecord)`. The
  argument change from `LogData` to `ReadWriteLogRecord` allows implementations to mutate logs. To
  obtain `LogData`, call `ReadWriteLogRecord#toLogData()`.
* Optimize `SdkLogEmitterProvider` to return noop `LogEmitter` when no `LogProcessor`s are
  registered.

#### Exporter

* Split out shared and internal exporter classes from `opentelemetry-exporter-otlp-common`
  to `opentelemetry-exporter-common`.
* Add experimental support for OTLP header based authentication. To use, add a dependency
  on `opentelemetry-exporter-common` and
  call `io.opentelemetry.exporter.internal.auth.Authenticator#setAuthenticatorOnDelegate(OtlpHttp{Signal}Builder, Authenticator)`.
* Add ability to collect export metrics on `ZipkinSpanExporter`
  via `ZipkinSpanExporter#setMeterProvider(MeterProvider)`.
* Minor optimization to OkHttp based exporters to cache endpoint URLs. Applies
  to `OtlpHttp{Signal}Exporter`, `OtlpGrpc{Signal}Exporter`, and more.
* Fix diagnostic log message in `OtlpGrpc{Signal}Exporter` to include correct environment variables.

#### SDK Extensions

* Extend View file based configuration with support for specifying explicit bucket histogram bucket
  boundaries and exponential bucket counts.
* Extend autoconfigure SPI `AutoConfigurationCustomizerProvider` and `ResourceProvider` with option
  to specify ordering.
* Add autoconfigure SPI with `ConfigurableLogExporterProvider`, allowing custom named log exporters
  to be provided and selected via autoconfigure.
* Extend autoconfigure SPI with `AutoConfigurationCustomizer#addPropertiesCustomizer`, providing the
  ability examine current configuration properties and add / overwrite properties.

## Version 1.16.0 (2022-07-13)

### API

* Fix bug `ImmutableKeyValuePairs` implementation that causes `ArrayIndexOutOfBoundsException` to be
  thrown under certain conditions.

### SDK

#### Traces

* Optimize `BatchSpanProcessor` using JcTools.

#### Metrics

* Tighten up exponential histogram implementation for alignment with specification: Default to 160
  positive and negative buckets. Remove ability to configure starting scale. Minimum number of
  buckets is one instead of zero.
* Allow `MetricExporter` and `MetricReader` to influence default aggregation. The default
  aggregation is used when no registered views match an instrument.

#### Exporter

* Fix handling of client keys in PEM format.
* For OTLP exporters, change behavior to use `OkHttpGrpcExporter` (OkHttp implementation that
  doesn't use any gRPC dependencies) unless `OtlpGrpc{Signal}Builder#setChannel(ManagedChannel)` is
  called by user. Previously, `OkHttpGrpcExporter` was used if no gRPC implementation was found on
  classpath.
* Add support to configure default aggregation on OTLP metric exporters
  via `Otlp{Protocol}MetricExporterBuilder#setDefaultAggregationSelector(DefaultAggregationSelector)`.

#### Testing

* Add span status assertions.

#### SDK Extensions

* Autoconfigure properly handles non-string system properties.
* Autoconfigure normalizes hyphens `-` to periods `.` when accessing `ConfigProperties`.

### OpenTracing Shim

* Add support for span wrappers.
* Store OpenTracing `SpanContext` in OpenTracing `Span` wrapper.
* Use `Baggage` of active span.

## Version 1.15.0 (2022-06-10)

### API

* Add batch callback API, allowing a single callback to record measurements to multiple metric
  instruments.

### SDK

#### Metrics

* `SdkMeterProvider#toString()` now returns a useful string describing configuration.
* Fix bug preventing proper function of Metrics SDK when multiple readers are
  present ([#4436](https://github.com/open-telemetry/opentelemetry-java/pull/4436)).
* Fix reporting intervals for metrics for delta
  readers ([#4400](https://github.com/open-telemetry/opentelemetry-java/issues/4400)).

#### Exporter

* BREAKING: merge all stable OTLP exporters into `opentelemetry-exporter-otlp`.
  `opentelemetry-exporter-otlp-trace`, `opentelemetry-exporter-otlp-metrics`,
  `opentelemetry-exporter-otlp-http-trace`, and `opentelemetry-exporter-otlp-http-metrics` are no
  longer published and their contents have been merged into a single artifact.
* BREAKING: merge log OTLP exporters into `opentelemetry-exporter-otlp-logs`.
  `opentelemetry-exporter-otlp-http-logs` is no longer published and its contents have been merged
  into a single artifact.
* Upgrade to OTLP protobuf version 0.18.0.
* RetryInterceptor retries on `SocketTimeoutException` with no message.
* Added `JaegerGrpcSpanExporterBuilder#setMeterProvider()`, enabling support of experimental jaeger
  span export metrics.
* DEPRECATION: the `opentelemetry-exporter-jaeger-proto` module containing jaeger proto definitions
  and corresponding generated classes is deprecated for removal in next major version.
* OTLP gRPC exporters support overriding `:authority`
  via `OtlpGrpc*ExporterBuilder#addHeader("host", "my-authority-override")`.

#### SDK Extensions

* BREAKING: Move `ConfigureableMetricExporterProvider`
  from `opentelemetry-sdk-extension-autoconfigure` to
  stable `opentelemetry-sdk-extension-autoconfigure-spi`.
* Autoconfigure now supports multiple values for `otel.metrics.exporter`.
* Autoconfigure now
  supports [general attribute limits](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#attribute-limits),
  applicable to span attributes, span event attributes, span link attributes, and log attributes.
* Autoconfigure now supports an experimental option to disable the SDK.
  If `otel.experimental.sdk.enabled=true`, `AutoConfiguredOpenTelemetrySdk#getOpenTelemetrySdk()`
  returns a minimal (but not noop) `OpenTelemetrySdk`. The same minimal instance is set
  to `GlobalOpenTelemetry`.
* New "get or default" methods have been added to `ConfigProperties`.
  E.g. `ConfigProperties#getString("otel.metrics.exporter", "otlp")` fetches the value for the
  property `otel.metrics.exporter` and returns `otlp` if it is not set.
* Fix bug in `ContainerResource` provider that caused it to throw an exception in some instances
  when containerd is used.

### Micrometer shim

* Cache descriptions such that metrics with the same name use the first seen description.

## Version 1.14.0 (2022-05-09)

The metrics SDK is stable! New stable artifacts include:

* `io.opentelemetry:opentelemetry-sdk-metrics` (also now included
  in `io.opentelemetry:opentelemetry-sdk`)
* `io.opentelemetry:opentelemetry-exporter-otlp-metrics` (also now included
  in `io.opentelemetry:opentelemetry-exporter-otlp`)
* `io.opentelemetry:opentelemetry-exporter-otlp-http-metrics`
* Metrics testing components have been moved
  from `io.opentelemetry:opentelemetry-sdk-metrics-testing` to the
  stable `io.opentelemetry:opentelemetry-sdk-testing`.

While the API of the metrics SDK is now stable, there are a couple of known issues that will be
addressed in the next release:

* The start time is incorrect for delta metrics when the first recording for a set of attributes
  occurs after the first
  collections ([#4400](https://github.com/open-telemetry/opentelemetry-java/issues/4400)).
* Registering multiple readers results in incorrect
  metrics ([#4436](https://github.com/open-telemetry/opentelemetry-java/pull/4436)).

### SDK

#### Traces

* Fix bug where non-runtime exception breaks `BatchSpanProcessor`.
* Fix bug preventing attribute limits from applying to exception events.

#### Logs

* BREAKING: Drop deprecated methods referencing `InstrumentationLibraryInfo` from Log SDK.

#### Metrics

* Instrument name is validated. If invalid, a warning is logged and a noop instrument is returned.
* Default unit is empty instead of `1`. If an invalid unit is set, a warning is logged and empty is
  used.
* Ensure symmetry between type of `PointData` and their type of exemplars (double or long).
* BREAKING: Rename `MetricReader#flush()` to `MetricReader#forceFlush()`.
* Introduce `AggregationTemporalitySelector` interface for selecting aggregation temporality based
  on instrument. `MetricReader` and `MetricExporter` extend `AggregationTemporalitySelector`.

#### SDK Extensions

* BREAKING: Remove deprecated option to specify temporality
  with `otel.exporter.otlp.metrics.temporality`.
  Use `otel.exporter.otlp.metrics.temporality.preference` instead.
* Log warning when `AwsXrayPropagator` can't identify parent span id.
* Fix jaeger remote sampling bug preventing correct parsing of 0-probability sampling strategies.

#### Exporter

* Fix prometheus exporter formatting bug when there are no attributes.
* Ensure prometheus metrics with the same name are serialized as a group.
* BREAKING: `OtlpHttpMetricExporterBuilder` and `OtlpGrpcMetricExporterBuilder` configure
  aggregation temporality via `#setAggregationTemporalitySelector(AggregationTemporalitySelector)`.

#### Testing

* BREAKING: Metrics testing components added to stable `io.opentelemetry:opentelemetry-sdk-testing`
  module, including `InMemoryMetricReader`, `InMemoryMetricExporter`,
  and `MetricAssertions.assertThat(MetricData)` has been moved
  to `OpenTelemetryAssertions.assertThat(MetricData)`.
* BREAKING: The patterns for metrics assertions have been adjusted to better align with assertj
  conventions. See [#4444](https://github.com/open-telemetry/opentelemetry-java/pull/4444) for
  examples demonstrating the change in assertion patterns.
* BREAKING: Metric assertion class names have
  been [simplified](https://github.com/open-telemetry/opentelemetry-java/pull/4433).
* Add `TraceAssert.hasSpansSatisfyingExactlyInAnyOrder(..)` methods.

### Micrometer shim

* Instrumentation scope name changed to `io.opentelemetry.micrometer1shim`.

### Project tooling

* Many improvements to the build and release workflows. Big thanks to @trask for driving
  standardization across `opentelemetry-java`, `opentelemetry-java-instrumentation`,
  and `opentelemetry-java-contrib`!

## Version 1.13.0 (2022-04-08)

Although we originally intended 1.13.0 to be the first stable release of the metrics SDK, we've
postponed it out of caution due to a large number of changes in both the metrics SDK specification
and the java implementation. This release should be considered a release candidate for the metrics
SDK. There are several notable changes mentioned in the Metrics section. Additionally, please note
that the Auto-configuration module now enables metric export by default via OTLP, i.e. by
default `otel.metrics.exporter` is set to `otlp` instead of `none`.

### API

* Fix `TraceStateBuilder` reuse bug.

### SDK

* `InstrumentationScopeInfo` replaces `InstrumentationLibraryInfo`. Methods
  returning `InstrumentationLibraryInfo` are deprecated.
* Add `ResourceBuilder#removeIf(Predicate)` method for symmetry with `AttributesBuilder`.

#### Traces

* Span events that record exceptions are instances of `ExceptionEventData`.

#### Metrics

* BREAKING: Remove `MetricReader` factory pattern:
  * `MetricReader` instances, rather than `MetricReaderFacotry`, are registered
    with `SdkMeterProviderBuilder`. For
    example: `SdkMeterProvider.builder().registerMetricReader(PeriodicMetricReader.builder(exporter).build())`.
  * `MetricReader` does not support custom implementations. Built-in readers
    include: `PeriodicMetricReader`, `PrometheusHttpServer`, and for testing, `InMemoryMetricReader`.
* BREAKING: Several breaking changes metrics to the `Data` classes:
  * `MetriaData` returns `InstrumentationScopeInfo` instead of `InstrumentationLibraryInfo`.
  * `MetricData` factories classes have been moved internal.
  * Exemplar data classes have been migrated to interfaces, and deprecated methods have been
    removed.
  * PointData classes have been migrated to interfaces.
  * `ValueAtPercentile` has been converted to `ValueAtQuantile` to reflect specification.
  * Drop `HistogramPointData` utility methods for finding bucket bounds.
* BREAKING: Move `InstrumentType` and `InstrumentValueType` to `io.opentelemetry.sdk.metrics`
  package.
* BREAKING: Several breaking changes to the `InstrumentSelector` / `View` APIs:
  * `InstrumentSelector` / `View` and corresponding builders have been moved
    to `io.opentelemetry.sdk.metrics` package.
  * `InstrumentSelector` meter selection criteria has been inlined and `MeterSelector` has been
    removed.
  * `InstrumentSelector` criteria has been reduced to reflect specification. All fields are exact
    match, except instrument name which accepts wildcard `*` and `?` matches.
  * If `InstrumentSelectorBuilder#build()` is called without any selection criteria, an exception
    will be thrown.
  * `View` baggage appending attribute processor has been removed. Available for experimental use
    via `SdkMeterProviderUtil#appendFilteredBaggageAttributes`.
  * Concept of `AttributeProcessor` has been moved internal.
  * If a View configures an aggregation that is not compatible with the instrument type of a
    selected instrument, a warning is logged and the View is ignored.
* BREAKING: Remove deprecated `Aggregation#histogram()`. Use `Aggregation#explicitBucketHistogram()`
  instead.
* Relax behavior around conflicting instruments. If you register two instruments with the same
  name but conflicting description, unit, type, or value type, both will be exported and a warning
  will be logged indicating the metric identity conflict. Previously, the second registered would
  have produced a noop instrument. Likewise, if two views are registered that produce instruments
  with conflicting names, or if an instrument conflicts with a registered view's name, both will be
  exported and a warning will be logged indicating the view conflict.
* BREAKING: Exemplars have been moved to internal. By default, exemplars are enabled
  with `with_sampled_trace` filter. This can be adjusted via experimental APIs
  via `SdkMeterProviderUtil#setExemplarFilter`.
* BREAKING: `MetricExporter#getPreferredTemporality()` has been removed and replaced
  with `getAggregationTemporality(InstrumentType)`, which allows exporters to dictate the
  aggregation temporality on a per-instrument
  basis. `MetricExporter#alwaysCumulative(InstrumentType)`
  and `MetricExporter#deltaPreferred(Instrument)` are provided as utilities representing common
  configurations.
* Callbacks associated with asynchronous instruments with multiple matching views will only be
  called once per collection, instead of once per view per collection.
* `PeriodicMetricReader` will no longer call `MetricExporter#export` if no metrics are available.
* BREAKING: `SdkMeterProviderBuilder#setMinimumCollectionInterval` has been removed. Available for
  experimental use via `SdkMeterProviderUtil#setMinimumCollectionInterval`.
* Introduce lock ensuring that metric collections occur sequentially.
* Add min and max to `HistogramDataPoint`.

#### Logs

* BREAKING: Deprecated name field has been removed.

#### Exporter

* Upgrade to OTLP protobuf version 0.16.0.
* Jaeger and Zipkin exporters export `otel.scope.name` and `otel.scope.version`, in addition
  to `otel.library.name` and `otel.library.version` which are retained for backwards compatibility.
* BREAKING: Remove deprecated `PrometheusCollector`. Use `PrometheusHttpServer` instead.
* Add support for mTLS authentication to OTLP and jaeger exporters.
* Only log once if OTLP gRPC export receives `UNIMPLEMENTED`.
* Jaeger remote sampler sets appropriate sampling strategy type if not provided in response.
* BREAKING: The `setPreferredTemporality` method has been removed
  from `OtlpGrpcMetricExporterBuilder` and `OtlpHttpMetricExporterBuilder`.
  Use `setAggregationTemporality(Function<InstrumentType, AggregationTemporality>)` instead.

#### SDK Extensions

* IMPORTANT: Auto-configuration sets `otel.metrics.exporter` to `otlp` instead of `none`, enabling
  metric export by default.
* Auto-configuration added `otel.java.enabled.resource-providers` property for opting into specific
  resource providers.

### Micrometer shim

* Bring micrometer shim over from `opentelemetry-java-instrumentation`. Artifact is available at
  maven coordinates `io.opentelemetry:opentelemetry-micrometer1-shim:1.13.0-alpha`.
* Add support for "prometheus mode", enabling better naming when exporting micrometer metrics via
  prometheus.

#### Testing

* Add int overload for equalTo attribute assertion.
* Add `SpanDataAssert.hasAttribute` methods.

## Version 1.12.0 (2022-03-03)

This release includes many breaking changes to the metrics SDK as we move towards marking its first stable release.
Notably, if you configure metric `View`s or have written a custom metric exporter, many of the classes and methods will
have been moved or renamed. There are still a few remaining cleanups targeted for the next release after which there
should not be many. Thanks for bearing with us on this.

### API

- New methods have been added to `Context` to propagate context for common Java 8 callback types
- `AttributesBuilder.put` now supports vararg versions for lists with `AttributeKey`
- Multiple metric async callbacks can be registered for the same instrument, and callbacks can be removed

### SDK

- An issue with Android desugaring of the SDK has been worked around
- EXPERIMENTAL: Support for disabling resource keys with `OTEL_EXPERIMENTAL_RESOURCE_DISABLED_KEYS`
- Fixed handling of `schemaUrl` in `Resource.toBuilder()`
- BREAKING: Many changes to `Data` classes used during export
- BREAKING: Many view configuration methods have been removed

#### Metrics

- APIs deprecated in the previous release have been removed
- DEPRECATION: `PrometheusCollector` for exporting OpenTelemetry metrics with the prometheus client library has been deprecated
- EXPERIMENTAL: File-based configuration of views
- Prometheus exporter now supports JPMS modules

#### Logs

- DEPRECATION: `LogData.getName` has been deprecated for removal

## Version 1.11.0 (2022-02-04)

### General

* Examples moved
  to [opentelemetry-java-examples](https://github.com/open-telemetry/opentelemetry-java-examples)

### SDK

#### Exporter

* Switch Jaeger remote sampler to use grpc lite
* Deprecate `.setChannel(ManagedChannel)` methods on OTLP gRPC exporters
* Deprecate `.setChannel(ManagedChannel)` methods on Jaeger gRPC exporter
* Experimental OTLP retry support now retries on connection timeouts

#### Metrics

* BREAKING Change: Deprecated `InMemoryMetricExporter` and `InMemoryMetricReader` have been removed.
  Use versions in `opentelemetry-sdk-metrics-testing` instead
* Deprecate `InstrumentType` values `OBSERVABLE_SUM` and `OBSERVABLE_UP_DOWN_SUM` in favor
  of `OBSERVABLE_COUNTER` and `OBSERVABLE_UP_DOWN_COUNTER`

#### Logs

* Add ability to configure log attribute limits via `SdkLogEmitterProviderBuilder#setLogLimits(..)`

#### SDK Extensions

* Auto-configuration added options to `AutoConfigurationCustomizer` for
  customizing `SdkTracerProviderBuilder`, `SdkMeterProviderBuilder`, `MetricExporter`
  , `SdkLogEmitterProviderBuilder`, and `LogExporter`
* Auto-configuration added option to skip shutdown hooks
* Auto-configuration adjusted the execution order of tracer and meter provider customization to
  happen after autoconfiguration
* Auto-configuration adjusted SPI factories to evaluate lazily
* Auto-configuration now uses sets configured `SdkMeterProvider` on `BatchLogProcessor`
  and `BatchSpanProcessor`
* Auto-configuration deprecated `SdkTracerProviderConfigurer` in favor
  of `AutoConfigurationCustomizer#addTracerProviderCustomizer(..)`

## Version 1.10.1 (2022-01-21)

### Bugfixes

* Fix issue preventing registration of PrometheusCollector with SDK
* Allow retry policy to be set for OkHttpGrpcExporter

## Version 1.10.0 (2022-01-07)

### API

* Performance of `GlobalOpenTelemetry.get` improved
* `buildWithCallback` for asynchronous metrics now return interfaces instead of void. The interfaces
  are empty but will allow adding functionality in the future
* BREAKING CHANGE: `Double/LongMeasurement.observe` have been removed
* BREAKING CHANGE: `GlobalMeterProvider` has been removed
* BREAKING CHANGE: `ObservableMeasurement`, an empty interface, has been removed. This type was not
  previously deprecated but is expected to have no use in apps due to the lack of functionality
* The Metrics API has been merged into the `opentelemetry-api`
  artifact. `OpenTelemetry.getMeterProvider()` is the new entrypoint
* BREAKING CHANGE: Bound metrics instruments have been removed for now to allow more time to bake
  while still providing a stable metrics API
* `Double/LongMeasurement.observe` has been renamed to `record`.`observe` is deprecated in this
  release
* `GlobalMeterProvider` has been deprecated. `GlobalOpenTelemetry.getMeterProvider` should be used
  instead
* A warning is logged now when registering async instruments with the same name. Previously
  subsequent invocations were just ignored.
* `GlobalOpenTelemetry` extended with helpers for meter creation

### SDK

* The semantic conventions have been updated to 1.8.0
* Deprecated methods have been removed from the `opentelemetry-sdk-autoconfigure` artifact.

#### Exporter

* The OkHttp gRPC exporters now support experimental retry
* OkHttp dispatcher threads are now spawned as daemon threads
* The JPMS module name for the logs exporter has been fixed
* Metrics exporters can have temporality configured
* HTTP exporters now support experimental retry
* Jaeger exporter allows setting trusted certificates
* gRPC exporter metric typos corected

#### Metrics

* `InMemoryMetricExporter` has been moved to the `opentelemetry-sdk-metrics-testing` artifact. The
  current class has been deprecated in this release
* Metric instrument usage violations consistently report issues as debug logs
* Some user callbacks have been wrapped to catch exceptions instead of throwing
* MinMaxSumCount/Count aggregation has been removed
* Empty aggregator is renamed to `drop`
* Cumulative aggregations will not be aggressively dropped every collection cycle

#### Logs

* A `opentelemetry-sdk-logs-testing` module has been added
* `SdkLogEmitterProvider` is now available through `OpenTelemetrySdk`
* LogDataBuilder can now take a SpanContext directly
* SdkLogEmitterProvider.get convenience method added

#### AWS

* HTTP requests now use OkHttp instead of the JDK

### OpenCensus Shim

* Shim span attributes are set before the span is created instead of after
* Exceptions are not thrown when activating a null span

#### SDK Extensions

* BREAKING CHANGE: Deprecated trace incubator types (DelegatingSpanData, SpanDataBuidler) have been
  removed
* BREAKING CHANGE: Deprecated `ExecutorServiceSpanProcessor` has been removed
* `cloud.platform` is now populated in AWS `Resource`
* Auto-configuration correctly uses configured class loader for configuring Resource
* Auto-configuration prints a debug log with the resolved tracer configuration
* Auto-configuration supports the logs signal

## Version 1.9.1 (2021-11-23)

### Bugfixes

- In Prometheus exporter, only populate exemplars for Prometheus types that support them
- Fix proto encoding of oneof values in metrics
- Correctly cleanup OkHttp client resources when shutting down exporters

## Version 1.9.0 (2021-11-11)

### General

- IMPORTANT: The deprecated `io.opentelemetry:opentelemetry-proto` module was removed. Java bindings for OTLP
  protobufs are now published
  via [opentelemetry-proto-java](https://github.com/open-telemetry/opentelemetry-proto-java), and
  available at maven coordinates `io.opentelemetry.proto:opentelemetry-proto`.

### API

- New `AttributesBuilder#remove(String)` and `AttributeBuilder#removeIf(Predicate<AttributeKey<?>>)`
  methods improve ergonomics of modifying attributes.
- `W3CBaggagePropagator` now encodes baggage values in URL encoded UTF-8 format, per
  the [W3C Baggage Specification](https://w3c.github.io/baggage/).

### SDK

- `DelegatingSpanData` has been promoted from incubation and is now available in the Trace SDK.
  The `DelegatingSpanData` class in
  the `io.opentelemetry:opentelemetry-sdk-extension-tracing-incubator` module is now deprecated.

#### Exporters

- The prometheus metric exporter now includes the `time_unix_nano` representing the epoch timestamp
  when collection occurred.
- The OTLP `grpc` exporters (`OtlpGrpcSpanExporter`, `OtlpGrpcLogExporter`,
  and `OtlpGrpcMetricExporter`) now include a default client implementation (`okhttp`). If a `grpc`
  implementation is detected on the classpath it will be used, but the exporters now work "out of
  the box" with no additional dependencies.

#### SDK Extensions

- IMPORTANT: The deprecated `io.opentelemetry:opentelemetry-sdk-extension-async-processor`
  module was removed. This module is now published
  via [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib),
  and available at maven coordinates `io.opentelemetry.contrib:opentelemetry-disruptor-processor`.
- The `ExecutorServiceSpanProcessor` from
  the `io.opentelemetry:opentelemetry-sdk-extension-tracing-incubator` module is now deprecated.

#### Logging (alpha)

- This release includes a rework of the Log SDK to
  implement [OTEP-0150](https://github.com/open-telemetry/oteps/blob/main/text/logs/0150-logging-library-sdk.md)
  and to have more symmetry to the Trace SDK. `LogSink` is now `LogEmitter`. `LogEmitter` instances
  are obtained from `SdkLogEmitterProvider`. Other additions include `MultiLogProcessor` (accessed
  via `LogProcessor#composite(...)`), `SimpleLogProcessor`, `InMemoryLogExporter`
  , `OtlpJsonLoggingLogExporter`, and `SystemOutLogExporter`.
- The Log SDK maven coordinates have changed
  from `io.opentelemetry:opentelemetry-sdk-extension-logging`
  to `io.opentelemetry:opentelemetry-sdk-logs`.

### Metrics (alpha)

- The `new InMemoryMetricReader()` constructor has been deprecated.
  Use `InMemoryMetricReader.create()` instead.
- A typo in `Aggregation.explictBucketHistogram()` has been fixed, and is now accessible
  at `Aggregation.explicitBucketHistogram()`.
- The `PeriodicMetricReader#builder(MetricExporter)` builder
  replaces `PeriodicMetricReader#newMetricReaderFactory(MetricExporter, Duration)`.
- Aggregation temporality is now influenced by metric exporters, and the ability to configure
  aggregation temporality via the view API has been removed. For example, the OTLP metric exporters
  support both `DELTA` and `CUMULATIVE` temporality. `CUMULATIVE` is the default preferred, but this
  can be changed either via programmatic configuration or
  via `OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY` if using autoconfigure.
- The `MeterProvider#get(String instrumentationName, String instrumentationVersion, String schemaUrl)`
  method is deprecated. Use `MeterProvider#meterBuilder(String instrumentationName)` with
  corresponding builder setters instead.
- Metric cardinality defenses have been added. Each instrument view now can have at most 2000
  distinct metric streams per collection cycle. Recordings for additional streams are dropped with a
  diagnostic log message. Additionally, cumulative metric streams (both for synchronous and
  asynchronous instruments) are aggressively forgotten each time a metric export occurs that does
  not include recordings for a particular stream. The net effect is that there is now a cap on
  metric memory consumption.

### Auto-configuration (alpha)

- BREAKING CHANGE: Remove deprecated `otel.experimental.exporter.otlp.protocol`,
  `otel.experimental.exporter.otlp.{signal}.protocol` properties. Please use
  `otel.exporter.otlp.protocol`, `otel.exporter.otlp.{signal}.protocol` instead.
- The autoconfigure module has introduced a powerful new `AutoConfiguredOpenTelemetrySdkBuilder`,
  and SPI for programmatically configuring the builder with `AutoConfigurationCustomizerProvider`.
  This provides improved ergonomics and control around autoconfigure customization.
- Added experimental support for enabling OTLP retry support for the `grpc` exporters. If enabled
  via `otel.experimental.exporter.otlp.retry.enabled`,
  a [default retry policy](https://opentelemetry.io/docs/languages/java/configuration/#properties-exporters)
  will be used.
- The metric export interval of `PeriodicMetricReader` is now configured
  via `otel.metric.export.interval`. The existing `otel.imr.export.interval` property has been
  deprecated.
- The SPI classloader can now be specified when using the autoconfigure module programmatically.

## Version 1.7.1 (2021-11-03)

### Exporters:

- [BUGFIX](https://github.com/open-telemetry/opentelemetry-java/issues/3813): In 1.7.0, the
  okhttp-based exporters (`OtlpHttpSpanExporter`, `OtlpHttpMetricExporter`, `OtlpHttpLogExporter`)
  did not properly close the okhttp response and hence would leak connections. This has been fixed in
  1.7.1.

## Version 1.7.0 (2021-10-08)

### General

- IMPORTANT: The `io.opentelemetry:opentelemetry-proto` module should now be considered
  *deprecated*. It will be removed from publications in a future release. If you need Java bindings
  for the OTLP protobufs, they are now being published via the
  new [opentelemetry-proto-java](https://github.com/open-telemetry/opentelemetry-proto-java)
  repository. They are at new maven coordinates: `io.opentelemetry.proto:opentelemetry-proto` and
  versioning is aligned with the released version of the protobuf definitions themselves.

### SDK

#### Exporters

- BREAKING CHANGE: The Jaeger gRPC exporter does not directly use the `protobuf-java` library for
  marshaling trace data. Along with this, the `opentelemetry-exporter-jaeger` artifact does not
  contain generated protobuf classes for the Jaeger API. If you were using these in your
  application, you must update your build configuration to also include the new `jaeger-proto`
  artifact. This artifact will not be included in a future 2.0 release of the SDK so it is
  recommended to instead generated the protobuf classes in your own build.
- BREAKING CHANGE: The `opentelemetry-exporter-otlp-http-*` exporter default endpoint ports have
  changed from `4317` to `4318`, in line
  with [recent changes](https://github.com/open-telemetry/opentelemetry-specification/pull/1970) to
  the spec.
- The OTLP gRPC exporters will now function without the `grpc-java` dependency, if `okhttp` is
  present on the classpath.
- The (alpha) metrics that are generated by the gRPC exporters have changed slightly. They now have
  a slightly different instrumentation library name, `"io.opentelemetry.exporters.otlp-grpc"` and
  the names of the metrics have also changed. Now emitted are metrics with
  names `otlp.exporter.seen` and `otlp.exported.exported`. Note that it is likely this will change
  in the future as the metrics semantic conventions are more defined.

### Auto-configuration (alpha)

- BREAKING CHANGE: The behavior of `otel.exporter.otlp.endpoint` has changed when the protocol
  is `http/protobuf`. The new behavior is in line
  with [recent changes](https://github.com/open-telemetry/opentelemetry-specification/pull/1975) to
  the specification, which states that the signal path  (e.g. `v1/traces` or `v1/metrics`) is
  appended to the configured endpoint. Values for signal specific endpoint configuration (
  e.g. `otel.exporter.otlp.traces.endpoint` and `otel.exporter.otlp.metrics.endpoint`) override the
  generic endpoint configuration and are used as-is without modification.
- The `compression` option for exporters now explicitly supports the `none` value, in addition to the existing `gzip` value.

### Metrics (alpha)

- BREAKING CHANGE: The `IntervalMetricReader` has been removed, and replaced with
  a `PeriodicMetricReader` that provides an implementation of the new `MetricReader` interface.
- This release includes initial support for multiple exporters to be configured for a single SDK
  instance. See the `SdkMeterProviderBuilder.registerMetricReader` method for more details.
- This release includes initial support for the SDK recording of Metric Exemplars for sampled Spans.
  See `SdkMeterProviderBuilder.setExemplarFilter` and the `ExemplarFilter` interface for
  more details.

### Logging (alpha)

- This release includes SDK extension interfaces for `LogProcessor`s and `LogExporter`s, and has
  implementations for batch log processing and export via OTLP. These classes are intended for usage
  in implementations of log appenders that emit OTLP log entries.

## Version 1.6.0 (2021-09-13)

### API

- Various performance optimizations
  - 1 and 2 element Attributes instances now bypass some logic to reduce object allocations.
  - The result of `hashCode()` of `AttributeKey` is now cached.
  - Checks for base-16 validity of TraceId and SpanId have been optimized.
  - Internally created `SpanContext` instances now bypass unneeded validation.

### Semantic Conventions (alpha)

- The `SemanticAttributes` and `ResourceAttributes` classes have been updated to match the semantic
  conventions as of specification release `1.6.1`.

### SDK

- The `io.opentelemetry.sdk.trace.ReadableSpan` interface has been expanded to include
  a `getAttribute(AttributeKey)` method.
- The `io.opentelemetry.sdk.trace.SpanLimits` class now supports enforcing a maximum Span attribute
  length (measured in characters) on String and String-array values.

#### Exporters

- The OTLP exporters have been undergone a significant internal rework. Various performance
  optimizations have been done on process of converting to the OTLP formats.
- The OTLP metric exporter no longer exports the deprecated metric `Labels`, only `Attributes`. This
  means that your collector MUST support at least OTLP version `0.9.0` to properly ingest metric
  data.
- BREAKING CHANGE: The `OtlpHttpMetricExporter` class has been moved into
  the `io.opentelemetry.exporter.otlp.http.metrics` package.
- BUGFIX: The `OtlpGrpcSpanExporter` and `OtlpGrpcMetricExporter` will now wait for the underlying
  grpc channel to be terminated when shutting down.
- The OTLP exporters now optionally support `gzip` compression. It is not enabled by default.

#### SDK Extensions

- The `AwsXrayIdGenerator` in the `opentelemetry-sdk-extension-aws` module has been deprecated. This
  implementation has been superseded by the one in
  the [opentelemetry-java-contrib](https://github.com/open-telemetry/opentelemetry-java-contrib)
  project and will not be maintained here going forward.

### Auto-configuration (alpha)

- The `otel.traces.exporter`/`OTEL_TRACES_EXPORTER` option now supports a comma-separated list of
  exporters.
- The Metrics SDK will no longer be configured by default. You must explicitly request an exporter
  configuration in order to have a Metrics SDK configured.
- BREAKING CHANGE: All SPI interfaces are now in a separate module from the autoconfiguration
  module: `opentelemetry-sdk-extension-autoconfigure-spi`.
- BREAKING CHANGE: `ConfigProperties` and `ConfigurationException` have been moved to a new
  package (`io.opentelemetry.sdk.autoconfigure.spi`) and
  module (`opentelemetry-sdk-extension-autoconfigure-spi`).
- BREAKING CHANGE: All SPI interfaces now take a `ConfigProperties` instance on their methods.
- BUGFIX: Exceptions thrown during the loading of an SPI implementation class are now handled more
  gracefully and will not bubble up unless you have explicitly requested the bad implementation as
  the one to use.
- You can now specify `gzip` compress for the OTLP exporters via the `otel.exporter.otlp.compression`
  /`OTEL_EXPORTER_OTLP_COMPRESSION` configuration option.
- You can now specify maximum Span attribute length via the `otel.span.attribute.value.length.limit`
  /`OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT` configuration option.

### Metrics (alpha)

- BREAKING CHANGES: The metrics SDK has numerous breaking changes, both behavioral and in the SDK's
  configuration APIs.
  - The default aggregation for a `Histogram` instrument has been changed to be a Histogram, rather
    than a Summary.
  - Registration of Views has undergone significant rework to match the current state of the SDK
    specification. Please reach out on CNCF slack in
    the [#otel-java](https://cloud-native.slack.com/archives/C014L2KCTE3) channel, or in
    a [github discussion](https://github.com/open-telemetry/opentelemetry-java/discussions) if you
    need assistance with converting to the new Views API.
  - The OTLP exporter no longer exports the deprecated metric `Labels`, only `Attributes`. This
    means that your collector MUST support at least OTLP version `0.9.0` to properly ingest metric
    data.
  - It is no longer possible to provide custom aggregations via a View. This feature will return in
    the future.

## Version 1.5.0 (2021-08-13)

### API
- The `io.opentelemetry.context.ContextStorage` interface now allows providing a root `Context`.

### SDK
- The `io.opentelemetry.sdk.trace.samplers.SamplingResult` class has been enhanced with new factory methods for the static result values.
- The `io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter` now supports setting custom TLS certificates.
- The `io.opentelemetry.sdk.trace.ReadableSpan` interface now exposes the parent SpanContext directly.
- The `io.opentelemetry.sdk.resources.Resource` now exposes a `getAttribute(AttributeKey)` method to directly retrieve attributes.
- A new `opentelemetry-exporter-otlp-http-trace` module is now available to support OTLP over HTTP exports.

#### SDK Extensions
- The `opentelemetry-sdk-extension-resources` module now provides a new `ContainerResource` that auto-detects docker container Resource attributes.
- The Jaeger Remote Sampler in the `opentelemetry-sdk-extension-jaeger-remote-sampler` module is now `java.io.Closeable`.

#### Testing
- The SDK testing module (`opentelemetry-sdk-testing`) has been enhanced with additional assertions for Spans and Attributes.

### Auto-configuration (alpha)
- BREAKING CHANGE: `io.opentelemetry.sdk.autoconfigure.ConfigProperties` in the `opentelemetry-sdk-extension-autoconfigure` is now an interface
  and `io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration.initiatize()` now accepts an optional `ConfigProperties`
  instance to add properties to the standard auto-detected ones.
- BREAKING CHANGE: `OpenTelemetrySdkAutoConfiguration.getResource()` has been removed in favor of using the new `OpenTelemetryResourceAutoConfiguration` class.
- The `opentelemetry-sdk-extension-autoconfigure` module now exposes a new `OpenTelemetryResourceAutoConfiguration`
  class specifically for acquiring autoconfigured `Resource` instances.
- The `opentelemetry-sdk-extension-autoconfigure` module now provides an option to *not* set the GlobalOpenTelemetry instance when auto-configuring.
- The `opentelemetry-sdk-extension-autoconfigure` module now has support for signal-specific timeout, header and TLS certificate configuration.
- A new SPI option is available for configuring a metrics exporter. See `io.opentelemetry.sdk.autoconfigure.spi.ConfigurableMetricExporterProvider` for details.
- A new `OTEL_TRACES_SAMPLER`/`otel.traces.sampler` option is available: `jaeger_remote`.
  - It can be configured using the `OTEL_TRACES_SAMPLER_ARG`/`otel.traces.sampler.arg`, which is parsed as a comma-separated map.
    - For example `-Dotel.traces.sampler=jaeger_remote -Dotel.traces.sampler.arg=endpoint=192.168.1.5:14250,pollingInterval=5000,initialSamplingRate=0.01`

### Semantic Conventions (alpha)
- The `SemanticAttributes` and `ResourceAttributes` classes have been updated to match the semantic conventions
  as of specification release `1.5.0`.

### Metrics (alpha)
- BREAKING CHANGE: The Metrics API has been completely re-written to match the newly specified API.
Please reach out on CNCF slack in the [#otel-java](https://cloud-native.slack.com/archives/C014L2KCTE3) channel,
or in a [github discussion](https://github.com/open-telemetry/opentelemetry-java/discussions) if you need assistance with converting to the new API.
- A new `opentelemetry-exporter-otlp-http-metrics` module is now available to support OTLP over HTTP exports.

## Version 1.4.1 (2021-07-15)

- Fill labels in addition to attributes during OTLP metrics export to support versions of the
OpenTelemetry Collector which do not support the new protocol yet.

## Version 1.4.0 (2021-07-10)

### API
#### Enhancements
- You can now assign an OpenTelemetry schema URL to a `Tracer` via the new `TracerBuilder` class that is
accessed via the `TracerProvider` or any of the global instances that delegate to one.

#### Extensions
- A new `@SpanAttribute` annotation has been added for adding method parameters to spans automatically. This
has no implementation in this release, but should be supported by the auto-instrumentation agent soon.

### Exporters
#### Bugfixes
- Calling `shutdown()` multiple times on the OTLP and Jaeger GRPC-based exporters will now work correctly and return a proper
implementation of `CompletableResultCode` for the calls beyond the first.

### SDK
#### Bugfixes
- If the `jdk.unsupported` package is not available, the `BatchSpanProcessor` will now fall back to a supported, standard `Queue` implementation.

#### Enhancements
- A `Resource` can now be assigned an OpenTelemetry schema URL via the `ResourceBuilder` or the `create(Attributes, String)`
method on the `Resource` itself.
- You can now obtain a default `Clock` based on system time via `Clock.getDefault`. The sdk-testing artifact also provides
a `TestClock` for unit testing.

### Semantic Conventions (alpha)
- The `SemanticAttributes` and `ResourceAttributes` classes have been updated to match the semantic conventions
as of specification release `1.4.0`. These classes also now expose a `SCHEMA_URL` field which points at the
version of the OpenTelemetry schema the files were generated from. There are no breaking changes in this update, only additions.

### Metrics (alpha)
- You can now assign an OpenTelemetry schema URL to a `Meter` via the new `MeterBuilder` class that is
accessed via the `MeterProvider` or any global instances that delegate to one.
- The metrics SDK now utilizes `Attributes` rather than `Labels` internally.
- You can now register an `IntervalMetricReader` as global and `forceFlush` the global reader.

## Version 1.3.0 (2021-06-09)

### API
#### Enhancements
- Parsing of the W3C Baggage header has been optimized.

### SDK
#### Behavioral Changes
- The implementation of SpanBuilder will no longer throw exceptions when null parameters are passed in. Instead,
it will treat these calls as no-ops.

#### Enhancements
- Memory usage of the Tracing SDK has been greatly reduced when exporting via the OTLP or Jaeger exporters.
- The OTLP protobuf version has been updated to v0.9.0

### Extensions
- A new experimental extension module has been added to provide a truly no-op implementation of the API. This
is published under the `io.opentelemetry.extension.noopapi` name.
- The `io.opentelemetry.sdk.autoconfigure` module now supports the `OTEL_SERVICE_NAME`/`otel.service.name`
environment variable/system property for configuring the SDK's `Resource` implementation.

### Metrics (alpha)
- The autoconfiguration code for metrics now supports durations to be provided with units attached to them (eg. "`100ms`"). This includes
the following environment variables/system properties:
  - `OTEL_EXPORTER_OTLP_TIMEOUT`/`otel.exporter.otlp.timeout`
  - `OTEL_IMR_EXPORT_INTERVAL`/`otel.imr.export.interval`

## Version 1.2.0 (2021-05-07)

### General

#### Enhancements
- The `"Implementation-Version"` attribute has been added to the jar manifests for all published jar artifacts.

### API

#### Enhancements
- A new method has been added to the Span and the SpanBuilder to enable adding a set of Attributes in one call, rather than
having to iterate over the contents and add them individually. See `Span.setAllAttributes(Attributes)` and `SpanBuilder.setAllAttributes(Attributes)`

#### Behavioral Changes
- Previously, an AttributeKey with a null underlying key would preserve the null. Now, this will be converted to an empty String.

### SDK

#### Enhancements
- The `IdGenerator.random()` method will now attempt to detect if it is being used in an Android environment, and use
a more Android-friendly `IdGenerator` instance in that case. This will affect any usage of the SDK that does not
explicitly specify a custom `IdGenerator` instance when running on Android.

#### Behavioral Changes
- The name used for Tracer instances that do not have a name has been changed to be an empty String, rather than the
previously used `"unknown"` value. This change is based on a specification clarification.

### Propagators

#### Bugfixes
- The B3 Propagator injectors now only include the relevant fields for the specific injection format.

#### Behavioral Changes
- The `W3CBaggagePropagator` will no longer explicitly populate an empty `Baggage` instance into the context when
the header is unparsable. It will now return the provided Context instance unaltered, as is required by the specification.
- The `AwsXrayPropagator` will no longer explicitly populate an invalid `Span` instance into the context when
the headers are unparsable. It will now return the provided Context instance unaltered, as is required by the specification.

### Exporters
- The `jaeger-thrift` exporter has had its dependency on the `jaeger-client` library updated to version `1.6.0`.
- The `zipkin` exporter now has an option to specific a custom timeout.
- The `zipkin`, `jaeger` and `jaeger-thrift` exporters will now report the `otel.dropped_attributes_count` and `otel.dropped_events_count`
tags if the numbers are greater than zero.

### Semantic Conventions (alpha)

#### Breaking Changes
- The SemanticAttributes and ResourceAttributes have both been updated to match the OpenTelemetry Specification v1.3.0 release, which
includes several breaking changes.
- Values that were previously defined as `enum`s are now defined as static `public static final ` constants of the appropriate type.

### OpenTracing Shim (alpha)

#### Enhancements
- Error logging support in the shim is now implemented according to the v1.2.0 specification.

### SDK Extensions
- A new `HostResource` Resource and the corresponding `ResourceProvider` has been added.
It will populate the `host.name` and `host.arch` Resource Attributes.
- A new `ExecutorServiceSpanProcessor` has been added to the `opentelemetry-sdk-extension-tracing-incubator` module. This implementation
of a batch SpanProcessor allows you to provide your own ExecutorService to do the background export work.
- The `autoconfigure` module now supports providing the timeout setting for the Jaeger GRPC exporter via
a system property (`otel.exporter.jaeger.timeout`) or environment variable (`OTEL_EXPORTER_JAEGER_TIMEOUT`).
- The `autoconfigure` module now supports providing the timeout setting for the Zipkin exporter via
a system property (`otel.exporter.zipkin.timeout`) or environment variable (`OTEL_EXPORTER_ZIPKIN_TIMEOUT`).
- The `autoconfigure` module now exposes the `EnvironmentResource` class to provide programmatic access to a `Resource`
built from parsing the `otel.resource.attributes` configuration property.

### Metrics (alpha)

#### Breaking Changes
- The deprecated `SdkMeterProvider.registerView()` method has been removed. The ViewRegistry is now immutable and cannot
be changed once the `SdkMeterProvider` has been built.

#### Bugfixes
- OTLP summaries now have the proper percentile value of `1.0` to represent the maximum; previously it was wrongly set to `100.0`.

#### Enhancements
- There is now full support for delta-aggregations with the `LongSumAggregator` and `DoubleSumAggregator`.
See `AggregatorFactory.sum(AggregationTemporality)`. The previous `AggregatorFactory.sum(boolean)` has been
deprecated and will be removed in the next release.

## Version 1.1.0 (2021-04-07)

### API

#### Bugfixes

- We now use our own internal `@GuardedBy` annotation for errorprone so there won't be an accidental
transitive dependency on a 3rd-party jar.
- The `TraceStateBuilder` now will not crash when an empty value is provided.

#### Enhancements

- The `Context` class now provides methods to wrap `java.util.concurrent.Executor` and `java.util.concurrent.ExecutorService`
instances to do context propagation using the current context. See `io.opentelemetry.context.Context.taskWrapping(...)` for
more details.

### OpenTracing Shim (alpha)

- The shim now supports methods that take a timestamp as a parameter.
- You can now specify both the `TEXT_MAP` and the `HTTP_HEADER` type propagators for the shim.
See `io.opentelemetry.opentracingshim.OpenTracingPropagators` for details.

### Extensions

- The AWS X-Ray propagator is now able to extract 64-bit trace ids.

### SDK

#### Bugfixes

- The `CompletableResultCode.join(long timeout, TimeUnit unit)` method will no longer `fail` the result
when the timeout happens. Nor will `whenComplete` actions be executed in that case.
- The `SimpleSpanProcessor` now keeps track of pending export calls and will wait for them to complete
via a CompletableResultCode when `forceFlush()` is called. Similarly, this is also done on `shutdown()`.
- The Jaeger Thrift exporter now correctly populates the parent span id into the exporter span.

#### Enhancements

- The SpanBuilder provided by the SDK will now ignore `Link` entries that are reference an invalid SpanContext.
This is an update from the OpenTelemetry Specification v1.1.0 release.
- The OTLP Exporters will now log more helpful messages when the collector is unavailable or misconfigured.
- The internals of the `BatchSpanProcessor` have had some optimization done on them, to reduce CPU
usage under load.
- The `Resource` class now has `builder()` and `toBuilder()` methods and a corresponding `ResourceBuilder` class
has been introduced for more fluent creation and modification of `Resource` instances.
- The standard exporters will now throttle error logging when export errors are too frequent. If more than 5
error messages are logged in a single minute by an exporter, logging will be throttled down to only a single
log message per minute.

### SDK Extensions

#### Bugfixes

- Removed a stacktrace on startup when using the `autoconfigure` module without a metrics SDK on the classpath.

#### Enhancements

- The `autoconfigure` module now supports `OTEL_EXPORTER_OTLP_METRICS_ENDPOINT` and `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT`
settings, in addition to the combined `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable. Corresponding
system properties are also supported (`-Dotel.exporter.otlp.metrics.endpoint` and `-Dotel.exporter.otlp.traces.endpoint`).
- An `SdkMeterProviderConfigurer` SPI is now available in the `autoconfigure` module.

### Semantic Conventions (alpha)

- The SemanticAttributes and ResourceAttributes have both been updated to match the OpenTelemetry Specification v1.1.0 release.
This includes a breaking changes to the constants defined in the `ResourceAttributes` class:
`ResourceAttributes.CLOUD_ZONE` has been replaced with `ResourceAttributes.CLOUD_AVAILABILITY_ZONE`.

### Metrics (alpha)

#### Breaking Changes

- The `ViewRegistry` now lets you register `View` objects, rather than `AggregatorFactory` instances.
- `GlobalMetricsProvider` has been renamed to `GlobalMeterProvider`.
- `View` registration has been moved to the `SdkMeterProviderBuilder` and the methods on the `SdkMeterProvider`
to add views have been deprecated. They will be removed in the next release.

#### Enhancements

- A new option for aggregation as Histograms is now available.

## Version 1.0.1 (2021-03-11)

### Bugfixes

- AWS resource extensions have been fixed to not throw NullPointerException in actual AWS environment

## Version 1.0.0 (2021-02-26)

### General

This releases marks the first stable release for the tracing, baggage and context APIs and the SDK.
Please see the [Versioning](VERSIONING.md) document for stability guarantees.

The best source of lsit of the now stable packages can be found in the
[opentelemetry-bom](https://repo1.maven.org/maven2/io/opentelemetry/opentelemetry-bom/1.0.0/opentelemetry-bom-1.0.0.pom)
artifact in maven central.

Javadoc is available at javadoc.io.
For example, [javadoc.io](https://javadoc.io/doc/io.opentelemetry/opentelemetry-api/1.0.0/index.html) for
the API module.

#### Changes

- The `opentelemetry-proto` module is now versioned as an `alpha` module, as it contains non-stable
metrics and logs signals. It has hence been removed from the main BOM.
- The `opentelemetry-sdk-extension-otproto` module has been removed. The classes in it have been moved
to a new `opentelemetry-exporter-otlp-common` module but have been repackaged into an unsupported,
internal package.

### Metrics (alpha)

#### Breaking Changes

- `PrometheusCollector.Builder` inner class has been moved to the top level as `PrometheusCollectorBuilder`.

## Version 0.17.1 (2021-02-19)

- Removed the unused `ResourceProvider` interface from the SDK. This interface is still available
in the `opentelemetry-sdk-extension-autoconfigure` module, where it is actually used.

## Version 0.17.0 (2021-02-17) - RC#3

### General

Note: In an effort to accelerate our work toward a 1.0.0 release, we have skipped the deprecation phase
on a number of breaking changes. We apologize for the inconvenience this may have caused. We are very
aware that these changes will impact users. If you need assistance in migrating from previous releases,
please open a [discussion topic](https://github.com/open-telemetry/opentelemetry-java/discussions) at
[https://github.com/open-telemetry/opentelemetry-java/discussions](https://github.com/open-telemetry/opentelemetry-java/discussions).

Many classes have been made final that previously were not. Please reach out if you have a need to
provide extended functionality, and we can figure out how best to solve your use-case.

### API

#### Breaking Changes

- `TraceStateBuilder.set(String, String)` has been renamed to `TraceStateBuilder.put(String, String)`.
- `BaggageBuilder.setParent()` and `BaggageBuilder.setNoParent()` have been removed from the Baggage APIs.
In addition, Baggage will no longer be implicitly generated from Baggage that is in the current context. You now must explicitly
get the `Baggage` instance from the `Context` and call `toBuilder()` on it in order to get the entries pre-populated in your builder.
- `TextMapPropagator.Setter` and `TextMapPropagator.Getter` have been moved to the top level and renamed to
`TextMapSetter` and `TextMapGetter` respectively.
- `OpenTelemetry.getDefault()` has been renamed to `OpenTelemetry.noop()`.
- `OpenTelemetry.getPropagating()` has been renamed to `OpenTelemetry.propagating()`.
- `TracerProvider.getDefault()` has been renamed to `TracerProvider.noop()`
- `Tracer.getDefault()` has been removed.
- `TraceId.getTraceIdRandomPart(CharSequence)` has been removed.
- The `B3Propagator.getInstance()` has been renamed to `B3Propagator.injectingSingleHeader()`.
- The `B3Propagator.builder()` method has been removed. As a replacement, you can use `B3Propagator.injectingMultiHeaders()` directly.

### SDK

#### Breaking Changes

- The SPI for configuring Resource auto-populators has been removed from the SDK and moved to the `opentelemetry-sdk-extension-autoconfigure` module.
This means that `Resource.getDefault()` will no longer be populated via SPI, but only include the bare minimum values from the SDK itself.
In order to get the auto-configured Resource attributes, you will need to use the `opentelemetry-sdk-extension-autoconfigure` module directly.
- `InstrumentationLibraryInfo.getEmpty()` has been renamed to `InstrumentationLibraryInfo.empty()`.
- `Resource.getEmpty()` has been renamed to `Resource.empty()`.
- When specifying the endpoints for grpc-based exporters, you now are required to specify the protocol. Hence, you must include
the `http://` or `https://` in front of your endpoint.
- The option on `SpanLimits` to truncate String-valued Span attributes has been removed (this is still pending in the specification).
- The `InMemoryMetricsExporter` has been removed from the `opentelemetry-sdk-testing` module.

#### Miscellaneous

- The default values for SpanLimits have been changed to 128, from 1000, to match the spec.

### Extensions

#### Breaking Changes

- In the `opentelemetry-sdk-extension-autoconfigure` module, we have changed the system property used to exclude some Resource auto-populators to be
`otel.java.disabled.resource-providers` instead of `otel.java.disabled.resource_providers`.
- In the `opentelemetry-sdk-extension-autoconfigure` module, you now specify the `OtTracePropagator` with the `"ottrace"` option, rather than `"ottracer"`.
- In the `opentelemetry-sdk-extension-autoconfigure` module, the default exporters are now set to be `"otlp"`, as required by the 1.0.0 specification.
- In the `opentelemetry-sdk-extension-autoconfigure` module, the default propagators are now set to be `"tracecontext,baggage"`, as required by the 1.0.0 specification.
- The `CommonProperties` class has been removed from the `opentelemetry-sdk-extension-otproto` module.

### Metrics (alpha)

#### API

- `Meter.getDefault()` has been removed.
- `MeterProvider.getDefault()` has been renamed to `MeterProvider.noop()`.

## Version 0.16.0 (2021-02-08) - RC#2

### General

Note: In an effort to accelerate our work toward a 1.0.0 release, we have skipped the deprecation phase
on a number of breaking changes. We apologize for the inconvenience this may have caused. We are very
aware that these changes will impact users. If you need assistance in migrating from previous releases,
please open a [discussion topic](https://github.com/open-telemetry/opentelemetry-java/discussions) at
[https://github.com/open-telemetry/opentelemetry-java/discussions](https://github.com/open-telemetry/opentelemetry-java/discussions).

#### Breaking Changes

- Methods and classes deprecated in 0.15.0 have been removed.

### API

#### Breaking Changes

- The `Span.Kind` enum has been moved to the top level, and named `SpanKind`.
- `DefaultOpenTelemetry` is no longer a public class. If you need the functionality previously provided by this
implementation, it can be accessed via new static methods on the `OpenTelemetry` interface itself.
- The `TraceFlags` interface has been re-introduced. This is now used, rather than a bare `byte` wherever
trace flags is used. In particular, `SpanContext.create()`, `SpanContext.createFromRemoteParent()` now require
a `TraceFlags` instance, and `SpanContext.getTraceFlags()` returns a `TraceFlags` instance.
- The names of static methods on `TraceFlags` have been normalized to match other similar classes, and now
return `TraceFlags` instead of `byte` where appropriate.
- The `Labels` interface and related classes have been moved into the alpha metrics modules and repackaged.
- `TraceId.copyHexInto(byte[] traceId, char[] dest, int destOffset)` has been removed.
- `SpanContext.getTraceIdAsHexString()` has been renamed to `SpanContext.getTraceId()`
- `SpanContext.getSpanIdAsHexString()` has been renamed to `SpanContext.getSpanId()`
- `BaggageEntry.getEntryMetadata()` has been renamed to `BaggageEntry.getMetadata()`
- `BaggageConsumer` has been removed in favor of a standard `java.util.function.BiConsumer<String, BaggageEntry>`
- `TraceFlags.isSampledFromHex(CharSequence src, int srcOffset)` has been removed.
- `SpanId` and `TraceId` methods that had a `String` parameter now accept `CharSequence`
and assume the id starts at the beginning.
- `SpanId.getSize()` and `TraceId.getSize()` have been removed.
- `SpanId.bytesFromHex()` has been removed.
- `SpanId.asLong(CharSequence)` has been removed.
- `SpanId.asBytes(CharSequence)` has been removed.
- `SpanId.getHexLength()` has been renamed to `SpanId.getLength()`
- `SpanId.bytesToHex()` has been renamed to `SpanId.fromBytes()`
- `TraceId.bytesFromHex()` has been removed.
- `TraceId.traceIdLowBytesAsLong(CharSequence)` has been removed.
- `TraceId.traceIdHighBytesAsLong(CharSequence)` has been removed.
- `TraceId.asBytes(CharSequence)` has been removed.
- `TraceId.getHexLength()` has been renamed to `TraceId.getLength()`
- `TraceId.bytesToHex()` has been renamed to `TraceId.fromBytes()`
- `StrictContextStorage` has been made private. Use -Dio.opentelemetry.context.enableStrictContext=true` to enable it
- `AwsXrayPropagator` has been moved to the `opentelemetry-extension-aws` artifact

#### Enhancements

- The `W3CTraceContextPropagator` class now directly implements the `TextMapPropagator` interface.
- The `OpenTelemetry` interface now has a `getDefault()` method which will return a completely no-op implementation.
- The `OpenTelmmetry` interface now has a `getPropagating(ContextPropagators propagators)` method which will
return an implementation that contains propagators, but is otherwise no-op.

#### Misc Notes

- The internal `StringUtils` class has had metrics-related methods removed from it. But, you weren't using
internal classes, were you?
- The internal `AbstractWeakConcurrentMap` class has been made non-public. See the line above about internal classes.

### Extensions

#### Breaking Changes

- The `OtTracerPropagator` has been renamed to `OtTracePropagator` in the trace-propagators extension module.

### SDK

#### Breaking Changes

- `TraceConfig` has been renamed to `SpanLimits` and relocated to the `io.opentelemetry.sdk.tracing` package.
All related method names have been renamed to match.
- `SpanData.getTraceState()` has been removed. The TraceState is still available via the SpanContext accessor.
- `SpanData.isSampled()` has been removed. The isSampled property is still available via the SpanContext accessor.

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

## Version 0.15.0 (2021-01-29) - RC#1

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

## Version 0.14.1 (2021-01-14)

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

## Version 0.13.0 (2020-12-17)

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

## Version 0.12.0 (2020-12-04)

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

## Version 0.11.0 (2020-11-18)

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

## Version 0.10.0 (2020-11-06)

### API

#### Enhancements

- The W3C Baggage Propagator is now available.
- The B3 Propagator now handles both single and multi-header formats.
- The B3 Propagator defaults to injecting the single B3 header, rather than the multi-header format.
- Mutating a method on `Span` now returns the `Span` to enable call-chaining.

#### Bug fixes

- The `package-info` file was removed from the `io.otel.context` package because it made the project incompatible with JPMS.

#### Breaking changes

- There have been many updates to the semantic conventions constants. The constants are now auto-generated from the YAML specification files, so the names will now be consistent across languages. For more information, see the [YAML Model for Semantic Conventions](https://github.com/open-telemetry/semantic-conventions/blob/main/model/README.md#yaml-model-for-semantic-conventions).
- All API classes have been moved into the `io.opentelemetry.api.` prefix to support JPMS users.
- The API no longer uses the `grpc-context` as the context implementation. It now uses `io.opentelemetry.context.Context`. This is published in the `opentelemetry-context` artifact. Interactions with the context were mostly moved to static methods in the `Span` and `Baggage` interfaces.
- The Baggage API has been reworked to more closely match the specification. This includes the removal of the `BaggageManager`. Baggage is fully functional within the API, without needing to install an SDK.
- `TracingContextUtils` and `BaggageUtils` were removed from the public API. Instead, use the appropriate static methods on the `Span` and `Baggage` classes, or use methods on the `Context` itself.
- The context propagation APIs have moved into the new `opentelemetry-context` context module.
- `DefaultSpan` was removed from the public API. Instead, use `Span.wrap(spanContext)` if you need a non-functional span that propagates the trace context.
- `DefaultMeter`, `DefaultMeterProvider`, `DefaultTracer` and `DefaultTracerProvider` were removed from the public API. You can access the same functionality with `getDefault()` methods on the `Meter`, `MeterProvider, `Tracer`, and `TracerProvider` classes, respectively.
- Some functionality from the `Tracer` interface is now available either on the `Span` interface or `Context` interface.
- The `OpenTelemetry` class is now an interface, with implementations. Methods on this interface have changed their names to reflect this change. For more information, see [OpenTelemetry.java](api/all/src/main/java/io/opentelemetry/api/OpenTelemetry.java).
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

- `TraceConfig` configuration option names (environment variables and system properties) were renamed to match the OpenTelemetery Specification.
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
