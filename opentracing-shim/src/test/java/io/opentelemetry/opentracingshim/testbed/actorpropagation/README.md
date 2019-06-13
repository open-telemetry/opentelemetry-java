# Actor propagation example.

This example shows `Span` usage with `Scala` frameworks using the `Actor` paradigm (such as the **Akka** framework), with both `tell` (fire and forget) and `ask` (asynchronous, but returning a result)  message passing ways. 

`Actor` mimics the API offered by such frameworks, and `java.util.concurrent.Phaser` is used to synchronize the steps of the example.

```java
  public void tell(final String message) {
    final Span parent = tracer.scopeManager().activeSpan();
    executor.submit(
        new Runnable() {
          @Override
          public void run() {
            Span child = tracer
                .buildSpan("received")
                .addReference(References.FOLLOWS_FROM, parent.context())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .start();
            try (Scope scope = tracer.activateSpan(child)) {
              child.log("received " + message);
            } finally {
              child.finish();
            }
          }
        });
```
