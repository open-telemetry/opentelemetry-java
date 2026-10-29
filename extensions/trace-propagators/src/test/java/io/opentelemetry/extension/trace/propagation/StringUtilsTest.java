/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void padLeft() {
    assertThat(StringUtils.padLeft("value", 10)).isEqualTo("00000value");
  }

  @Test
  void padLeft_length_does_not_exceed_minimum() {
    assertThat(StringUtils.padLeft("value", 3)).isEqualTo("value");
    assertThat(StringUtils.padLeft("value", -10)).isEqualTo("value");
    assertThat(StringUtils.padLeft("value", 0)).isEqualTo("value");
  }

  @Test
  void padLeft_throws_for_null_value() {
    assertThatThrownBy(() -> StringUtils.padLeft(null, 10))
        .isInstanceOf(NullPointerException.class);
  }
}
