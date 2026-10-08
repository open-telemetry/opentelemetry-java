/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RetryUtilTest {

  private static final Instant NOW = Instant.parse("2026-07-17T00:00:00Z");

  @ParameterizedTest
  @MethodSource("retryAfterArgs")
  void retryAfterNanos(String retryAfter, Instant now, Long expectedNanos) {
    OptionalLong delayNanos = RetryUtil.retryAfterNanos(retryAfter, now);

    if (expectedNanos == null) {
      assertThat(delayNanos).isEmpty();
    } else {
      assertThat(delayNanos).hasValue(expectedNanos);
    }
  }

  private static Stream<Arguments> retryAfterArgs() {
    return Stream.of(
        argumentSet("null", null, Instant.EPOCH, null),
        argumentSet("seconds", "30", Instant.EPOCH, TimeUnit.SECONDS.toNanos(30)),
        argumentSet("negative seconds", "-1", Instant.EPOCH, null),
        argumentSet("malformed", "bad-value", Instant.EPOCH, null),
        argumentSet(
            "future date",
            ZonedDateTime.ofInstant(NOW.plusSeconds(45), ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME),
            NOW,
            TimeUnit.SECONDS.toNanos(45)),
        argumentSet(
            "past date clamps to zero",
            ZonedDateTime.ofInstant(NOW.minusSeconds(1), ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME),
            NOW,
            0L));
  }
}
