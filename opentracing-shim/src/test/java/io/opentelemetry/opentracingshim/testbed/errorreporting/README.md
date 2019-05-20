# Error Reporting example.

This example shows a few common error reporting cases, mostly interacting with `Scope` and `Span` propagation. In such cases, at the very least `opentracing.tags.Tag.ERROR` must be set in the `Span`:

```java
Span span = tracer.buildSpan("one").start();
executor.submit(new Runnable() {
    @Override
    public void run() {
	try (Scope scope = tracer.activateSpan(span)) {
	    throw new RuntimeException("Invalid state");
	} catch (Exception exc) {
	    Tags.ERROR.set(span, true);
	} finally {
	    span.finish();
	}
    }
});
```
