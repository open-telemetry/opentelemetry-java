/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class DefaultEventEmitterTest {

  @Test
  void emit() {
    assertThatCode(() -> DefaultEventEmitter.getInstance().emit("event-name", Attributes.empty()))
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                DefaultEventEmitter.getInstance()
                    .emit("event-name", Attributes.builder().put("key1", "value1").build()))
        .doesNotThrowAnyException();
  }
}
