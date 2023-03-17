/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import static java.util.logging.Level.WARNING;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(ApiUsageLogger.class)
class ApiUsageLoggerTest {

  @RegisterExtension
  LogCapturer apiUsageLogs = LogCapturer.create().captureForLogger(ApiUsageLogger.class.getName());

  @Test
  void log() {
    ApiUsageLogger.log("thing", WARNING);
    apiUsageLogs.assertContains("thing");
  }
}
