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
    classAvailable(io.opentelemetry.exporter.jaeger.proto.api_v2.Collector.class);
    classAvailable(io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc.class);
    classAvailable(io.opentelemetry.exporter.jaeger.proto.api_v2.Model.class);
  }

  private static void classAvailable(Class<?> clazz) {
    Assertions.assertThatCode(() -> Class.forName(clazz.getName())).doesNotThrowAnyException();
  }
}
