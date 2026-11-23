# Logging Patterns

Use `java.util.logging` (JUL), not SLF4J, in `src/main/`:

```java
private static final Logger LOGGER = Logger.getLogger(MyClass.class.getName());
```

For test assertions, see [log capture](testing-patterns.md#log-capture).

## Aggregating repeated failures

At normally enabled levels, prefer a summary of repeated failures. At finer levels, individual occurrences can be useful when their sequence or differences matter. Distinct failures may warrant separate diagnostics.

For example, [`BatchSpanProcessor.exportCurrentBatch()`](../../sdk/trace/src/main/java/io/opentelemetry/sdk/trace/export/BatchSpanProcessor.java) uses `droppedSpanCount.getAndSet(0)` to report drops since the last export, rather than warning for every dropped span.

An operational summary can appear at `INFO` or `WARNING`, with individual details at a finer level on the same logger. Tell operators that more details are available at that level. Repetition alone does not make a routine rejection a warning.

## API usage diagnostics

Use [`ApiUsageLogger`](../../common/src/main/java/io/opentelemetry/common/impl/ApiUsageLogger.java) to diagnose instrumentation API misuse. It emits one `WARNING`, then detailed `FINEST` messages with call-site stack traces on `io.opentelemetry.usage`. Enable `FINEST` on that logger to investigate misuse.

Use `logNullParam` for null arguments and `logUsageIssue` for other violations. Supply the API class, method, and reason so the message identifies the problem without requiring stack-trace inspection. See [null guards](api-design.md#null-guards) for the required return or fallback behavior.

## Choosing severity

`INFO` and above are commonly enabled by default. Finer levels are usually opt-in.

- Use `WARNING` or `SEVERE` for operational problems requiring attention. For example, [`HttpExporter.onError()`](../../exporters/otlp/all/src/main/java/io/opentelemetry/exporter/otlp/internal/HttpExporter.java) reports export failures at `SEVERE`, with throttling.
- Use `INFO` for operational information and summaries that remain useful at normal production volume.
- Use finer levels for troubleshooting details and expected, non-actionable failures. For example, `HttpExporter.levelOnFailure()` demotes shutdown-related failures to `FINE`.

Detailed opt-in logs may intentionally be verbose and unthrottled. For example, [`RetryInterceptor.intercept()`](../../exporters/sender/okhttp/src/main/java/io/opentelemetry/exporter/sender/okhttp/internal/RetryInterceptor.java) logs individual attempts at `FINER`, including the attempt number and whether the failure is retryable. Suppressing these records can hide a sequence needed for troubleshooting. Avoid duplicate records for the same event.

## Including exceptions

Include a stack trace when it helps identify the offending caller or understand the failure's cause. A known validation failure can still benefit from a call-site stack trace. For example, an invalid instrument name may originate in one of many instrumentation libraries. An exception's causal chain can also explain the failure; for example, `HttpExporter.onError()` includes an `IOException` that may have several underlying causes.

Distinguish two questions:

- **What failed?** Identify the field and validation rule directly.
- **Who called this code?** A call-site stack trace can identify the instrumentation misusing an API.

The second question can justify a stack trace even when the failure itself is understood. Capture it deliberately at a fine-grained level, once per operation or with throttling, rather than throwing and logging an exception for every rejected value.

For example, `ApiUsageLogger.logUsageIssue()` constructs `new Throwable()` inside an `isLoggable(Level.FINEST)` check to capture the caller, not an unexpected implementation failure.

Pass exceptions as the logger's `Throwable` argument, not as concatenated `getMessage()` text:

```java
logger.log(Level.FINE, "Failed to read response body", exception);
```

Exception construction captures a stack even when logging is disabled; rendering and output add further cost. Avoid exception-driven validation for routine invalid input. Guard expensive diagnostic preparation with `isLoggable`. For example, `RetryInterceptor.intercept()` checks `isLoggable(Level.FINER)` before building its response representation.

## Limiting and protecting logged data

Bound variable fields, especially externally supplied values. Include only what diagnosis requires. Avoid credentials, tokens, cookies, and other sensitive data. Truncation limits size but does not redact secrets. Apply these rules at finer levels too.

For example, [`HttpExporter.extractResponseBodyMessage()`](../../exporters/otlp/all/src/main/java/io/opentelemetry/exporter/otlp/internal/HttpExporter.java) caps fallback response-body text at 1,024 bytes. A bounded prefix can likewise identify an invalid instrument name without echoing an arbitrarily long argument.

## Preserving behavior

A logging-only change should preserve retries, exception handling, fallback behavior, and success/failure results. Evaluate behavioral changes separately.

For example, [`JdkHttpSender.sendInternal()`](../../exporters/sender/jdk/src/main/java/io/opentelemetry/exporter/sender/jdk/internal/JdkHttpSender.java) retries eligible `IOException`s. Propagating a response-body read failure into that loop can resend a request despite receiving an HTTP success status. This is not merely a logging change.

## Rate limiting

Aggregation limits repetition within an operation. Rate limiting limits repetition across operations. Use [`ThrottlingLogger`](../../sdk/common/src/main/java/io/opentelemetry/sdk/common/internal/ThrottlingLogger.java) for frequently recurring messages at normally enabled levels. Detailed opt-in troubleshooting logs may remain unthrottled.

A per-request limiter bounds each request independently. Use a longer-lived limiter when the combined log rate across requests must be bounded. For example, `HttpExporter` owns a `ThrottlingLogger` shared across exports.

By default, `ThrottlingLogger` permits an initial burst, announces suppression, and reduces sustained output to one record per minute. It does not count suppressed failures. Aggregate separately when that count matters.

Rate limiting output does not avoid argument evaluation. For example, `new Throwable()` is constructed before `logger.log(level, message, new Throwable())` checks its limit. If `ThrottlingLogger` API or behavior is insufficient, enhance it rather than implementing a separate limiter.
