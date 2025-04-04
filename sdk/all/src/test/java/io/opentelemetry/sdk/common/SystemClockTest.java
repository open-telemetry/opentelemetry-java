/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SystemClockTest {

  @Test
  void now_microsPrecision() {
    // If we test many times, we can be fairly sure we get at least one timestamp that isn't
    // coincidentally rounded to millis precision.
    int numHasMicros = 0;
    for (int i = 0; i < 100; i++) {
      long now = SystemClock.getInstance().now();
      if (now % 1000000 != 0) {
        numHasMicros++;
      }
    }
    assertThat(numHasMicros).isNotZero();
  }

  @Test
  void now_highPrecision() {
    // If we test many times, we can be fairly sure we get at least one timestamp that isn't
    // coincidentally rounded to millis precision.
    int numHasMicros = 0;
    for (int i = 0; i < 100; i++) {
      long now = SystemClock.getInstance().now(true);
      if (now % 1000000 != 0) {
        numHasMicros++;
      }
    }
    assertThat(numHasMicros).isNotZero();
  }
}
