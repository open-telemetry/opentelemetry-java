# Promise propagation example.

This example shows `Span` usage with `Promise` objects, such as the ones in **Akka** and other asynchronous frameworks: objects representing the result of a concurrent operation that can have multiple callbacks for success/error, along a way to manually set the final result (a writable `Future`, that is).

A top-level `Span` is created for the main work, and later passed to the `Promise` object, to be used as reference for the newly created `Span`s in each callback. Since those callbacks are scheduled through a executor and will possibly run at different times, possibly in parallel, the `FOLLOWS_FROM` reference is used instead of the usual `CHILD_OF`, to document the top-level `Span` doesn't depend on the finish time of its children.

```java
  // Promise.success()
  public void success(final T result) {
    for (final SuccessCallback<T> callback : successCallbacks) {
      context.submit(
          new Runnable() {
            @Override
            public void run() {
              Span childSpan = tracer
                  .buildSpan("success")
                  .addReference(References.FOLLOWS_FROM, parentSpan.context())
                  .withTag(Tags.COMPONENT.getKey(), "success")
                  .start();
              try (Scope childScope = tracer.activateSpan(childSpan)) {
                callback.accept(result);
              } finally {
                childSpan.finish();
              }
              ...
            }
          });
    }
  }
```
