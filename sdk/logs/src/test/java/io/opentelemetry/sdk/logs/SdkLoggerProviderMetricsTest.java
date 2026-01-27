/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.common.internal.SemConvAttributes.ERROR_TYPE;
import static io.opentelemetry.sdk.common.internal.SemConvAttributes.OTEL_COMPONENT_NAME;
import static io.opentelemetry.sdk.common.internal.SemConvAttributes.OTEL_COMPONENT_TYPE;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SdkLoggerProviderMetricsTest {

  @Mock private LogRecordExporter mockExporter;

  @Test
  void simple() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    LoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                SimpleLogRecordProcessor.builder(exporter)
                    .setMeterProvider(() -> meterProvider)
                    .build())
            .setMeterProvider(() -> meterProvider)
            .build();

    Logger logger = loggerProvider.get("test");

    logger.logRecordBuilder().emit();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(1).hasAttributes())),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_log_processor")))));

    logger.logRecordBuilder().emit();
    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(2).hasAttributes())),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_log_processor")))));
  }

  @Test
  void batch() throws Exception {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    BatchLogRecordProcessor processor =
        BatchLogRecordProcessor.builder(mockExporter)
            .setMaxQueueSize(1)
            // Manually flush
            .setScheduleDelay(Duration.ofDays(1))
            .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
            .setMeterProvider(() -> meterProvider)
            .build();
    LoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(processor)
            .setMeterProvider(() -> meterProvider)
            .build();

    Logger logger = loggerProvider.get("test");

    CompletableResultCode result1 = new CompletableResultCode();
    CompletableResultCode result2 = new CompletableResultCode();
    when(mockExporter.export(any())).thenReturn(result1).thenReturn(result2);

    // Will immediately be processed.
    logger.logRecordBuilder().emit();
    Thread.sleep(500); // give time to start processing a batch of size 1
    // We haven't completed the export so this span is queued.
    logger.logRecordBuilder().emit();
    // Queue is full, this span is dropped.
    logger.logRecordBuilder().emit();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.capacity")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor",
                                                ERROR_TYPE,
                                                "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(3).hasAttributes())));

    result1.succeed();
    result2.fail();
    processor.forceFlush().join(1, TimeUnit.SECONDS);

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.capacity")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor",
                                                ERROR_TYPE,
                                                "export_failed")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "batching_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "batching_log_processor",
                                                ERROR_TYPE,
                                                "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(3).hasAttributes())));

    lenient().when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    processor.shutdown();
  }

  @Test
  void simpleExportError() {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    MeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(metricReader).build();

    LoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(
                SimpleLogRecordProcessor.builder(mockExporter)
                    .setMeterProvider(() -> meterProvider)
                    .build())
            .setMeterProvider(() -> meterProvider)
            .build();

    Logger logger = loggerProvider.get("test");

    when(mockExporter.export(any())).thenReturn(CompletableResultCode.ofFailure());

    logger.logRecordBuilder().emit();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributes(
                                            Attributes.of(
                                                OTEL_COMPONENT_NAME,
                                                "simple_log_processor/0",
                                                OTEL_COMPONENT_TYPE,
                                                "simple_log_processor",
                                                ERROR_TYPE,
                                                "export_failed")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(1).hasAttributes())));
  }
}
