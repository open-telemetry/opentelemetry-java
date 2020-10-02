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
try (Scope scope = tracer.withSpan(span)) {
    // Do things.
} catch (Exception ex) {
    span.recordException(ex);
} finally {
    span.end();
}
```

It would not be possible to call `recordException` if `span` was also using try-with-resources.
Because this is a common usage for spans, we do not support try-with-resources.