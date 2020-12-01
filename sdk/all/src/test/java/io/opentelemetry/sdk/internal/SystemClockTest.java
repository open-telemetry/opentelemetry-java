/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

// This test is placed in the all artifact instead of the common one so it uses the dependency jar
// instead of the classes directly, which allows verifying mrjar behavior.
class SystemClockTest {

  @EnabledOnJre(JRE.JAVA_8)
  @Test
  void millisPrecision() {
    // If we test many times, we can be fairly sure we didn't just get lucky with having a rounded
    // result on a higher than expected precision timestamp.
    for (int i = 0; i < 100; i++) {
      long now = SystemClock.getInstance().now();
      assertThat(now % 1000000).isZero();
    }
  }

  @DisabledOnJre(JRE.JAVA_8)
  @Test
  void microsPrecision() {
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
}
