# Suspend-resume example.

This example shows `Span` usage with the suspend-resume model found in asynchronous frameworks, where each step is reported and finally signaled as done. 

`SuspendResume` is used as the class reporting progress (at which point the captured `Span` is activated)  and signaling the operation success (at which point the `Span` is finished).

```java
  // SuspendResume
  public void doPart(String name) {
    try (Scope scope = tracer.scopeManager().activate(span)) {
      scope.span().log("part: " + name);
    }
  }

  public void done() {
    span.finish();
  }
```
