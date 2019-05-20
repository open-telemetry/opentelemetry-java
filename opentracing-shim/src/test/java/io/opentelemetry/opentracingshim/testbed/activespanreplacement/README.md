# Active Span replacement example.

This example shows a `Span` being created and then passed to an asynchronous task, which will temporary activate it to finish its processing, and further restore the previously active `Span`.

```java
// Create a new Span for this task
Span taskSpan = tracer.buildSpan("task").start();
try (Scope scope = tracer.scopeManager().activate(taskSpan)) {

    // Simulate work strictly related to the initial Span
    // and finish it.
    try (Scope initialScope = tracer.scopeManager().activate(initialSpan)) {
    } finally {
      initialSpan.finish();
    }

    // Continue doing tasks under taskSpan
} finally {
  taskSpan.finish();
}
```
