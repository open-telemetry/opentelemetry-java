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
import io.opentelemetry.sdk.common.Clock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ThrottlingLoggerTest {
  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(ThrottlingLoggerTest.class);

  @Test
  void delegation() {
    ThrottlingLogger logger =
        new ThrottlingLogger(Logger.getLogger(ThrottlingLoggerTest.class.getName()));

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
  void fiveInAMinuteTriggersLimiting() {
    Clock clock = TestClock.create();
    ThrottlingLogger logger =
        new ThrottlingLogger(Logger.getLogger(ThrottlingLoggerTest.class.getName()), clock);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");

    logger.log(Level.WARNING, "oh no I should be suppressed!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(6);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains(
        "Too many log messages detected. Will only log once per minute from now on.");
  }

  @Test
  void allowsTrickleOfMessages() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger =
        new ThrottlingLogger(Logger.getLogger(ThrottlingLoggerTest.class.getName()), clock);
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(1);
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(2);
    clock.advanceMillis(30_001);
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    assertThat(logs.size()).isEqualTo(4);

    clock.advanceMillis(30_001);
    logger.log(Level.WARNING, "oh no 2nd minute!");
    logger.log(Level.WARNING, "oh no 2nd minute!");
    assertThat(logs.size()).isEqualTo(6);
    clock.advanceMillis(30_001);
    logger.log(Level.WARNING, "oh no 2nd minute!");
    logger.log(Level.WARNING, "oh no 2nd minute!");
    assertThat(logs.size()).isEqualTo(8);

    clock.advanceMillis(30_001);
    logger.log(Level.WARNING, "oh no 3rd minute!");
    logger.log(Level.WARNING, "oh no 3rd minute!");
    assertThat(logs.size()).isEqualTo(10);
    clock.advanceMillis(30_001);
    logger.log(Level.WARNING, "oh no 3rd minute!");
    logger.log(Level.WARNING, "oh no 3rd minute!");
    assertThat(logs.size()).isEqualTo(12);
  }

  @Test
  void afterAMinuteLetOneThrough() {
    TestClock clock = TestClock.create();
    ThrottlingLogger logger =
        new ThrottlingLogger(Logger.getLogger(ThrottlingLoggerTest.class.getName()), clock);

    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no!");

    logger.log(Level.WARNING, "oh no I should be suppressed!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");

    assertThat(logs.getEvents()).hasSize(6);
    logs.assertDoesNotContain("oh no I should be suppressed!");
    logs.assertContains(
        "Too many log messages detected. Will only log once per minute from now on.");

    clock.advanceMillis(60_001);
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(7);
    assertThat(logs.getEvents().get(6).getMessage()).isEqualTo("oh no!");

    clock.advanceMillis(60_001);
    logger.log(Level.WARNING, "oh no!");
    logger.log(Level.WARNING, "oh no I should be suppressed!");
    assertThat(logs.getEvents()).hasSize(8);
    assertThat(logs.getEvents().get(7).getMessage()).isEqualTo("oh no!");
  }
}
