# Late Span finish example.

This example shows a `Span` for a top-level operation, with independent, unknown lifetime, acting as parent of a few asynchronous subtasks (which must re-activate it but not finish it).

```java
// Inside the function submitting the subtasks.
executor.submit(new Runnable() {
    @Override
    public void run() {
        try (Scope scope = tracer.scopeManager().activate(parentSpan)) {
        }
    }
});
```
