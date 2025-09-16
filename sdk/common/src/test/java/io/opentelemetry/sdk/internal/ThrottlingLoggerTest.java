/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.ERROR;
import static org.slf4j.event.Level.INFO;
import static org.slf4j.event.Level.WARN;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(ThrottlingLoggerTest.class)
class ThrottlingLoggerTest {

  private static final Logger realLogger = Logger.getLogger(ThrottlingLoggerTest.class.getName());

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(ThrottlingLoggerTest.class);

  @Test
  void delegation() {
    ThrottlingLogger logger = new ThrottlingLogger(realLogger);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.INFO, "oh yes!");
    RuntimeException throwable = new RuntimeException();
    logger.log(Level.SEVERE, "secrets", throwable);

    logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(WARN), "oh no!");
    logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(INFO), "oh yes!");
    assertThat(
            logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(ERROR), "secrets")
                .getThrowable())
        .isSameAs(throwable);
  }

  @Test
  void delegationCustom() {
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, 10, 2, TimeUnit.HOURS);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.INFO, "oh yes!");
    RuntimeException throwable = new RuntimeException();
    logger.log(Level.SEVERE, "secrets", throwable);

    logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(WARN), "oh no!");
    logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(INFO), "oh yes!");
    assertThat(
            logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(ERROR), "secrets")
                .getThrowable())
        .isSameAs(throwable);
  }

  @Test
  void logsBelowLevelDontCount() {
    ThrottlingLogger logger =
        new ThrottlingLogger(Logger.getLogger(ThrottlingLoggerTest.class.getName()));

    for (int i = 0; i < 100; i++) {
      // FINE is below the default level and thus shouldn't impact the rate.
      logger.log(Level.FINE, "secrets", new RuntimeException());
    }
    logger.log(Level.INFO, "oh yes!");

    logs.assertContains(loggingEvent -> loggingEvent.getLevel().equals(INFO), "oh yes!");
  }

  @Test
  void fiveInAMinuteTriggersLimiting() {
    Clock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(7);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains(
        "Too many log messages detected. Will only log 1 time(s) per minute from now on.");
    logs.assertContains("oh no I should trigger suppression!");
  }

  @Test
  void tenInAnHourTriggersLimiting() {
    Clock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock, 10, 2, TimeUnit.HOURS);

    for (int i = 0; i < 10; i++) {
      logger.log(Level.WARNING, "oh no!");
    }

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(12);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains(
        "Too many log messages detected. Will only log 2 time(s) per hour from now on.");
    logs.assertContains("oh no I should trigger suppression!");
  }

  @Test
  void allowsTrickleOfMessages() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock);
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(1);
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(2);
    clock.advance(Duration.ofMillis(30_001));
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(4);

    clock.advance(Duration.ofMillis(30_001));
    logger.log(Level.WARNING, "oh no 2nd minute!");
    logger.log(Level.WARNING, "oh no 2nd minute!");
    assertThat(logs.size()).isEqualTo(6);
    clock.advance(Duration.ofMillis(30_001));
    logger.log(Level.WARNING, "oh no 2nd minute!");
    logger.log(Level.WARNING, "oh no 2nd minute!");
    assertThat(logs.size()).isEqualTo(8);

    clock.advance(Duration.ofMillis(30_001));
    logger.log(Level.WARNING, "oh no 3rd minute!");
    logger.log(Level.WARNING, "oh no 3rd minute!");
    assertThat(logs.size()).isEqualTo(10);
    clock.advance(Duration.ofMillis(30_001));
    logger.log(Level.WARNING, "oh no 3rd minute!");
    logger.log(Level.WARNING, "oh no 3rd minute!");
    assertThat(logs.size()).isEqualTo(12);
  }

  @Test
  void afterAMinuteLetOneThrough() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(7);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains("oh no I should trigger suppression!");
    logs.assertContains(
        "Too many log messages detected. Will only log 1 time(s) per minute from now on.");

    clock.advance(Duration.ofMillis(60_001));
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(8);
    assertThat(logs.getEvents().get(7).getMessage()).isEqualTo("oh no!");

    clock.advance(Duration.ofMillis(60_001));
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(9);
    assertThat(logs.getEvents().get(8).getMessage()).isEqualTo("oh no!");
  }

  @Test
  void afterAnHourLetTwoThrough() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock, 10, 2, TimeUnit.HOURS);

    for (int i = 0; i < 10; i++) {
      logger.log(Level.WARNING, "oh no!");
    }

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(12);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains("oh no I should trigger suppression!");
    logs.assertContains(
        "Too many log messages detected. Will only log 2 time(s) per hour from now on.");

    clock.advance(Duration.ofMinutes(61));
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(14);
    assertThat(logs.getEvents().get(13).getMessage()).isEqualTo("oh no!");

    clock.advance(Duration.ofMinutes(61));
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(16);
    assertThat(logs.getEvents().get(15).getMessage()).isEqualTo("oh no!");

    logs.assertDoesNotContain("oh no I should be suppressed!");
  }

  @Test
  void allowOnlyOneLogPerMinuteAfterSuppression() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(7);

    clock.advance(Duration.ofMillis(12_001));
    logger.log(Level.WARNING, "suppression 1");
    clock.advance(Duration.ofMillis(12_001));
    logger.log(Level.WARNING, "suppression 2");
    clock.advance(Duration.ofMillis(12_001));
    logger.log(Level.WARNING, "suppression 3");
    clock.advance(Duration.ofMillis(12_001));
    logger.log(Level.WARNING, "suppression 4");
    clock.advance(Duration.ofMillis(12_001));
    logger.log(Level.WARNING, "allowed 1");

    logs.assertDoesNotContain("suppression 1");
    logs.assertDoesNotContain("suppression 2");
    logs.assertDoesNotContain("suppression 3");
    logs.assertDoesNotContain("suppression 4");
    logs.assertContains("allowed 1");

    assertThat(logs.getEvents()).hasSize(8);
    assertThat(logs.getEvents().get(7).getMessage()).isEqualTo("allowed 1");
  }

  @Test
  void allowOnlyTwoLogPerHourAfterSuppression() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger = new ThrottlingLogger(realLogger, clock, 10, 2, TimeUnit.HOURS);

    for (int i = 0; i < 10; i++) {
      logger.log(Level.WARNING, "oh no!");
    }

    logger.log(Level.WARNING, "oh no I should trigger suppression!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(12);

    clock.advance(Duration.ofMinutes(10));
    logger.log(Level.WARNING, "suppression 1");
    clock.advance(Duration.ofMinutes(10));
    logger.log(Level.WARNING, "suppression 2");
    clock.advance(Duration.ofMinutes(10));
    clock.advance(Duration.ofSeconds(1));
    logger.log(Level.WARNING, "allowed 1");
    clock.advance(Duration.ofMinutes(10));
    logger.log(Level.WARNING, "suppression 3");
    clock.advance(Duration.ofMinutes(10));
    logger.log(Level.WARNING, "suppression 4");
    clock.advance(Duration.ofMinutes(10));
    clock.advance(Duration.ofSeconds(1));
    logger.log(Level.WARNING, "allowed 2");

    logs.assertDoesNotContain("suppression 1");
    logs.assertDoesNotContain("suppression 2");
    logs.assertDoesNotContain("suppression 3");
    logs.assertDoesNotContain("suppression 4");
    logs.assertContains("allowed 1");
    logs.assertContains("allowed 2");

    assertThat(logs.getEvents()).hasSize(14);
    assertThat(logs.getEvents().get(12).getMessage()).isEqualTo("allowed 1");
    assertThat(logs.getEvents().get(13).getMessage()).isEqualTo("allowed 2");
  }
}
