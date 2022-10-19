/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static io.opentelemetry.api.internal.ValidationUtil.API_USAGE_LOGGER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.LoggingEvent;

class DefaultLoggerTest {

  @RegisterExtension
  LogCapturer apiUsageLogs = LogCapturer.create().captureForLogger(API_USAGE_LOGGER_NAME);

  @Test
  @SuppressLogger(loggerName = API_USAGE_LOGGER_NAME)
  void buildAndEmit() {
    // Logger with no event.domain
    assertThatCode(
            () ->
                DefaultLogger.getInstance(true)
                    .logRecordBuilder()
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                DefaultLogger.getInstance(true)
                    .eventBuilder("event-name")
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThat(apiUsageLogs.getEvents()).isEmpty();

    // Logger with event.domain
    assertThatCode(
            () ->
                DefaultLogger.getInstance(false)
                    .logRecordBuilder()
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                DefaultLogger.getInstance(false)
                    .eventBuilder("event-name")
                    .setEpoch(100, TimeUnit.SECONDS)
                    .setEpoch(Instant.now())
                    .setContext(Context.root())
                    .setSeverity(Severity.DEBUG)
                    .setSeverityText("debug")
                    .setBody("body")
                    .setAttribute(AttributeKey.stringKey("key1"), "value1")
                    .setAllAttributes(Attributes.builder().put("key2", "value2").build())
                    .emit())
        .doesNotThrowAnyException();
    assertThat(apiUsageLogs.getEvents())
        .hasSize(1)
        .extracting(LoggingEvent::getMessage)
        .allMatch(
            log ->
                log.equals(
                    "Cannot emit event from Logger without event domain. Please use LoggerBuilder#setEventDomain(String) when obtaining Logger."));
  }
}
