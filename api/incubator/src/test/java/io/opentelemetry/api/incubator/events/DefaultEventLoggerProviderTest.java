/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class DefaultEventLoggerProviderTest {

  @Test
  void noopEventLoggerProvider_doesNotThrow() {
    EventLoggerProvider provider = EventLoggerProvider.noop();

    assertThat(provider).isSameAs(DefaultEventLoggerProvider.getInstance());
    assertThatCode(() -> provider.get("scope-name")).doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .eventLoggerBuilder("scope-name")
                    .setInstrumentationVersion("1.0")
                    .setSchemaUrl("http://schema.com")
                    .build())
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                provider
                    .eventLoggerBuilder("scope-name")
                    .build()
                    .builder("namespace.event-name")
                    .emit())
        .doesNotThrowAnyException();
  }
}
