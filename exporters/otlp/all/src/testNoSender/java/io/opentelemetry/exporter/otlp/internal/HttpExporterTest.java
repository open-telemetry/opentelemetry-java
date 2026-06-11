/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.opentelemetry.sdk.common.internal.StandardComponentId;
import org.junit.jupiter.api.Test;

class HttpExporterTest {

  @Test
  void build_NoHttpSenderProvider() {
    assertThatThrownBy(
            () ->
                new HttpExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER,
                        "http://localhost")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-jdk");
  }
}
