/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultEventLoggerTest {

  @Test
  void emit() {
    assertThatCode(() -> DefaultEventLogger.getInstance().emit("event-name", Attributes.empty()))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                DefaultEventLogger.getInstance()
                    .emit(
                        "event-domain.event-name",
                        Attributes.builder().put("key1", "value1").build()))
        .doesNotThrowAnyException();
  }

  @Test
  void builder() {
    Attributes attributes = Attributes.builder().put("key1", "value1").build();
    EventLogger eventLogger = DefaultEventLogger.getInstance();
    assertThatCode(
            () ->
                eventLogger
                    .builder("com.example.MyEvent", attributes)
                    .setTimestamp(123456L, TimeUnit.NANOSECONDS)
                    .setTimestamp(Instant.now())
                    .emit())
        .doesNotThrowAnyException();
  }
}
