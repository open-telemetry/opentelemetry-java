/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class DefaultLoggerProviderTest {

  @Test
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
  }
}
