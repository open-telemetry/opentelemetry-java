/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DebugConfigTest {

  @Test
  void enableForTests() {
    DebugConfig.enableForTesting(true);
    assertThat(DebugConfig.isMetricsDebugEnabled()).isTrue();
    DebugConfig.enableForTesting(false);
    assertThat(DebugConfig.isMetricsDebugEnabled()).isFalse();
  }

  @Test
  void hasActionableMessage() {
    // Ensure error message includes system property.
    assertThat(DebugConfig.getHowToEnableMessage()).contains("-D");
  }
}
