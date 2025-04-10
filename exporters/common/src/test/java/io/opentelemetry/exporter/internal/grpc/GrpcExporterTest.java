/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.sdk.internal.ComponentId;
import org.junit.jupiter.api.Test;

class GrpcExporterTest {

  @Test
  void build_NoGrpcSenderProvider() {
    assertThatThrownBy(
            () ->
                new GrpcExporterBuilder<>(
                        "exporter", ExporterMetrics.Signal.SPAN, "testing",10, new URI("http://localhost"), null, "/path")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-upstream");
  }
}
