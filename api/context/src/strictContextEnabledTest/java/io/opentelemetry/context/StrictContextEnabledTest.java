/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

@SuppressWarnings("MustBeClosedChecker")
class StrictContextEnabledTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");

  @Test
  void garbageCollectedScope() {
    Logger logger = Logger.getLogger(StrictContextStorage.class.getName());
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
    try {
      Context.current().with(ANIMAL, "cat").makeCurrent();

      await()
          .atMost(Duration.ofSeconds(30))
          .untilAsserted(
              () -> {
                System.gc();
                assertThat(logged).doesNotHaveValue(null);
                LogRecord record = logged.get();
                assertThat(record.getLevel()).isEqualTo(Level.SEVERE);
                assertThat(record.getMessage())
                    .isEqualTo("Scope garbage collected before being closed.");
                assertThat(record.getThrown().getMessage())
                    .matches("Thread \\[Test worker\\] opened a scope of .* here:");
              });
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(true);
    }
  }
}
