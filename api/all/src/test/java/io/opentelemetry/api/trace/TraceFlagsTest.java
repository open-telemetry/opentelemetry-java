/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceFlags} and {@link TraceFlagsBuilder}. */
class TraceFlagsTest {

  @Test
  void defaultInstances() {
    assertThat(TraceFlags.getDefault().asHex()).isEqualTo("00");
    assertThat(TraceFlags.builder().build().asHex()).isEqualTo("00");
    assertThat(TraceFlags.builder().setSampled(true).build().asHex()).isEqualTo("01");
    assertThat(TraceFlags.builder().setSampled(false).build().asHex()).isEqualTo("00");
    assertThat(TraceFlags.builder().setRandomTraceId(true).build().asHex()).isEqualTo("02");
    assertThat(TraceFlags.builder().setRandomTraceId(false).build().asHex()).isEqualTo("00");
    assertThat(TraceFlags.builder().setSampled(true).setRandomTraceId(true).build().asHex())
        .isEqualTo("03");
    assertThat(TraceFlags.builder().setRandomTraceId(true).setSampled(true).build().asHex())
        .isEqualTo("03");
  }

  @Test
  void idempotency() {
    assertThat(TraceFlags.builder().setRandomTraceId(true).setRandomTraceId(true).build().asHex())
        .isEqualTo("02");
    assertThat(TraceFlags.builder().setSampled(true).setSampled(true).build().asHex())
        .isEqualTo("01");
  }

  @Test
  void setAndClear() {
    assertThat(TraceFlags.builder().setRandomTraceId(true).setRandomTraceId(false).build().asHex())
        .isEqualTo("00");
    assertThat(TraceFlags.builder().setSampled(true).setSampled(false).build().asHex())
        .isEqualTo("00");
  }

  @Test
  void isSampled() {
    assertThat(TraceFlags.fromByte((byte) 0xff).isSampled()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x01).isSampled()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x02).isSampled()).isFalse();
    assertThat(TraceFlags.fromByte((byte) 0x03).isSampled()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x05).isSampled()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x00).isSampled()).isFalse();
  }

  @Test
  void isTraceIdRandom() {
    assertThat(TraceFlags.fromByte((byte) 0xff).isTraceIdRandom()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x01).isTraceIdRandom()).isFalse();
    assertThat(TraceFlags.fromByte((byte) 0x02).isTraceIdRandom()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x03).isTraceIdRandom()).isTrue();
    assertThat(TraceFlags.fromByte((byte) 0x05).isTraceIdRandom()).isFalse();
    assertThat(TraceFlags.fromByte((byte) 0x00).isTraceIdRandom()).isFalse();
  }

  @Test
  void toFromHex() {
    for (int i = 0; i < 256; i++) {
      String hex = Integer.toHexString(i);
      if (hex.length() == 1) {
        hex = "0" + hex;
      }
      assertThat(TraceFlags.fromHex(hex, 0).asHex()).isEqualTo(hex);
    }
  }

  @Test
  void toFromByte() {
    for (int i = 0; i < 256; i++) {
      assertThat(TraceFlags.fromByte((byte) i).asByte()).isEqualTo((byte) i);
    }
  }
}
