/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.SystemOutLogExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LoggerProviderConfigurationTest {

  @Test
  void configureLoggerProvider() {
    Map<String, String> properties = Collections.singletonMap("otel.logs.exporter", "otlp");

    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
    LoggerProviderConfiguration.configureLoggerProvider(
        builder,
        DefaultConfigProperties.createForTest(properties),
        LoggerProviderConfiguration.class.getClassLoader(),
        MeterProvider.noop(),
        (a, unused) -> a);
    SdkLoggerProvider loggerProvider = builder.build();

    try {
      assertThat(loggerProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState ->
                  assertThat(sharedState)
                      .extracting("logProcessor")
                      .isInstanceOf(BatchLogProcessor.class)
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
    } finally {
      loggerProvider.shutdown();
    }
  }

  @Test
  void configureSpanProcessors_multipleExportersWithLogging() {
    LogExporter loggingExporter = SystemOutLogExporter.create();
    LogExporter otlpExporter = OtlpGrpcLogExporter.builder().build();

    assertThat(
            LoggerProviderConfiguration.configureLogProcessors(
                ImmutableMap.of("logging", loggingExporter, "otlp", otlpExporter),
                MeterProvider.noop()))
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleLogProcessor.class)
        .hasAtLeastOneElementOfType(BatchLogProcessor.class);
  }
}
