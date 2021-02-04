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
  void isDefaultSampled() {
    assertThat(TraceFlags.getDefault()).isEqualTo((byte) 0x0);
  }

  @Test
  void toByteFromBase16() {
    assertThat(TraceFlags.fromHexCharacters('f', 'f')).isEqualTo((byte) 0xff);
    assertThat(TraceFlags.fromHexCharacters('0', '1')).isEqualTo((byte) 0x1);
    assertThat(TraceFlags.fromHexCharacters('0', '5')).isEqualTo((byte) 0x5);
    assertThat(TraceFlags.fromHexCharacters('0', '0')).isEqualTo((byte) 0x0);
  }
}
