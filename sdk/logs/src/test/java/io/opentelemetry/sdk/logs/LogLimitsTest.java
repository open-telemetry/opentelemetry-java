/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LogLimitsTest {

  @Test
  void defaultLogLimits() {
    assertThat(LogLimits.getDefault().getMaxNumberOfAttributes()).isEqualTo(128);
    assertThat(LogLimits.getDefault().getMaxAttributeValueLength()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void updateLogLimits_All() {
    LogLimits logLimits =
        LogLimits.builder().setMaxNumberOfAttributes(8).setMaxAttributeValueLength(9).build();
    assertThat(logLimits.getMaxNumberOfAttributes()).isEqualTo(8);
    assertThat(logLimits.getMaxAttributeValueLength()).isEqualTo(9);

    // Preserves values
    LogLimits logLimitsDupe = logLimits.toBuilder().build();
    // Use reflective comparison to catch when new fields are added.
    assertThat(logLimitsDupe)
        .usingRecursiveComparison()
        .withStrictTypeChecking()
        .isEqualTo(logLimits);
  }

  @Test
  void invalidLogLimits() {
    assertThatThrownBy(() -> LogLimits.builder().setMaxNumberOfAttributes(-1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> LogLimits.builder().setMaxAttributeValueLength(-1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validLogLimits() {
    assertThatCode(() -> LogLimits.builder().setMaxNumberOfAttributes(0))
        .doesNotThrowAnyException();
    assertThatCode(() -> LogLimits.builder().setMaxAttributeValueLength(0))
        .doesNotThrowAnyException();
  }
}
