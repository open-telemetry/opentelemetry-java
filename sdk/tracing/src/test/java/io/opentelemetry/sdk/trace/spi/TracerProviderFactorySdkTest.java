/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.spi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TracerProviderFactorySdk}. */
class TracerProviderFactorySdkTest {

  @Test
  void testDefault() {
    Tracer tracerSdk = TracerSdkProvider.builder().build().get("");
    assertThat(OpenTelemetry.getGlobalTracerProvider().get("")).isInstanceOf(tracerSdk.getClass());
  }
}
