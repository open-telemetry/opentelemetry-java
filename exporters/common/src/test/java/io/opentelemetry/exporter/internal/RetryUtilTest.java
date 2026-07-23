/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RetryUtilTest {

  @Test
  void retryAfterSeconds() {
    OptionalLong delayNanos = RetryUtil.retryAfterNanos("30", Instant.EPOCH);

    assertThat(delayNanos).hasValue(TimeUnit.SECONDS.toNanos(30));
  }

  @Test
  void retryAfterDate() {
    Instant now = Instant.parse("2026-07-17T00:00:00Z");
    String retryAfter =
        ZonedDateTime.ofInstant(now.plusSeconds(45), ZoneOffset.UTC)
            .format(DateTimeFormatter.RFC_1123_DATE_TIME);

    OptionalLong delayNanos = RetryUtil.retryAfterNanos(retryAfter, now);

    assertThat(delayNanos).hasValue(TimeUnit.SECONDS.toNanos(45));
  }

  @Test
  void retryAfterPastDateClampsToZero() {
    Instant now = Instant.parse("2026-07-17T00:00:00Z");
    String retryAfter =
        ZonedDateTime.ofInstant(now.minusSeconds(1), ZoneOffset.UTC)
            .format(DateTimeFormatter.RFC_1123_DATE_TIME);

    OptionalLong delayNanos = RetryUtil.retryAfterNanos(retryAfter, now);

    assertThat(delayNanos).hasValue(0L);
  }

  @Test
  void retryAfterMalformed() {
    OptionalLong delayNanos = RetryUtil.retryAfterNanos("bad-value", Instant.EPOCH);

    assertThat(delayNanos).isEmpty();
  }
}
