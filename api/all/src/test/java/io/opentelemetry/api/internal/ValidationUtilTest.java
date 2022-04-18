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
    assertThat(ValidationUtil.isValidInstrumentName("1", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Instrument name \"1\" is invalid, returning noop instrument. Instrument names must consist of 63 or less characters including alphanumeric, _, ., -, and start with a letter. suffix");
  }

  @Test
  void isValidInstrumentName() {
    // Valid names
    assertThat(ValidationUtil.isValidInstrumentName("f")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("F")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("foo")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("a1")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("a.")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("abcdefghijklmnopqrstuvwxyz")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("a1234567890")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName("a_-.")).isTrue();
    assertThat(ValidationUtil.isValidInstrumentName(new String(new char[63]).replace('\0', 'a')))
        .isTrue();

    // Empty and null not allowed
    assertThat(ValidationUtil.isValidInstrumentName(null)).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("")).isFalse();
    // Must start with a letter
    assertThat(ValidationUtil.isValidInstrumentName("1")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName(".")).isFalse();
    // Illegal characters
    assertThat(ValidationUtil.isValidInstrumentName("a~")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a!")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a@")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a#")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a$")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a%")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a^")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a&")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a*")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a(")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a)")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a=")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a+")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a{")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a}")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a[")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a]")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a\\")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a|")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a<")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a>")).isFalse();
    assertThat(ValidationUtil.isValidInstrumentName("a?")).isFalse();
    // Must be 63 characters or less
    assertThat(ValidationUtil.isValidInstrumentName(new String(new char[64]).replace('\0', 'a')))
        .isFalse();
  }
}
