/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(loggerName = "io.opentelemetry.usage")
class ApiUsageLoggerTest {

  @RegisterExtension
  LogCapturer apiUsageLogs = LogCapturer.create().captureForLogger("io.opentelemetry.usage");

  @Test
  void log() {
    ApiUsageLogger.log(ApiUsageLoggerTest.class, "log", "thing went wrong", Level.WARNING);
    apiUsageLogs.assertContains("ApiUsageLoggerTest.log(): thing went wrong");
  }
}
