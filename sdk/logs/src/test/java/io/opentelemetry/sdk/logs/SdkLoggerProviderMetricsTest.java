/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.common.internal.SemConvConstants.ERROR_TYPE;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_COMPONENT_NAME;
import static io.opentelemetry.sdk.common.internal.SemConvConstants.OTEL_COMPONENT_TYPE;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessorBuilder;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
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
class SdkLoggerProviderMetricsTest {

  @Mock private LogRecordExporter mockExporter;

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void componentNamesAreUniqueAndStable(boolean batching) {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {
      SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
      for (int i = 0; i < 2; i++) {
        builder.addLogRecordProcessor(
            batching
                ? BatchLogRecordProcessor.builder(InMemoryLogRecordExporter.create())
                    .setMeterProvider(() -> meterProvider)
                    .setInternalTelemetryVersion(InternalTelemetryVersion.LATEST)
                    .build()
                : SimpleLogRecordProcessor.builder(InMemoryLogRecordExporter.create())
                    .setMeterProvider(() -> meterProvider)
                    .build());
      }
      try (SdkLoggerProvider provider = builder.build()) {
        List<String> previousNames = null;
        for (int count = 1; count <= 2; count++) {
          provider.get("test").logRecordBuilder().emit();
          assertThat(provider.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
          MetricData metric =
              reader.collectAllMetrics().stream()
                  .filter(m -> m.getName().equals("otel.sdk.processor.log.processed"))
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

    InMemoryLogRecordExporter exporter = InMemoryLogRecordExporter.create();
    SimpleLogRecordProcessor processor =
        SimpleLogRecordProcessor.builder(exporter).setMeterProvider(() -> meterProvider).build();
    SdkLoggerProvider loggerProvider =
        SdkLoggerProvider.builder()
            .addLogRecordProcessor(processor)
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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_log_processor")))));

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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "simple_log_processor")))));

    // Logs rejected after the call to shutdown, regardless of completion so no join.
    processor.shutdown();
    logger.logRecordBuilder().emit();

    assertThat(metricReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(3).hasAttributes())),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "simple_log_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "simple_log_processor"),
                                            equalTo(ERROR_TYPE, "already_shutdown")))));
  }

  @Test
  @SuppressLogger(BatchLogRecordProcessorBuilder.class)
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
    // We haven't completed the export so this log is queued.
    logger.logRecordBuilder().emit();
    // Queue is full, this log is dropped.
    logger.logRecordBuilder().emit();

    // Export hasn't finished but processed metric is incremented
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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(3).hasAttributes())));

    lenient().when(mockExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
    // Logs rejected after the call to shutdown, regardless of completion so no join.
    processor.shutdown();

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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.queue.size")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(0)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(
                                                OTEL_COMPONENT_TYPE, "batching_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.processor.log.processed")
                    .hasLongSumSatisfying(
                        s ->
                            s.hasPointsSatisfying(
                                p ->
                                    p.hasValue(2)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor"),
                                            equalTo(ERROR_TYPE, "already_shutdown")),
                                p ->
                                    p.hasValue(1)
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("batching_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "batching_log_processor"),
                                            equalTo(ERROR_TYPE, "queue_full")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(4).hasAttributes())));
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
                                        .hasAttributesSatisfyingExactly(
                                            satisfies(
                                                OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches("simple_log_processor/[0-9]+")),
                                            equalTo(OTEL_COMPONENT_TYPE, "simple_log_processor")))),
            m ->
                assertThat(m)
                    .hasName("otel.sdk.log.created")
                    .hasLongSumSatisfying(
                        s -> s.hasPointsSatisfying(p -> p.hasValue(1).hasAttributes())));
  }
}
