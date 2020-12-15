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

  @Test
  void isValidMetricName() {
    assertThat(StringUtils.isValidMetricName("")).isFalse();
    assertThat(
            StringUtils.isValidMetricName(
                String.valueOf(new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1])))
        .isFalse();
    assertThat(StringUtils.isValidMetricName("abcd")).isTrue();
    assertThat(StringUtils.isValidMetricName("ab.cd")).isTrue();
    assertThat(StringUtils.isValidMetricName("ab12cd")).isTrue();
    assertThat(StringUtils.isValidMetricName("1abcd")).isFalse();
    assertThat(StringUtils.isValidMetricName("ab*cd")).isFalse();
  }
}
