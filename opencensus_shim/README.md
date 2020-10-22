
# OpenTelemetry OpenCensus Shim

The OpenCensus shim allows applications and libraries that are instrumented
with OpenTelemetry, but depend on other libraries instrumented with OpenCensus,
to export trace spans from both OpenTelemetry and OpenCensus with the correct
parent-child relationship.

The shim maps all OpenCensus spans to OpenTelemetry spans, which are then exported
by any configured OpenTelemetry exporter.

## Usage

To allow the shim to work, add the shim as a dependency.

Libraries do not need to do anything else for this to work.

Applications only need to be configured for OpenTelemetry, not OpenCensus.

## Known Problems

* OpenCensus links added after an OpenCensus span is created will not be
exported, as OpenTelemetry only supports links added when a span is created.
* There is a 10-minute timeout, as well as a 10,000 entries limit in
the shim span cache. This means if an OpenCensus span is not ended within 10
minutes, or if more than 10,000 active spans are created after the span,
the span will be deleted and will not be exported.
