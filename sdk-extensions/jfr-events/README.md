# OpenTelemetry SDK Extension Java Flight Recorder (JFR) Events

[![Javadocs][javadoc-image]][javadoc-url]

Create JFR events that can be recorded and viewed in Java Mission Control (JMC).
* Creates Open Telemetry Tracing/Span events for spans
    * The thread and stracktrace will be of the thead ending the span which might be different than the thread creating the span.
    * Has the fields
        * Operation Name
        * Trace ID
        * Parent Span ID
        * Span ID
* Creates Open Telemetry Tracing/Scope events for scopes
    * Thread will match the thread the scope was active in and the stacktrace will be when scope was closed
    * Multiple scopes might be collected for a single span
    * Has the fields
        * Trace ID
        * Span ID
* Supports the Open Source version of JFR in Java 11.
    * Might support back port to OpenJDK 8, but not tested and classes are built with JDK 11 bytecode.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-jfr-events.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-jfr-events
