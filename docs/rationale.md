# OpenTelemetry Rationale

When creating a library, often times designs and decisions are made that get lost over time. This
document tries to collect information on design decisions to answer common questions that may come
up when you explore the SDK.

## Span not `Closeable`

Because a `Span` has a lifecycle, where it is started and MUST be ended, it seems intuitive that a
`Span` should implement `Closeable` or `AutoCloseable` to allow usage with Java try-with-resources
construct. However, `Span`s are unique in that they must still be alive when handling exceptions,
which try-with-resources does not allow. Take this example:

```java
Span span = tracer.spanBuilder("someWork").startSpan();
try (Scope scope = TracingContextUtils.currentContextWith(span)) {
    // Do things.
} catch (Exception ex) {
    span.recordException(ex);
} finally {
    span.end();
}
```

It would not be possible to call `recordException` if `span` was also using try-with-resources.
Because this is a common usage for spans, we do not support try-with-resources.


## Versioning and Releases

### Assumptions

- This project uses semver v2, as does the rest of OpenTelemetry.

### Goals

- API Stability:
    - Once the API for a given signal (spans, logs, metrics, baggage) has been officially released, code instrumented with that API module will
function, *with no recompilation required*, with any API+SDK that has the same major version, and equal or greater minor or patch version.
    - For example, libraries that are instrumented with `opentelemetry-api-trace:1.0.1` will function, at runtime with
SDK library `opentelemetry-sdk-trace:1.11.33` plus `opentelemetry-api-trace:1.11.33` (or whatever specific versions are specified by
      the bom version `1.11.33`, if the individual versions have diverged).
    - We call this requirement the "ABI" compatibility requirement for "Application Binary Interface" compatibility.
- SDK Stability:
    - Public portions of the SDK (constructors, configuration, end-user interfaces) must remain backwards compatible.
        - Precisely what this includes has yet to be delineated.
- Internal implementation details of both the API and SDK are allowed to be changed,
  as long as the public APIs are not changed in an ABI-incompatible manner.

### Methods

- Mature signals
    - API modules for mature (i.e. released) signals will be transitive dependencies of the `opentelemetry-api` module.
    - Methods for accessing mature APIs will be added, as appropriate to the `OpenTelemetry` interface.
    - SDK modules for mature (i.e. released) signals will be transitive dependencies of the `opentelemetry-sdk` module.
    - Configuration options for the SDK modules for mature signals will be exposed, as appropriate, on the `OpenTelemetrySdk` class.
    - Modules for these mature signals will be included in the opentelemetry-bom to ensure that users runtime dependencies are kept in sync.
    - Mixing and matching runtime API and SDK versions, eg. by avoiding use of the BOM, will not be supported by this project.
    - Once a public API (either in the official API or in the SDK) has been released, we will endeavor to support that API in perpetuity.

- Immature or experimental signals
    - API modules for immature signals will not be transitive dependencies of the `opentelemetry-api` module.
    - API modules will be versioned with an "-alpha" suffix to make it abundantly clear that depending on them is at your own risk.
    - API modules for immature signals will be co-versioned along with mature API modules, with the added suffix.
    - The java packages for immature APIs will be used as if they were mature signals. This will enable users to easily transition from immature to
    mature usage, without having to change imports.
    - SDK modules for immature signals will also be versioned with an "-alpha" suffix, in parallel to their API modules.

### Examples

Purely for illustration purposes, not intended to represent actual releases:

- `v1.0.0` release:
    - `io.opentelemetry:opentelemetry-api:1.0.0`
        - Includes APIs for tracing, baggage, context, propagators (via the context dependency)
    - `io.opentelemetry:opentelemetry-api-metrics:1.0.0-alpha`
        - Note: packages here are the final package structure: `io.opentelemetry.api.metrics.*`
    - `io.opentelemetry:opentelemetry-sdk-trace:1.0.0`
    - `io.opentelemetry:opentelemetry-sdk-common:1.0.0`
        - Shared code for metrics/trace implementations (clocks, etc)
    - `io.opentelemetry:opentelemetry-sdk-metrics:1.0.0-alpha`
        - Note: packages here are the final package structure: `io.opentelemetry.sdk.metrics.*`
    - `io.opentelemetry:opentelemetry-sdk-all:1.0.0`
        - The SDK side of `io.opentelemetry:opentelemetry-api:1.0.0`
        - No mention of metrics in here!
- `v1.15.0` release (with metrics)
    - `io.opentelemetry:opentelemetry-api:1.15.0`
        - Contains APIs for tracing, baggage, propagators (via the context dependency), metrics
    - `io.opentelemetry:opentelemetry-sdk-trace:1.15.0`
    - `io.opentelemetry:opentelemetry-sdk-common:1.15.0`
        - Shared code for metrics/trace implementations (clocks, etc)
    - `io.opentelemetry:opentelemetry-sdk-metrics:1.15.0`
        - Note: packages here have not changed from the experimental jar...just a jar rename happened.
    - `io.opentelemetry:opentelemetry-sdk-all:1.15.0`
        - The SDK side of io.opentelemetry:opentelemetry-api:1.15.0
