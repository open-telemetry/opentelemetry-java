# Listener Response example.

This example shows a `Span` created upon a message being sent to a `Client`, and its handling along a related, **not shared** `ResponseListener` object with a `onResponse(Object response)` method to finish it.

```java
    public Future<Object> send(final Object message) {
        Span span = tracer.buildSpan("send").
                withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .startManual();
        return execute(message, new ResponseListener(span));
    }

    private Future<Object> execute(final Object message, final ResponseListener responseListener) {
        return executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                // send via wire and get response
                Object response = message + ":response";
                responseListener.onResponse(response);
                return response;
            }
        });
    }
```
