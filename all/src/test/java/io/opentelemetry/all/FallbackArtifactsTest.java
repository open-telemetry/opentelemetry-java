/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.all;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This test asserts that artifacts which are no longer published continue to be referenced in
 * {@code opentelemetry-bom}.
 */
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

  @Test
  void sdkExtensionAws() {
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.BeanstalkResource");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.BeanstalkResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.Ec2Resource");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.Ec2ResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.EcsResource");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.EcsResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.EksResource");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.EksResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.LambdaResource");
    classAvailable("io.opentelemetry.sdk.extension.aws.resource.LambdaResourceProvider");
    classAvailable("io.opentelemetry.sdk.extension.aws.trace.AwsXrayIdGenerator");
  }

  @Test
  void extensionAws() {
    classAvailable("io.opentelemetry.extension.aws.AwsConfigurablePropagator");
    classAvailable("io.opentelemetry.extension.aws.AwsXrayPropagator");
  }

  private static void classAvailable(String fqcn) {
    Assertions.assertThatCode(() -> Class.forName(fqcn)).doesNotThrowAnyException();
  }
}
