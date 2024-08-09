/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultLoggerTest {

  @Test
  void buildAndEmit() {
    assertThatCode(
            () ->
                DefaultLogger.getInstance()
                    .logRecordBuilder()
                    .setTimestamp(100, TimeUnit.SECONDS)
                    .setTimestamp(Instant.now())
                    .setObservedTimestamp(100, TimeUnit.SECONDS)
                    .setObservedTimestamp(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setBody(Value.of("body"))
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
  }
}
