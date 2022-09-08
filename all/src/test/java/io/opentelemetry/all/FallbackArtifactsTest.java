/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FallbackArtifactsTest {

  @Test
  void exporterJaegerProto() {
    classAvailable("io.opentelemetry.exporter.jaeger.proto.api_v2.Collector");
    classAvailable("io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc");
    classAvailable("io.opentelemetry.exporter.jaeger.proto.api_v2.Model");
  }

  private static void classAvailable(String fqcn) {
    Assertions.assertThatCode(() -> Class.forName(fqcn)).doesNotThrowAnyException();
  }
}
