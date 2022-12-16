/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class DefaultEventEmitterProviderTest {

  @Test
  void noopEventEmitterProvider_doesNotThrow() {
    EventEmitterProvider provider = EventEmitterProvider.noop();

    assertThat(provider).isSameAs(DefaultEventEmitterProvider.getInstance());
    assertThatCode(() -> provider.get("scope-name", "event-domain")).doesNotThrowAnyException();
    assertThatCode(
            () ->
                provider
                    .eventEmitterBuilder("scope-name", "event-domain")
                    .setInstrumentationVersion("1.0")
                    .setSchemaUrl("http://schema.com")
                    .build())
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                provider
                    .eventEmitterBuilder("scope-name", "event-domain")
                    .build()
                    .emit("event-name", Attributes.empty()))
        .doesNotThrowAnyException();
  }
}
