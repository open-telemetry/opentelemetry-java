/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MetricExporterConfigurationTest {
  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    OtlpGrpcMetricExporter exporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            ConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.otlp.timeout", "10")),
            SdkMeterProvider.builder().build());
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OtlpGrpcMetricExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("timeoutNanos")
                      .isEqualTo(TimeUnit.MILLISECONDS.toNanos(10L)));
    } finally {
      exporter.shutdown();
    }
  }
}
