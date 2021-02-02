/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetricsStringUtilsTest {

  @Test
  void isValidMetricName() {
    assertThat(MetricsStringUtils.isValidMetricName("")).isFalse();
    assertThat(
            MetricsStringUtils.isValidMetricName(
                String.valueOf(new char[MetricsStringUtils.METRIC_NAME_MAX_LENGTH + 1])))
        .isFalse();
    assertThat(MetricsStringUtils.isValidMetricName("abcd")).isTrue();
    assertThat(MetricsStringUtils.isValidMetricName("ab.cd")).isTrue();
    assertThat(MetricsStringUtils.isValidMetricName("ab12cd")).isTrue();
    assertThat(MetricsStringUtils.isValidMetricName("1abcd")).isFalse();
    assertThat(MetricsStringUtils.isValidMetricName("ab*cd")).isFalse();
  }
}
