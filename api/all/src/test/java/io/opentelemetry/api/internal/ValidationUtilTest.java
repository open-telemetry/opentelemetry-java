/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(loggerName = ValidationUtil.API_USAGE_LOGGER_NAME)
public class ValidationUtilTest {

  @RegisterExtension
  LogCapturer apiUsageLogs =
      LogCapturer.create().captureForLogger(ValidationUtil.API_USAGE_LOGGER_NAME);

  @Test
  void isValidInstrumentName_InvalidNameLogs() {
    assertThat(ValidationUtil.isValidInstrumentUnit("日", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Unit \"日\" is invalid. Instrument unit must be 63 or less ASCII characters." + " suffix");
  }

  @Test
  void isValidInstrumentUnit() {
    assertThat(ValidationUtil.isValidInstrumentUnit("a")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentUnit("A")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentUnit("foo129")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentUnit("!@#$%^&*()")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentUnit(new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(ValidationUtil.isValidInstrumentUnit(null)).isFalse();
    assertThat(ValidationUtil.isValidInstrumentUnit("")).isFalse();
    // Non-ascii characters
    assertThat(ValidationUtil.isValidInstrumentUnit("日")).isFalse();
    // Must be 63 characters or less
    assertThat(ValidationUtil.isValidInstrumentUnit(new String(new char[64]).replace('\0', 'a')))
        .isFalse();
  }
}
