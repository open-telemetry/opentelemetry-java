/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

@SuppressLogger(loggerName = "io.opentelemetry.usage")
class ApiUsageLoggerTest {

  @RegisterExtension
  LogCapturer apiUsageLogs =
      LogCapturer.create().captureForLogger("io.opentelemetry.usage", Level.TRACE);

  @BeforeEach
  void resetWarnOnce() throws Exception {
    Field warnOnce = ApiUsageLogger.class.getDeclaredField("WARN_ONCE");
    warnOnce.setAccessible(true);
    ((AtomicBoolean) warnOnce.get(null)).set(false);
  }

  @Test
  void log() {
    ApiUsageLogger.logUsageIssue(ApiUsageLoggerTest.class, "log", "thing went wrong");
    apiUsageLogs.assertContains("ApiUsageLoggerTest.log(): thing went wrong");
  }

  @Test
  void logNullParam() {
    ApiUsageLogger.logNullParam(ApiUsageLoggerTest.class, "logNullParam", "myParam");
    apiUsageLogs.assertContains("ApiUsageLoggerTest.logNullParam(): myParam is null");
  }

  @Test
  void warnOnce() {
    ApiUsageLogger.logUsageIssue(ApiUsageLoggerTest.class, "warnOnce", "first");
    ApiUsageLogger.logUsageIssue(ApiUsageLoggerTest.class, "warnOnce", "second");
    long count =
        apiUsageLogs.getEvents().stream()
            .filter(e -> e.getMessage().contains("OpenTelemetry API usage issue detected"))
            .count();
    assertEquals(1, count);
  }
}
