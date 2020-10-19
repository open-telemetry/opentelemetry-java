/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkTest {

  @Test
  void testDefault() {
    assertThat(((TracerSdkProvider) OpenTelemetrySdk.getTracerManagement()).get(""))
        .isSameAs(OpenTelemetry.getTracerProvider().get(""));
    assertThat(OpenTelemetrySdk.getBaggageManager()).isSameAs(OpenTelemetry.getBaggageManager());
    assertThat(OpenTelemetrySdk.getMeterProvider()).isSameAs(OpenTelemetry.getMeterProvider());
  }

  @Test
  void testShortcutVersions() {
    assertThat(OpenTelemetry.getTracer("testTracer1"))
        .isEqualTo(OpenTelemetry.getTracerProvider().get("testTracer1"));
    assertThat(OpenTelemetry.getTracer("testTracer2", "testVersion"))
        .isEqualTo(OpenTelemetry.getTracerProvider().get("testTracer2", "testVersion"));
    assertThat(OpenTelemetry.getMeter("testMeter1"))
        .isEqualTo(OpenTelemetry.getMeterProvider().get("testMeter1"));
    assertThat(OpenTelemetry.getMeter("testMeter2", "testVersion"))
        .isEqualTo(OpenTelemetry.getMeterProvider().get("testMeter2", "testVersion"));
  }
}
