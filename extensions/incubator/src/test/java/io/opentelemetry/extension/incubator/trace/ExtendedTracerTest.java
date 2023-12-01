/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Named.named;

import com.google.errorprone.annotations.Keep;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.extension.incubator.propagation.Propagation;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.assertj.SpanDataAssert;
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.SemanticAttributes;
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
                extendedTracer.run(
                    "test",
                    () -> {
                      // runs in span
                      throw new IllegalStateException("ex");
                    }));

    String result =
        extendedTracer.call(
            "another test",
            () -> {
              // runs in span
              return "result";
            });
    assertThat(result).isEqualTo("result");

    otelTesting
        .assertTraces()
        .hasSize(2)
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
                                            OpenTelemetryAssertions.equalTo(
                                                SemanticAttributes.EXCEPTION_TYPE,
                                                "java.lang.IllegalStateException"),
                                            OpenTelemetryAssertions.satisfies(
                                                SemanticAttributes.EXCEPTION_STACKTRACE,
                                                string ->
                                                    string.contains(
                                                        "java.lang.IllegalStateException: ex")),
                                            OpenTelemetryAssertions.equalTo(
                                                SemanticAttributes.EXCEPTION_MESSAGE, "ex")))),
            trace -> trace.hasSpansSatisfyingExactly(a -> a.hasName("another test")));
  }

  @Test
  void propagation() {
    extendedTracer.run(
        "parent",
        () -> {
          ContextPropagators propagators = otelTesting.getOpenTelemetry().getPropagators();
          Map<String, String> propagationHeaders =
              Propagation.getTextMapPropagationContext(propagators);
          assertThat(propagationHeaders).hasSize(1).containsKey("traceparent");

          extendedTracer.traceServerSpan(
              propagationHeaders, extendedTracer.spanBuilder("child"), () -> null, propagators);
        });

    otelTesting
        .assertTraces()
        .hasSize(1)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    SpanDataAssert::hasNoParent, span -> span.hasParent(trace.getSpan(0))));
  }

  @Test
  void callWithBaggage() {
    String value =
        extendedTracer.call(
            "parent",
            () ->
                ExtendedTracer.callWithBaggage(
                    Collections.singletonMap("key", "value"),
                    () -> Baggage.current().getEntryValue("key")));

    assertThat(value).isEqualTo("value");
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
                        t.traceServerSpan(
                            Collections.emptyMap(),
                            t.spanBuilder("span"),
                            c,
                            otelTesting.getOpenTelemetry().getPropagators()),
                    SpanKind.SERVER,
                    StatusData.error()))),
        Arguments.of(
            named(
                "server - ignore exception",
                new ExtractAndRunParameter(
                    (t, c) ->
                        t.traceServerSpan(
                            Collections.emptyMap(),
                            t.spanBuilder("span"),
                            c,
                            otelTesting.getOpenTelemetry().getPropagators(),
                            ignoreException),
                    SpanKind.SERVER,
                    StatusData.unset()))),
        Arguments.of(
            named(
                "consumer",
                new ExtractAndRunParameter(
                    (t, c) ->
                        t.traceConsumerSpan(
                            Collections.emptyMap(),
                            t.spanBuilder("span"),
                            c,
                            otelTesting.getOpenTelemetry().getPropagators()),
                    SpanKind.CONSUMER,
                    StatusData.error()))),
        Arguments.of(
            named(
                "consumer - ignore exception",
                new ExtractAndRunParameter(
                    (t, c) ->
                        t.traceConsumerSpan(
                            Collections.emptyMap(),
                            t.spanBuilder("span"),
                            c,
                            otelTesting.getOpenTelemetry().getPropagators(),
                            ignoreException),
                    SpanKind.CONSUMER,
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
        .hasSize(1)
        .hasTracesSatisfyingExactly(
            trace ->
                trace.hasSpansSatisfyingExactly(
                    span -> span.hasKind(parameter.wantKind).hasStatus(parameter.wantStatus)));
  }
}
