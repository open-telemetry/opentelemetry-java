/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.sdk.internal.SemConvAttributes.ERROR_TYPE;
import static io.opentelemetry.sdk.internal.SemConvAttributes.OTEL_COMPONENT_NAME;
import static io.opentelemetry.sdk.internal.SemConvAttributes.OTEL_COMPONENT_TYPE;
import static io.opentelemetry.sdk.internal.SemConvAttributes.OTEL_SPAN_PARENT_ORIGIN;
import static io.opentelemetry.sdk.internal.SemConvAttributes.OTEL_SPAN_SAMPLING_RESULT;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
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
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkTracerProviderMetricsTest {

  @Mock private Sampler sampler;
  @Mock private SpanExporter mockExporter;

  @Test
  void simple() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    InMemorySpanExporter exporter = InMemorySpanExporter.create();
    TracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(
                SimpleSpanProcessor.builder(exporter).setMeterProvider(() -> meterProvider).build())
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
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor")))),
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
  }

  @Test
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor",
                                                ERROR_TYPE,
                                                "queue_full")))),
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.span.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor",
                                                ERROR_TYPE,
                                                "export_failed")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_span_processor",
                                                ERROR_TYPE,
                                                "queue_full")))),
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
    processor.shutdown();
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
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_span_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_span_processor",
                                                ERROR_TYPE,
                                                "export_failed")))),
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
