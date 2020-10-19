/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage.spi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.baggage.BaggageManagerSdk;
import org.junit.jupiter.api.Test;

class BaggageManagerFactorySdkTest {

  @Test
  void testDefault() {
    assertThat(OpenTelemetry.getBaggageManager()).isInstanceOf(BaggageManagerSdk.class);
  }
}
