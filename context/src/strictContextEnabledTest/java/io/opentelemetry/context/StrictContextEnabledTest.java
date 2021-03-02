/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.netmikey.logunit.api.LogCapturer;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

@SuppressWarnings("MustBeClosedChecker")
class StrictContextEnabledTest {

  private static final ContextKey<String> ANIMAL = ContextKey.named("animal");

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(StrictContextStorage.class);

  @Test
  void garbageCollectedScope() {
    Context.current().with(ANIMAL, "cat").makeCurrent();

    await()
        .atMost(Duration.ofSeconds(30))
        .untilAsserted(
            () -> {
              System.gc();
              LoggingEvent log =
                  logs.assertContains("Scope garbage collected before being closed.");
              assertThat(log.getLevel()).isEqualTo(Level.ERROR);
              assertThat(log.getThrowable().getMessage())
                  .matches("Thread \\[Test worker\\] opened a scope of .* here:");
            });
  }
}
