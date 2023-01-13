/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.trace.internal.JcTools;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LoggerProviderConfigurationTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configureLoggerProvider() {
    Map<String, String> properties = Collections.singletonMap("otel.logs.exporter", "otlp");
    List<Closeable> closeables = new ArrayList<>();

    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
    LoggerProviderConfiguration.configureLoggerProvider(
        builder,
        DefaultConfigProperties.createForTest(properties),
        LoggerProviderConfiguration.class.getClassLoader(),
        MeterProvider.noop(),
        (a, unused) -> a,
        closeables);
    cleanup.addCloseables(closeables);

    try (SdkLoggerProvider loggerProvider = builder.build()) {
      assertThat(loggerProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState ->
                  assertThat(sharedState)
                      .extracting("logRecordProcessor")
                      .isInstanceOf(BatchLogRecordProcessor.class)
                      .extracting("worker")
                      .satisfies(
                          worker -> {
                            assertThat(worker)
                                .extracting("scheduleDelayNanos")
                                .isEqualTo(TimeUnit.MILLISECONDS.toNanos(200));
                            assertThat(worker)
                                .extracting("exporterTimeoutNanos")
                                .isEqualTo(TimeUnit.MILLISECONDS.toNanos(30000));
                            assertThat(worker).extracting("maxExportBatchSize").isEqualTo(512);
                            assertThat(worker)
                                .extracting("queue")
                                .isInstanceOfSatisfying(
                                    ArrayBlockingQueue.class,
                                    queue -> assertThat(queue.remainingCapacity()).isEqualTo(2048));
                          }));
      assertThat(closeables)
          .hasExactlyElementsOfTypes(
              OtlpGrpcLogRecordExporter.class, BatchLogRecordProcessor.class);
    }
  }

  @Test
  void configureLogRecordProcessors_multipleExportersWithLogging() {
    List<Closeable> closeables = new ArrayList<>();

    List<LogRecordProcessor> logRecordProcessors =
        LoggerProviderConfiguration.configureLogRecordProcessors(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            ImmutableMap.of(
                "logging",
                SystemOutLogRecordExporter.create(),
                "otlp",
                OtlpGrpcLogRecordExporter.builder().build()),
            MeterProvider.noop(),
            closeables);
    cleanup.addCloseables(closeables);

    assertThat(logRecordProcessors)
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleLogRecordProcessor.class)
        .hasAtLeastOneElementOfType(BatchLogRecordProcessor.class);
    assertThat(closeables)
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleLogRecordProcessor.class)
        .hasAtLeastOneElementOfType(BatchLogRecordProcessor.class);
  }

  @Test
  void configureBatchLogRecordProcessor() {
    Map<String, String> properties = new HashMap<>();
    properties.put("otel.blrp.schedule.delay", "100000");
    properties.put("otel.blrp.max.queue.size", "2");
    properties.put("otel.blrp.max.export.batch.size", "3");
    properties.put("otel.blrp.export.timeout", "4");

    try (BatchLogRecordProcessor processor =
        LoggerProviderConfiguration.configureBatchLogRecordProcessor(
            DefaultConfigProperties.createForTest(properties),
            SystemOutLogRecordExporter.create(),
            MeterProvider.noop())) {
      assertThat(processor)
          .extracting("worker")
          .satisfies(
              worker -> {
                assertThat(worker)
                    .extracting("scheduleDelayNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(100000));
                assertThat(worker)
                    .extracting("exporterTimeoutNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(4));
                assertThat(worker).extracting("maxExportBatchSize").isEqualTo(3);
                assertThat(worker)
                    .extracting("queue")
                    .isInstanceOfSatisfying(
                        Queue.class, queue -> assertThat(JcTools.capacity(queue)).isEqualTo(2));
                assertThat(worker)
                    .extracting("logRecordExporter")
                    .isInstanceOf(SystemOutLogRecordExporter.class);
              });
    }
  }
}
