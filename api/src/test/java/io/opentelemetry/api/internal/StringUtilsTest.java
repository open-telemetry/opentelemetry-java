/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void isPrintableString() {
    assertThat(StringUtils.isPrintableString("abcd")).isTrue();
    assertThat(StringUtils.isPrintableString("\2ab\3cd")).isFalse();
  }
}
