/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.opentelemetry.sdk.common.internal.StandardComponentId;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class GrpcExporterTest {

  @Test
  void build_NoGrpcSenderProvider() {
    assertThatThrownBy(
            () ->
                new GrpcExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        Duration.ofSeconds(10),
                        new URI("http://localhost"),
                        "service/method")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-managed-channel");
  }
}
