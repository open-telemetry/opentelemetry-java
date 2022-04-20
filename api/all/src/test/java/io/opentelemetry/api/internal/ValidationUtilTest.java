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
  void checkValidInstrumentName_InvalidNameLogs() {
    assertThat(ValidationUtil.checkValidInstrumentName("1", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Instrument name \"1\" is invalid, returning noop instrument. Instrument names must consist of 63 or less characters including alphanumeric, _, ., -, and start with a letter. suffix");
  }

  @Test
  void checkValidInstrumentName() {
    // Valid names
    assertThat(ValidationUtil.checkValidInstrumentName("f")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("F")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("foo")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("a1")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("a.")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("abcdefghijklmnopqrstuvwxyz")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("a1234567890")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName("a_-.")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentName(new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(ValidationUtil.checkValidInstrumentName(null)).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("")).isFalse();
    // Must start with a letter
    assertThat(ValidationUtil.checkValidInstrumentName("1")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName(".")).isFalse();
    // Illegal characters
    assertThat(ValidationUtil.checkValidInstrumentName("a~")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a!")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a@")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a#")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a$")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a%")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a^")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a&")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a*")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a(")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a)")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a=")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a+")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a{")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a}")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a[")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a]")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a\\")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a|")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a<")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a>")).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentName("a?")).isFalse();
    // Must be 63 characters or less
    assertThat(ValidationUtil.checkValidInstrumentName(new String(new char[64]).replace('\0', 'a')))
        .isFalse();
  }

  @Test
  void checkValidInstrumentUnit_InvalidUnitLogs() {
    assertThat(ValidationUtil.checkValidInstrumentUnit("日", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Unit \"日\" is invalid. Instrument unit must be 63 or less ASCII characters." + " suffix");
  }

  @Test
  void checkValidInstrumentUnit() {
    assertThat(ValidationUtil.checkValidInstrumentUnit("a")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentUnit("A")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentUnit("foo129")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentUnit("!@#$%^&*()")).isTrue();
    assertThat(ValidationUtil.checkValidInstrumentUnit(new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(ValidationUtil.checkValidInstrumentUnit(null)).isFalse();
    assertThat(ValidationUtil.checkValidInstrumentUnit("")).isFalse();
    // Non-ascii characters
    assertThat(ValidationUtil.checkValidInstrumentUnit("日")).isFalse();
    // Must be 63 characters or less
    assertThat(ValidationUtil.checkValidInstrumentUnit(new String(new char[64]).replace('\0', 'a')))
        .isFalse();
  }
}
