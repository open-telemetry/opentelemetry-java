/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.sdk.internal.ComponentId;
import org.junit.jupiter.api.Test;

class HttpExporterTest {

  @Test
  void build_NoHttpSenderProvider() {
    assertThatThrownBy(() -> new HttpExporterBuilder<>("name", ExporterMetrics.Signal.SPAN, "testing", "http://localhost").build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-jdk");
  }
}
