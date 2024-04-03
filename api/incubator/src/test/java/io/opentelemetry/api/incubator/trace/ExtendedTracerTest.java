/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Named.named;

import com.google.errorprone.annotations.Keep;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.testing.assertj.SpanDataAssert;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.SemanticAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExtendedTracerTest {

  interface ThrowingBiConsumer<T, U> {
    void accept(T t, U u) throws Throwable;
  }

  @RegisterExtension
  static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

  private final ExtendedTracer extendedTracer =
      ExtendedTracer.create(otelTesting.getOpenTelemetry().getTracer("test"));

  @Test
  void wrapInSpan() {
    assertThatIllegalStateException()
        .isThrownBy(
            () ->
                extendedTracer
                    .spanBuilder("test")
                    .startAndRun(
                        () -> {
                          // runs in span
                          throw new IllegalStateException("ex");
                        }));

    String result =
        extendedTracer
            .spanBuilder("another test")
            .startAndCall(
                () -> {
                  // runs in span
                  return "result";
                });
    assertThat(result).isEqualTo("result");

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    span ->
                        span.hasName("test")
                            .hasStatus(StatusData.error())
                            .hasEventsSatisfyingExactly(
                                event ->
                                    event
                                        .hasName("exception")
                                        .hasAttributesSatisfyingExactly(
                                            equalTo(
                                                SemanticAttributes.EXCEPTION_TYPE,
                                                "java.lang.IllegalStateException"),
                                            satisfies(
                                                SemanticAttributes.EXCEPTION_STACKTRACE,
                                                string ->
                                                    string.contains(
                                                        "java.lang.IllegalStateException: ex")),
                                            equalTo(SemanticAttributes.EXCEPTION_MESSAGE, "ex")))),
            trace -> trace.hasSpansSatisfyingExactly(a -> a.hasName("another test")));
  }

  @Test
  void propagation() {
    extendedTracer
        .spanBuilder("parent")
        .startAndRun(
            () -> {
              ContextPropagators propagators = otelTesting.getOpenTelemetry().getPropagators();
              Map<String, String> propagationHeaders =
                  ExtendedContextPropagators.getTextMapPropagationContext(propagators);
              assertThat(propagationHeaders).hasSize(1).containsKey("traceparent");

              // make sure the parent span is not stored in a thread local anymore
              Span invalid = Span.getInvalid();
              //noinspection unused
              try (Scope unused = invalid.makeCurrent()) {
                extendedTracer
                    .spanBuilder("child")
                    .setSpanKind(SpanKind.SERVER)
                    .setParent(Context.current())
                    .setNoParent()
                    .setParentFrom(propagators, propagationHeaders)
                    .setAttribute(
                        "key",
                        "value") // any method can be called here on the span (and we increase the
                    // test coverage)
                    .setAttribute("key2", 0)
                    .setAttribute("key3", 0.0)
                    .setAttribute("key4", false)
                    .setAttribute(SemanticAttributes.CLIENT_PORT, 1234L)
                    .addLink(invalid.getSpanContext())
                    .addLink(invalid.getSpanContext(), Attributes.empty())
                    .setAllAttributes(Attributes.empty())
                    .setStartTimestamp(0, java.util.concurrent.TimeUnit.NANOSECONDS)
                    .setStartTimestamp(Instant.MIN)
                    .startAndRun(() -> {});
              }
            });

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    SpanDataAssert::hasNoParent, span -> span.hasParent(trace.getSpan(0))));
  }

  private static class ExtractAndRunParameter {
    private final ThrowingBiConsumer<ExtendedTracer, SpanCallable<Void, Throwable>> extractAndRun;
    private final SpanKind wantKind;
    private final StatusData wantStatus;

    private ExtractAndRunParameter(
        ThrowingBiConsumer<ExtendedTracer, SpanCallable<Void, Throwable>> extractAndRun,
        SpanKind wantKind,
        StatusData wantStatus) {
      this.extractAndRun = extractAndRun;
      this.wantKind = wantKind;
      this.wantStatus = wantStatus;
    }
  }

  @Keep
  private static Stream<Arguments> extractAndRun() {
    BiConsumer<Span, Throwable> ignoreException =
        (span, throwable) -> {
          // ignore
        };
    return Stream.of(
        Arguments.of(
            named(
                "server",
                new ExtractAndRunParameter(
                    (t, c) ->
                        t.spanBuilder("span")
                            .setSpanKind(SpanKind.SERVER)
                            .setParentFrom(
                                otelTesting.getOpenTelemetry().getPropagators(),
                                Collections.emptyMap())
                            .startAndCall(c),
                    SpanKind.SERVER,
                    StatusData.error()))),
        Arguments.of(
            named(
                "server - ignore exception",
                new ExtractAndRunParameter(
                    (t, c) ->
                        t.spanBuilder("span")
                            .setSpanKind(SpanKind.SERVER)
                            .setParentFrom(
                                otelTesting.getOpenTelemetry().getPropagators(),
                                Collections.emptyMap())
                            .startAndCall(c, ignoreException),
                    SpanKind.SERVER,
                    StatusData.unset()))));
  }

  @ParameterizedTest
  @MethodSource
  void extractAndRun(ExtractAndRunParameter parameter) {
    assertThatException()
        .isThrownBy(
            () ->
                parameter.extractAndRun.accept(
                    extendedTracer,
                    () -> {
                      throw new RuntimeException("ex");
                    }));

    otelTesting
        .assertTraces()
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    span -> span.hasKind(parameter.wantKind).hasStatus(parameter.wantStatus)));
  }
}
