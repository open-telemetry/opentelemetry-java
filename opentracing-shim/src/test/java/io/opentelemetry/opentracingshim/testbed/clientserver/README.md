# Client-Server example.

This example shows a `Span` created by a `Client`, which will send a `Message`/`SpanContext` to a `Server`, which will in turn extract such context and use it as parent of a new (server-side) `Span`.

`Client.send()` is used to send messages and inject the `SpanContext` using the `TEXT_MAP` format, and `Server.process()` will process received messages and will extract the context used as parent.

```java
    public void send() throws InterruptedException {
        Message message = new Message();

        Span span = tracer.buildSpan("send")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
            .withTag(Tags.COMPONENT.getKey(), "example-client")
            .start();
        try (Scope scope = tracer.activateSpan(span)) {
            tracer.inject(span.context(), Builtin.TEXT_MAP, new TextMapInjectAdapter(message));
            queue.put(message);
        } finally {
            span.finish();
        }
    }
```
