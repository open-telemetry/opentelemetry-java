/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultEventLoggerTest {

  @Test
  void builder() {
    EventLogger eventLogger = DefaultEventLogger.getInstance();
    assertThatCode(
            () ->
                eventLogger
                    .builder("namespace.myEvent")
                    .put("key", "value")
                    .setTimestamp(123456L, TimeUnit.NANOSECONDS)
                    .setTimestamp(Instant.now())
                    .setContext(Context.current())
                    .setSeverity(Severity.DEBUG)
                    .setAttributes(Attributes.empty())
                    .emit())
        .doesNotThrowAnyException();
  }
}
