/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder.nameEquals;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.internal.TracerConfig.disabled;

import io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder;
import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExtendedTracerTest {

  /**
   * {@link ExtendedTracer#spanBuilder(String)} delegates to {@link SdkTracer#spanBuilder(String)}
   * and casts the result to {@link ExtendedSpanBuilder}. Therefore, we need to confirm that {@link
   * SdkTracer#spanBuilder(String)} correctly returns {@link ExtendedSpanBuilder} and not {@link
   * io.opentelemetry.api.trace.SpanBuilder} in all cases, else the user will get {@link
   * ClassCastException}.
   */
  @ParameterizedTest
  @MethodSource("spanBuilderArgs")
  void spanBuilder(Supplier<ExtendedSpanBuilder> spanBuilderSupplier) {
    ExtendedSpanBuilder spanBuilder = spanBuilderSupplier.get();
    assertThat(spanBuilder).isInstanceOf(ExtendedSpanBuilder.class);
  }

  private static Stream<Arguments> spanBuilderArgs() {
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(InMemorySpanExporter.create()))
            .addTracerConfiguratorCondition(nameEquals("tracerB"), disabled())
            .build();

    ExtendedTracer tracerA = (ExtendedTracer) tracerProvider.get("tracerA");
    ExtendedTracer tracerB = (ExtendedTracer) tracerProvider.get("tracerB");

    SdkTracerProvider tracerProvider2 =
        SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(InMemorySpanExporter.create()))
            .build();
    ExtendedTracer tracerC = (ExtendedTracer) tracerProvider.get("tracerC");
    tracerProvider2.shutdown();

    return Stream.of(
        // Simple case
        Arguments.of(spanBuilderSupplier(() -> tracerA.spanBuilder("span"))),
        // Disabled tracer
        Arguments.of(spanBuilderSupplier(() -> tracerB.spanBuilder("span"))),
        // Invalid span name
        Arguments.of(spanBuilderSupplier(() -> tracerB.spanBuilder(null))),
        Arguments.of(spanBuilderSupplier(() -> tracerB.spanBuilder(" "))),
        // Shutdown tracer provider
        Arguments.of(spanBuilderSupplier(() -> tracerC.spanBuilder("span"))));
  }

  private static Supplier<ExtendedSpanBuilder> spanBuilderSupplier(
      Supplier<ExtendedSpanBuilder> spanBuilderSupplier) {
    return spanBuilderSupplier;
  }
}
