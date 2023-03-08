/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal;

import static io.opentelemetry.sdk.metrics.internal.InstrumentNameValidator.checkValidInstrumentName;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class InstrumentNameValidatorTest {

  @RegisterExtension
  LogCapturer apiUsageLogs =
      LogCapturer.create().captureForLogger(InstrumentNameValidator.LOGGER_NAME);

  @Test
  void checkValidInstrumentName_InvalidNameLogs() {
    assertThat(checkValidInstrumentName("1", " suffix")).isFalse();
    apiUsageLogs.assertContains(
        "Instrument name \"1\" is invalid, returning noop instrument. Instrument names must consist of 63 or fewer characters including alphanumeric, _, ., -, and start with a letter. suffix");
  }

  @Test
  void checkValidInstrumentNameTest() {
    // Valid names
    assertThat(checkValidInstrumentName("f")).isTrue();
    assertThat(checkValidInstrumentName("F")).isTrue();
    assertThat(checkValidInstrumentName("foo")).isTrue();
    assertThat(checkValidInstrumentName("a1")).isTrue();
    assertThat(checkValidInstrumentName("a.")).isTrue();
    assertThat(checkValidInstrumentName("abcdefghijklmnopqrstuvwxyz")).isTrue();
    assertThat(checkValidInstrumentName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")).isTrue();
    assertThat(checkValidInstrumentName("a1234567890")).isTrue();
    assertThat(checkValidInstrumentName("a_-.")).isTrue();
    assertThat(checkValidInstrumentName(new String(new char[63]).replace('\0', 'a'))).isTrue();

    // Empty and null not allowed
    assertThat(checkValidInstrumentName(null)).isFalse();
    assertThat(checkValidInstrumentName("")).isFalse();
    // Must start with a letter
    assertThat(checkValidInstrumentName("1")).isFalse();
    assertThat(checkValidInstrumentName(".")).isFalse();
    // Illegal characters
    assertThat(checkValidInstrumentName("a~")).isFalse();
    assertThat(checkValidInstrumentName("a!")).isFalse();
    assertThat(checkValidInstrumentName("a@")).isFalse();
    assertThat(checkValidInstrumentName("a#")).isFalse();
    assertThat(checkValidInstrumentName("a$")).isFalse();
    assertThat(checkValidInstrumentName("a%")).isFalse();
    assertThat(checkValidInstrumentName("a^")).isFalse();
    assertThat(checkValidInstrumentName("a&")).isFalse();
    assertThat(checkValidInstrumentName("a*")).isFalse();
    assertThat(checkValidInstrumentName("a(")).isFalse();
    assertThat(checkValidInstrumentName("a)")).isFalse();
    assertThat(checkValidInstrumentName("a=")).isFalse();
    assertThat(checkValidInstrumentName("a+")).isFalse();
    assertThat(checkValidInstrumentName("a{")).isFalse();
    assertThat(checkValidInstrumentName("a}")).isFalse();
    assertThat(checkValidInstrumentName("a[")).isFalse();
    assertThat(checkValidInstrumentName("a]")).isFalse();
    assertThat(checkValidInstrumentName("a\\")).isFalse();
    assertThat(checkValidInstrumentName("a|")).isFalse();
    assertThat(checkValidInstrumentName("a<")).isFalse();
    assertThat(checkValidInstrumentName("a>")).isFalse();
    assertThat(checkValidInstrumentName("a?")).isFalse();
    // Must be 63 characters or fewer
    assertThat(checkValidInstrumentName(new String(new char[64]).replace('\0', 'a'))).isFalse();
  }
}
