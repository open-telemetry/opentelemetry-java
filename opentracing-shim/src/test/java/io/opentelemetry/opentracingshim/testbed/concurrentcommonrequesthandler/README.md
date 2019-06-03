# Concurrent common Request Handler example.

This example shows a `Span` used with `RequestHandler`, which is used as a middleware (as in web frameworks) to concurrently manage a new `Span` per operation through its `beforeRequest()`/`afterResponse()` methods.

Since its methods are not guaranteed to be run in the same thread, activation of such `Span`s is not done.

```java
    public void beforeRequest(Object request, Context context) {
        // we cannot use active span because we don't know in which thread it is executed
        // and we cannot therefore activate span. thread can come from common thread pool.
        SpanBuilder spanBuilder = tracer.buildSpan(OPERATION_NAME)
                .ignoreActiveSpan()
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);

        if (parentContext != null) {
            spanBuilder.asChildOf(parentContext);
        }

        context.put("span", spanBuilder.start());
    }

    public void afterResponse(Object response, Context context) {
        Object spanObject = context.get("span");
        if (spanObject instanceof Span) {
            Span span = (Span) spanObject;
            span.finish();
        }
    }
```
