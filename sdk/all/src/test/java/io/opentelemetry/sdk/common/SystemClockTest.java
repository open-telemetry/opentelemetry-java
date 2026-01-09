/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;

class SystemClockTest {

  @Test
  void now() {
    assertThat(SystemClock.getInstance().now()).isNotZero();
    assertThat(SystemClock.getInstance().now(true)).isNotZero();
    assertThat(SystemClock.getInstance().now(false)).isNotZero();
  }

  @Test
  // On java 8, the APIs used to produce micro precision are available but still only produce millis
  // precision
  @DisabledOnJre(JRE.JAVA_8)
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
  void now_lowPrecision() {
    // If we test many times, we can be fairly sure we didn't just get lucky with having a rounded
    // result on a higher than expected precision timestamp.
    for (int i = 0; i < 100; i++) {
      long now = SystemClock.getInstance().now(false);
      assertThat(now % 1000000).isZero();
    }
  }

  @Test
  // On java 8, the APIs used to produce micro precision are available but still only produce millis
  // precision
  @DisabledOnJre(JRE.JAVA_8)
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
