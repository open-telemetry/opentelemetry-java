/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CommonTest {

  @Test
  void isTraceIdValid() {
    assertThat(Common.isTraceIdValid(null)).isFalse();
    assertThat(Common.isTraceIdValid("")).isFalse();
    assertThat(Common.isTraceIdValid("0000000000000001")).isTrue();
    assertThat(Common.isTraceIdValid("00000000000000000000000000000001")).isTrue();
    assertThat(Common.isTraceIdValid("000000000000000")).isFalse();
    assertThat(Common.isTraceIdValid("0000000000000000")).isFalse();
  }

  @Test
  void isSpanIdValid() {
    assertThat(Common.isSpanIdValid(null)).isFalse();
    assertThat(Common.isSpanIdValid("")).isFalse();
    assertThat(Common.isSpanIdValid("0000000000000001")).isTrue();
    assertThat(Common.isSpanIdValid("0000000000000000")).isFalse();
  }
}
