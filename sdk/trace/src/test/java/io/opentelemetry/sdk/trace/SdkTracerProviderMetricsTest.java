/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.common.internal.SemConvConstants.ERROR_TYPE;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_COMPONENT_NAME;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_COMPONENT_TYPE;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_SPAN_PARENT_ORIGIN;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_SPAN_SAMPLING_RESULT;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkTracerProviderMetricsTest {

  @Mock private Sampler sampler;
  @Mock private SpanExporter mockExporter;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void componentNamesAreUniqueAndStable(boolean batching) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {
      SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
      for (int i = 0; i < 2; i++) {
        builder.addSpanProcessor(
            batching
                ? BatchSpanProcessor.builder(InMemorySpanExporter.create())
                    .setMeterProvider(() -> meterProvider)
                    .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
                    .build()
                : SimpleSpanProcessor.builder(InMemorySpanExporter.create())
                    .setMeterProvider(() -> meterProvider)
                    .build());
      }
      try (SdkTracerProvider provider = builder.build()) {
        List<String> previousNames = null;
        for (int count = 1; count <= 2; count++) {
          provider.get("test").spanBuilder("span").startSpan().end();
          assertThat(provider.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
          MetricData metric =
              reader.collectAllMetrics().stream()
                  .filter(m -> m.getName().equals("otel.sdk.processor.span.processed"))
                  .findFirst()
                  .get();
          assertThat(metric.getLongSumData().getPoints()).hasSize(2);
          List<String> names =
              metric.getLongSumData().getPoints().stream()
                  .map(point -> point.getAttributes().get(OTEL_COMPONENT_NAME))
                  .collect(Collectors.toList());
          assertThat(names).doesNotHaveDuplicates().doesNotContainNull();
          if (previousNames != null) {
            assertThat(names).containsExactlyInAnyOrderElementsOf(previousNames);
          }
          previousNames = names;
          for (LongPointData point : metric.getLongSumData().getPoints()) {
            assertThat(point.getValue()).isEqualTo(count);
          }
        }
      }
    }
  }

  @Test
  void simple() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    SimpleSpanProcessor processor =
        SimpleSpanProcessor.builder(exporter).setMeterProvider(() -> meterProvider).build();
    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(processor)
            .setMeterProvider(() -> meterProvider)
            .setSampler(sampler)
            .build();

    Tracer tracer = tracerProvider.get("test");

    setSamplingDecision(SamplingDecision.RECORD_AND_SAMPLE);
    Span span = tracer.spanBuilder("span").startSpan();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));
    span.end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasUnit("{span}")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));

    setSamplingDecision(SamplingDecision.RECORD_ONLY);
    span = tracer.spanBuilder("span").startSpan();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));
    span.end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));

    setSamplingDecision(SamplingDecision.DROP);
    span = tracer.spanBuilder("span").startSpan();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));
    span.end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));

    span =
        tracer
            .spanBuilder("span")
            .setParent(
                Context.root()
                    .with(
                        Span.wrap(
                            SpanContext.create(
                                TraceId.fromLongs(1, 2),
                                SpanId.fromLong(3),
                                TraceFlags.getDefault(),
                                TraceState.getDefault()))))
            .startSpan();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "local",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));
    span.end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "local",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));

    setSamplingDecision(SamplingDecision.RECORD_AND_SAMPLE);
    span =
        tracer
            .spanBuilder("span")
            .setParent(
                Context.root()
                    .with(
                        Span.wrap(
                            SpanContext.createFromRemoteParent(
                                TraceId.fromLongs(1, 2),
                                SpanId.fromLong(3),
                                TraceFlags.getDefault(),
                                TraceState.getDefault()))))
            .startSpan();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "remote",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "local",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));
    span.end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "remote",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "local",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));

    // Spans rejected after the call to shutdown, regardless of completion so no join.
    processor.shutdown();
    tracer.spanBuilder("span").startSpan().end();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "simple_span_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "simple_span_processor"),
                                            equalTo(ERROR_TYPE, "already_shutdown")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "remote",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_ONLY")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "local",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "DROP")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")),
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_ONLY")))));
  }

  @Test
  @SuppressLogger(BatchSpanProcessor.class)
  void batch() throws Exception {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    BatchSpanProcessor processor =
        BatchSpanProcessor.builder(mockExporter)
            .setMaxQueueSize(1)
            .setMaxExportBatchSize(1)
            // Manually flush
            .setScheduleDelay(Duration.ofDays(1))
            .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
            .setMeterProvider(() -> meterProvider)
            .build();
    TracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(processor)
            .setMeterProvider(() -> meterProvider)
            .setSampler(Sampler.alwaysOn())
            .build();

    Tracer tracer = tracerProvider.get("test");

    CompletableResultCode result1 = new CompletableResultCode();
    CompletableResultCode result2 = new CompletableResultCode();
    when(mockExporter.export(any())).thenReturn(result1).thenReturn(result2);

    // Will immediately be processed.
    tracer.spanBuilder("span").startSpan().end();
    Thread.sleep(500); // give time to start processing a batch of size 1
    // We haven't completed the export so this span is queued.
    tracer.spanBuilder("span").startSpan().end();
    // Queue is full, this span is dropped.
    tracer.spanBuilder("span").startSpan().end();

    // Export hasn't finished but processed metric is incremented
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.capacity")
                    .hasUnit("{span}")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.size")
                    .hasUnit("{span}")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasUnit("{span}")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_span_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(3)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));

    result1.succeed();
    result2.fail();
    processor.forceFlush().join(1, TimeUnit.SECONDS);

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.capacity")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_span_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(3)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));

    lenient().when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    // Spans rejected after the call to shutdown, regardless of completion so no join.
    processor.shutdown();

    tracer.spanBuilder("span").startSpan().end();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.capacity")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_span_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_span_processor"),
                                            equalTo(ERROR_TYPE, "already_shutdown")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_span_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_span_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(4)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));
  }

  @Test
  void simpleExportError() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    TracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(
                SimpleSpanProcessor.builder(mockExporter)
                    .setMeterProvider(() -> meterProvider)
                    .build())
            .setMeterProvider(() -> meterProvider)
            .setSampler(Sampler.alwaysOn())
            .build();

    Tracer tracer = tracerProvider.get("test");

    when(mockExporter.export(any())).thenReturn(CompletableResultCode.ofFailure());

    tracer.spanBuilder("span").startSpan().end();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_span_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.started")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_PARENT_ORIGIN,
                                                "none",
                                                OTEL_SPAN_SAMPLING_RESULT,
                                                "RECORD_AND_SAMPLE")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.span.live")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_SPAN_SAMPLING_RESULT, "RECORD_AND_SAMPLE")))));
  }

  private void setSamplingDecision(SamplingDecision decision) {
    when(sampler.shouldSample(any(), any(), any(), any(), any(), any()))
        .thenReturn(SamplingResult.create(decision));
  }
}
