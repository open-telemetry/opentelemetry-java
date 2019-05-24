# Stateless common Request Handler example.

This example shows a `Span` used with `RequestHandler`, which is used as common, *stateless* middleware (as in web frameworks) to manage a new `Span` per operation through its `beforeRequest()`/`afterResponse()` methods. As these methods do not expose any object storing state, a thread-local field in `RequestHandler` itself is used to contain the `Scope` related to `Span` activation.

```java
    public void beforeRequest(Object request) {
        logger.info("before send {}", request);

        Span span = tracer.buildSpan(OPERATION_NAME).start();
        tlsScope.set(tracer.activateSpan(span));
    }

    public void afterResponse(Object response) {
        logger.info("after response {}", response);

        // Finish the Span
        tracer.scopeManager().activeSpan().finish();

        // Deactivate the Span
        tlsScope.get().close();
    }
```
