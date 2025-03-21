/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.junit.Assert.assertNotNull;

import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;

// TODO:
final class DeclarativeConfiguredOpenTelemetrySdkBuilderTest {

  @Test
  void build() {
    // given
    String configurationFilePath =
        DeclarativeConfiguredOpenTelemetrySdkBuilderTest.class
            .getClassLoader()
            .getResource("otel-sdk-config.yaml")
            .getPath()
            .toString();

    // when
    DeclarativeConfiguredOpenTelemetrySdk sdk =
        DeclarativeConfiguredOpenTelemetrySdk.builder()
            .setConfigurationFilePath(configurationFilePath)
            .build();

    // then
    Tracer tracer =
        sdk.getOpenTelemetrySdk().getTracer("io.opentelemetry.sdk.extension.incubator.fileconfig");
    assertNotNull(tracer);
    sdk.getOpenTelemetrySdk().shutdown();
  }
}
