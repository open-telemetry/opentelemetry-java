/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryUtil {

  private static final Set<String> RETRYABLE_GRPC_STATUS_CODES;
  private static final Set<Integer> RETRYABLE_HTTP_STATUS_CODES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));
  private static final DateTimeFormatter RFC_850_DATE =
      new DateTimeFormatterBuilder()
          .appendPattern("EEEE, dd-MMM-")
          // Keep the date unresolved until the two-digit year can be interpreted relative to now.
          .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 2, 2000)
          .appendPattern(" HH:mm:ss 'GMT'")
          .toFormatter(Locale.US)
          .withResolverStyle(ResolverStyle.STRICT);
  private static final DateTimeFormatter ASCTIME_DATE =
      DateTimeFormatter.ofPattern("EEE MMM ppd HH:mm:ss uuuu", Locale.US)
          .withResolverStyle(ResolverStyle.STRICT);

  static {
    Set<Integer> retryableGrpcStatusCodes = new HashSet<>();
    retryableGrpcStatusCodes.add(GrpcStatusCode.CANCELLED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DEADLINE_EXCEEDED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.RESOURCE_EXHAUSTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.ABORTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.OUT_OF_RANGE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.UNAVAILABLE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DATA_LOSS.getValue());
    RETRYABLE_GRPC_STATUS_CODES =
        Collections.unmodifiableSet(
            retryableGrpcStatusCodes.stream().map(Object::toString).collect(Collectors.toSet()));
  }

  private RetryUtil() {}

  /** Returns the retryable gRPC status codes. */
  public static Set<String> retryableGrpcStatusCodes() {
    return RETRYABLE_GRPC_STATUS_CODES;
  }

  /** Returns the retryable HTTP status codes. */
  public static Set<Integer> retryableHttpResponseCodes() {
    return RETRYABLE_HTTP_STATUS_CODES;
  }

  /**
   * Returns the delay specified by a {@code Retry-After} header, or empty if the value is absent or
   * malformed.
   */
  public static OptionalLong retryAfterNanos(@Nullable String retryAfter) {
    return retryAfterNanos(retryAfter, Instant.now());
  }

  static OptionalLong retryAfterNanos(@Nullable String retryAfter, Instant now) {
    if (retryAfter == null) {
      return OptionalLong.empty();
    }

    try {
      long delaySeconds = Long.parseLong(retryAfter);
      // RFC 9110 delay-seconds is 1*DIGIT. Long.parseLong also accepts signs and non-ASCII digits.
      for (int i = 0; i < retryAfter.length(); i++) {
        char digit = retryAfter.charAt(i);
        if (digit < '0' || digit > '9') {
          return OptionalLong.empty();
        }
      }
      return OptionalLong.of(TimeUnit.SECONDS.toNanos(delaySeconds));
    } catch (NumberFormatException ignored) {
      // Fall through and try the HTTP-date form.
    }

    try {
      Instant retryAfterInstant = parseHttpDate(retryAfter, now);
      long delayNanos = Duration.between(now, retryAfterInstant).toNanos();
      return OptionalLong.of(Math.max(0, delayNanos));
    } catch (DateTimeException | ArithmeticException ignored) {
      return OptionalLong.empty();
    }
  }

  private static Instant parseHttpDate(String value, Instant now) {
    try {
      return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
    } catch (DateTimeParseException ignored) {
      // RFC 9110 also requires recipients to accept the two obsolete HTTP-date formats.
    }
    if (value.indexOf(',') < 0) {
      return LocalDateTime.parse(value, ASCTIME_DATE).toInstant(ZoneOffset.UTC);
    }

    TemporalAccessor parsed = RFC_850_DATE.parse(value);
    MonthDay monthDay = MonthDay.from(parsed);
    LocalTime time = LocalTime.from(parsed);
    int twoDigitYear = parsed.get(ChronoField.YEAR_OF_ERA) % 100;
    LocalDateTime futureLimit = LocalDateTime.ofInstant(now, ZoneOffset.UTC).plusYears(50);
    // Pick the latest matching year, then check the full timestamp at the boundary.
    int year = futureLimit.getYear() - Math.floorMod(futureLimit.getYear() - twoDigitYear, 100);
    MonthDay limitDay = MonthDay.from(futureLimit);
    if (year == futureLimit.getYear()
        && (monthDay.isAfter(limitDay)
            || (monthDay.equals(limitDay) && time.isAfter(futureLimit.toLocalTime())))) {
      year -= 100;
    }

    LocalDate date = LocalDate.of(year, monthDay.getMonth(), monthDay.getDayOfMonth());
    if (date.getDayOfWeek().getValue() != parsed.get(ChronoField.DAY_OF_WEEK)) {
      throw new DateTimeException("Day of week does not match the RFC 850 date");
    }
    return date.atTime(time).toInstant(ZoneOffset.UTC);
  }
}
