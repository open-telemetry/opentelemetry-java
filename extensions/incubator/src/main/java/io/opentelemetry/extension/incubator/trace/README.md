# extended-tracer

Utility methods to make it easier to use the OpenTelemetry tracer.

## Usage Examples

Here are some examples how the utility methods can help reduce boilerplate code.

### Tracing a function

Before:

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

After:

```java
Tracing tracing = new Tracing(openTelemetry, "service");
String transactionId = tracing.call("reset_checkout", () -> resetCheckout(cartId));
```

Note:

- Use `run` instead of `call` if the function returns `void`.
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

```java
// in checkout service: get request headers into a Map<String, String> requestHeaders
Map<String, String> requestHeaders = new HashMap<>();
String cartId = "cartId";

SpanBuilder spanBuilder = tracer.spanBuilder("checkout_cart");
String transactionId;

TextMapGetter<Map<String, String>> TEXT_MAP_GETTER =
    new TextMapGetter<Map<String, String>>() {
      @Override
      public Set<String> keys(Map<String, String> carrier) {
        return carrier.keySet();
      }

      @Override
      @Nullable
      public String get(@Nullable Map<String, String> carrier, String key) {
        //noinspection ConstantConditions
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

After:

```java
Tracing tracing = new Tracing(openTelemetry, "service");
Map<String, String> propagationHeaders = tracing.getTextMapPropagationContext();
// add propagationHeaders to request headers and call checkout service
```

```java
// in checkout service: get request headers into a Map<String, String> requestHeaders
Map<String, String> requestHeaders = new HashMap<>();
String cartId = "cartId";

Tracing tracing = new Tracing(openTelemetry, "service");
String transactionId = tracing.traceServerSpan(requestHeaders,
    tracer.spanBuilder("checkout_cart"), () -> processCheckout(cartId));
```

Note:

- You can use `traceConsumerSpan` if you want to trace a consumer (e.g. from a message queue)
  instead of a server.
- Exceptions are re-thrown without modification - see [Exception handling](#exception-handling)
  for more details.

### Setting baggage entries

Before:

```java
BaggageBuilder builder = Baggage.current().toBuilder();
builder.put("key", "value");
Context context = builder.build().storeInContext(Context.current());
try (Scope ignore = context.makeCurrent()) {
  String value = Baggage.current().getEntryValue("key");
}
```

After:

```java
Tracing tracing = new Tracing(openTelemetry, "service");
String value = Tracing.callWithBaggage(
    Collections.singletonMap("key", "value"),
    () -> Baggage.current().getEntryValue("key"))
```

## Exception handling

`Tracing` re-throws exceptions without modification. This means you can catch exceptions around
`Tracing` calls and handle them as you would without `Tracing`.

Note that the `Tracing` methods do not declare any checked exceptions
(the idea is taken from [@SneakyThrows](https://projectlombok.org/features/SneakyThrows)).
Declaring a checked exception would force callers to handle it, which would create a lot of
boilerplate code. Instead, `Tracing` re-throws checked exceptions as unchecked exceptions.

## Component owners

- [Gregor Zeitlinger](https://github.com/zeitlinger), Grafana Labs

Learn more about component owners in [component_owners.yml](../.github/component_owners.yml).
