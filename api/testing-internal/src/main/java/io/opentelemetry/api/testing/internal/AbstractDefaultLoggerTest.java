/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.testing.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for No-op {@link Logger}. */
public abstract class AbstractDefaultLoggerTest {

  protected abstract LoggerProvider getLoggerProvider();

  protected abstract Logger getLogger();

  @Test
  void noopLoggerProvider_doesNotThrow() {
    LoggerProvider provider = LoggerProvider.noop();

    assertThat(provider).isSameAs(getLoggerProvider());
    assertThatCode(() -> provider.get("scope-name")).doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .loggerBuilder("scope-name")
                    .setInstrumentationVersion("1.0")
                    .setSchemaUrl("http://schema.com")
                    .build())
        .doesNotThrowAnyException();

    assertThatCode(() -> provider.loggerBuilder("scope-name").build().logRecordBuilder())
        .doesNotThrowAnyException();
  }

  @Test
  void buildAndEmit() {
    assertThatCode(
            () ->
                getLogger()
                    .logRecordBuilder()
                    .setEventName("event name")
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
                    .setException(new RuntimeException("error"))
                    .emit())
        .doesNotThrowAnyException();
  }
}
