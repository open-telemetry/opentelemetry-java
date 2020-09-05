/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.correlationcontext.spi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.sdk.correlationcontext.CorrelationContextManagerSdk;
import org.junit.jupiter.api.Test;

class CorrelationContextManagerFactorySdkTest {

  @Test
  void testDefault() {
    assertThat(OpenTelemetry.getCorrelationContextManager())
        .isInstanceOf(CorrelationContextManagerSdk.class);
  }
}
