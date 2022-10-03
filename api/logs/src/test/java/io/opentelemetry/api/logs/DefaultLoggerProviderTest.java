/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static io.opentelemetry.api.internal.ValidationUtil.API_USAGE_LOGGER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import org.junit.jupiter.api.Test;

class DefaultLoggerProviderTest {

  @Test
  @SuppressLogger(loggerName = API_USAGE_LOGGER_NAME)
  void noopLoggerProvider_doesNotThrow() {
    LoggerProvider provider = LoggerProvider.noop();

    assertThat(provider).isSameAs(DefaultLoggerProvider.getInstance());
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
    assertThatCode(() -> provider.loggerBuilder("scope-name").build().eventBuilder("event-name"))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .loggerBuilder("scope-name")
                    .setEventDomain("event-domain")
                    .build()
                    .logRecordBuilder())
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .loggerBuilder("scope-name")
                    .setEventDomain("event-domain")
                    .build()
                    .eventBuilder("event-name"))
        .doesNotThrowAnyException();
  }
}
