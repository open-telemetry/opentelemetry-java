/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SpanExporterConfigurationTest {

  // Timeout difficult to test using real exports so just check implementation detail here.
  @Test
  void configureOtlpTimeout() {
    SpanExporter exporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            ConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.otlp.timeout", "10")));
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OtlpGrpcSpanExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("timeoutNanos")
                      .isEqualTo(TimeUnit.MILLISECONDS.toNanos(10L)));
    } finally {
      exporter.shutdown();
    }
  }
}
