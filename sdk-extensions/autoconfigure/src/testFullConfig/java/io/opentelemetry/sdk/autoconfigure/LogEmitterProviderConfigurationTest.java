/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.SystemOutLogExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LogEmitterProviderConfigurationTest {

  @Test
  void configureLogEmitterProvider() {
    Map<String, String> properties = Collections.singletonMap("otel.logs.exporter", "otlp");

    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    SdkLogEmitterProvider logEmitterProvider =
        LogEmitterProviderConfiguration.configureLogEmitterProvider(
            resource, DefaultConfigProperties.createForTest(properties), MeterProvider.noop());
    try {
      assertThat(logEmitterProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState -> {
                assertThat(sharedState).extracting("resource").isEqualTo(resource);
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
                        });
              });
    } finally {
      logEmitterProvider.shutdown();
    }
  }

  @Test
  void configureSpanProcessors_multipleExportersWithLogging() {
    LogExporter loggingExporter = SystemOutLogExporter.create();
    LogExporter otlpExporter = OtlpGrpcLogExporter.builder().build();

    assertThat(
            LogEmitterProviderConfiguration.configureLogProcessors(
                ImmutableMap.of("logging", loggingExporter, "otlp", otlpExporter),
                MeterProvider.noop()))
        .hasSize(2)
        .hasAtLeastOneElementOfType(SimpleLogProcessor.class)
        .hasAtLeastOneElementOfType(BatchLogProcessor.class);
  }
}
