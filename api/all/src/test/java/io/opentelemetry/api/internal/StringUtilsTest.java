/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  @SuppressWarnings("AvoidEscapedUnicodeCharacters")
  void isPrintableString() {
    assertThat(StringUtils.isPrintableString("abcd")).isTrue();
    assertThat(StringUtils.isPrintableString("\u0002ab")).isFalse();
    assertThat(StringUtils.isPrintableString("\u0127ab")).isFalse();
  }
}
