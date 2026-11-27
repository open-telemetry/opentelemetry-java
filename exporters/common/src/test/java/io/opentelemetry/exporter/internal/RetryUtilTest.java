/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.time.Duration;
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
        argumentSet("zero seconds", "0", Instant.EPOCH, 0L),
        argumentSet("leading zeros", "0030", Instant.EPOCH, TimeUnit.SECONDS.toNanos(30)),
        argumentSet("positive sign", "+30", Instant.EPOCH, null),
        argumentSet("negative zero", "-0", Instant.EPOCH, null),
        argumentSet("non-ASCII digits", "٣٠", Instant.EPOCH, null),
        argumentSet("empty", "", Instant.EPOCH, null),
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
            0L),
        argumentSet(
            "RFC 850 date", "Friday, 17-Jul-26 00:00:45 GMT", NOW, TimeUnit.SECONDS.toNanos(45)),
        argumentSet(
            "RFC 850 date at the 50-year limit",
            "Friday, 17-Jul-76 00:00:00 GMT",
            NOW,
            Duration.between(NOW, NOW.atOffset(ZoneOffset.UTC).plusYears(50)).toNanos()),
        argumentSet(
            "RFC 850 date beyond the 50-year limit resolves to the past",
            "Saturday, 17-Jul-76 00:00:01 GMT",
            NOW,
            0L),
        argumentSet(
            "RFC 850 date in the next century",
            "Friday, 01-Jan-00 00:00:00 GMT",
            Instant.parse("2099-12-31T23:59:59Z"),
            TimeUnit.SECONDS.toNanos(1)),
        argumentSet("asctime date", "Fri Jul 17 00:00:45 2026", NOW, TimeUnit.SECONDS.toNanos(45)),
        argumentSet(
            "asctime date with a space-padded day",
            "Tue Jul  7 00:00:45 2026",
            NOW.minusSeconds(TimeUnit.DAYS.toSeconds(10)),
            TimeUnit.SECONDS.toNanos(45)),
        argumentSet("invalid RFC 850 date", "Friday, 31-Feb-26 00:00:45 GMT", NOW, null),
        argumentSet("invalid asctime date", "Fri Jul 17 24:00:00 2026", NOW, null));
  }
}
