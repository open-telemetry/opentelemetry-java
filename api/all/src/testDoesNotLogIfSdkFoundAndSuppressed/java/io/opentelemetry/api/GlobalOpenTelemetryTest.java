/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

public class GlobalOpenTelemetryTest {

  @Test
  void logIsSuppressed() {
    Logger logger = GlobalOpenTelemetry.logger;
    AtomicReference<LogRecord> logged = new AtomicReference<>();
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            logged.set(record);
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    GlobalOpenTelemetry.get();
    assertThat(logged).hasValue(null);
  }
}
