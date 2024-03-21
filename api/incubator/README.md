# ExtendedTracer

Utility methods to make it easier to use the OpenTelemetry tracer.

## Usage Examples

Here are some examples how the utility methods can help reduce boilerplate code.

### Tracing a function

Before:

<!-- markdownlint-disable -->
```java
Span span = tracer.spanBuilder("reset_checkout").startSpan();
String transactionId;
try (Scope scope = span.makeCurrent()) {
  transactionId = resetCheckout(cartId);
} catch (Throwable e) {
  span.setStatus(StatusCode.ERROR);
  span.recordException(e);
  throw e; // or throw new RuntimeException(e) - depending on your error handling strategy
} finally {
  span.end();
}
```
<!-- markdownlint-enable -->

After:

```java
import io.opentelemetry.extension.incubator.trace.ExtendedTracer;

ExtendedTracer extendedTracer = ExtendedTracer.create(tracer);
String transactionId = extendedTracer.spanBuilder("reset_checkout").startAndCall(() -> resetCheckout(cartId));
```

If you want to set attributes on the span, you can use the `startAndCall` method on the span builder:

```java
import io.opentelemetry.extension.incubator.trace.ExtendedTracer;

ExtendedTracer extendedTracer = ExtendedTracer.create(tracer);
String transactionId = extendedTracer.spanBuilder("reset_checkout")
    .setAttribute("foo", "bar")
    .startAndCall(() -> resetCheckout(cartId));
```

Note:

- Use `startAndRun` instead of `startAndCall` if the function returns `void` (both on the tracer and span builder).
- Exceptions are re-thrown without modification - see [Exception handling](#exception-handling)
  for more details.

### Trace context propagation

Before:

```java
Map<String, String> propagationHeaders = new HashMap<>();
openTelemetry
    .getPropagators()
    .getTextMapPropagator()
    .inject(
        Context.current(),
        propagationHeaders,
        (map, key, value) -> {
          if (map != null) {
            map.put(key, value);
          }
        });

// add propagationHeaders to request headers and call checkout service
```

<!-- markdownlint-disable -->
```java
// in checkout service: get request headers into a Map<String, String> requestHeaders
Map<String, String> requestHeaders = new HashMap<>();
String cartId = "cartId";

SpanBuilder spanBuilder = tracer.spanBuilder("checkout_cart");

TextMapGetter<Map<String, String>> TEXT_MAP_GETTER =
    new TextMapGetter<Map<String, String>>() {
      @Override
      public Set<String> keys(Map<String, String> carrier) {
        return carrier.keySet();
      }

      @Override
      @Nullable
      public String get(@Nullable Map<String, String> carrier, String key) {
        return carrier == null ? null : carrier.get(key);
      }
    };

Map<String, String> normalizedTransport =
    requestHeaders.entrySet().stream()
        .collect(
            Collectors.toMap(
                entry -> entry.getKey().toLowerCase(Locale.ROOT), Map.Entry::getValue));
Context newContext = openTelemetry
    .getPropagators()
    .getTextMapPropagator()
    .extract(Context.current(), normalizedTransport, TEXT_MAP_GETTER);
String transactionId;
try (Scope ignore = newContext.makeCurrent()) {
  Span span = spanBuilder.setSpanKind(SERVER).startSpan();
  try (Scope scope = span.makeCurrent()) {
    transactionId = processCheckout(cartId);
  } catch (Throwable e) {
    span.setStatus(StatusCode.ERROR);
    span.recordException(e);
    throw e; // or throw new RuntimeException(e) - depending on your error handling strategy
  } finally {
    span.end();
  }
}
```
<!-- markdownlint-enable -->

After:

```java
import io.opentelemetry.extension.incubator.propagation.ExtendedContextPropagators;

Map<String, String> propagationHeaders =
    ExtendedContextPropagators.getTextMapPropagationContext(openTelemetry.getPropagators());
// add propagationHeaders to request headers and call checkout service
```

```java
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.extension.incubator.trace.ExtendedTracer;

// in checkout service: get request headers into a Map<String, String> requestHeaders
Map<String, String> requestHeaders = new HashMap<>();
String cartId = "cartId";

ExtendedTracer extendedTracer = ExtendedTracer.create(tracer);
String transactionId = extendedTracer.spanBuilder("checkout_cart")
    .setSpanKind(SpanKind.SERVER)
    .setParentFrom(openTelemetry.getPropagators(), requestHeaders)
    .startAndCall(() -> processCheckout(cartId));
```

## Exception handling

`ExtendedTracer` re-throws exceptions without modification. This means you can
catch exceptions around `ExtendedTracer` calls and handle them as you would without `ExtendedTracer`.

When an exception is encountered during an `ExtendedTracer` call, the span is marked as error and
the exception is recorded.

If you want to customize this behaviour, e.g. to only record the exception, because you are
able to recover from the error, you can call the overloaded method of `startAndCall` or
`startAndRun` that takes an exception handler:

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.incubator.trace.ExtendedTracer;

ExtendedTracer extendedTracer = ExtendedTracer.create(tracer);
String transactionId = extendedTracer.spanBuilder("checkout_cart")
    .startAndCall(() -> processCheckout(cartId), Span::recordException);
```
