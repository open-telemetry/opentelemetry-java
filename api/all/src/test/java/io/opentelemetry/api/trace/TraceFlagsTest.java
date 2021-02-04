/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceFlags}. */
class TraceFlagsTest {

  @Test
  void defaultInstances() {
    assertThat(TraceFlags.getDefault()).isEqualTo("00");
    assertThat(TraceFlags.getSampled()).isEqualTo("01");
  }

  @Test
  void isSampled() {
    assertThat(TraceFlags.isSampled("ff")).isTrue();
    assertThat(TraceFlags.isSampled("01")).isTrue();
    assertThat(TraceFlags.isSampled("05")).isTrue();
    assertThat(TraceFlags.isSampled("00")).isFalse();
  }

  @Test
  void fromBuffer() {
    assertThat(TraceFlags.fromBuffer("ff", 0)).isEqualTo("ff");
    assertThat(TraceFlags.fromBuffer("01", 0)).isEqualTo("01");
    assertThat(TraceFlags.fromBuffer("05", 0)).isEqualTo("05");
    assertThat(TraceFlags.fromBuffer("00", 0)).isEqualTo("00");
  }

  @Test
  void toFromByte() {
    for (int i = 0; i < 256; i++) {
      assertThat(TraceFlags.asByte(TraceFlags.fromByte((byte) i))).isEqualTo((byte) i);
    }
  }
}
