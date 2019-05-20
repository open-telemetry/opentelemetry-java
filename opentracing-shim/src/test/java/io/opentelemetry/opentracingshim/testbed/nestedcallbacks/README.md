# Nested callbacks example.

This example shows a `Span` for a top-level operation, and how it can be manually passed down on a list of nested callbacks (always one at a time), re-activated on each one, and finished **only** when the last one executes.

```java
try (Scope scope = tracer.scopeManager().activate(span)) {
    span.setTag("key1", "1");

    executor.submit(new Runnable() {
	@Override
	public void run() {
	    try (Scope scope = tracer.scopeManager().activate(span)) {
		span.setTag("key2", "2");
                ...

```
