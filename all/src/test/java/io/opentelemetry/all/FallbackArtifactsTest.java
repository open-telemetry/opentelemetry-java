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

  @Test
  void extensionAnnotations() {
    classAvailable("io.opentelemetry.extension.annotations.WithSpan");
    classAvailable("io.opentelemetry.extension.annotations.SpanAttribute");
  }

  @Test
  void sdkExtensionResources() {
    classAvailable("io.opentelemetry.sdk.extension.resources.ContainerResource");
    classAvailable("io.opentelemetry.sdk.extension.resources.ContainerResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.resources.HostResource");
    classAvailable("io.opentelemetry.sdk.extension.resources.HostResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.resources.OsResource");
    classAvailable("io.opentelemetry.sdk.extension.resources.OsResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.resources.ProcessResource");
    classAvailable("io.opentelemetry.sdk.extension.resources.ProcessResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.resources.ProcessRuntimeResource");
    classAvailable("io.opentelemetry.sdk.extension.resources.ProcessRuntimeResourceProvider");
  }

  private static void classAvailable(String fqcn) {
    Assertions.assertThatCode(() -> Class.forName(fqcn)).doesNotThrowAnyException();
  }
}
