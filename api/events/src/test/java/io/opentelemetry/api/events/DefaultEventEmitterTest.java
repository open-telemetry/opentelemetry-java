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

  @Test
  void emitWithTimestamp() {
    EventEmitter emitter = DefaultEventEmitter.getInstance();
    Attributes attributes = Attributes.builder().put("key1", "value1").build();
    assertThatCode(() -> emitter.emit(System.nanoTime(), "event-name", attributes))
        .doesNotThrowAnyException();
  }

  @Test
  void builder() {
    Attributes attributes = Attributes.builder().put("key1", "value1").build();
    EventEmitter emitter = DefaultEventEmitter.getInstance();
    assertThatCode(
            () ->
                emitter
                    .builder()
                    .setEventName("myEvent")
                    .setAttributes(attributes)
                    .setTimestamp(123456L)
                    .emit())
        .doesNotThrowAnyException();
  }

  @Test
  void builderWithName() {
    Attributes attributes = Attributes.builder().put("key1", "value1").build();
    EventEmitter emitter = DefaultEventEmitter.getInstance();
    assertThatCode(
            () -> emitter.builder("myEvent").setAttributes(attributes).setTimestamp(123456L).emit())
        .doesNotThrowAnyException();
  }

  @Test
  void builderWithNameAndAttrs() {
    Attributes attributes = Attributes.builder().put("key1", "value1").build();
    EventEmitter emitter = DefaultEventEmitter.getInstance();
    assertThatCode(() -> emitter.builder("myEvent", attributes).setTimestamp(123456L).emit())
        .doesNotThrowAnyException();
  }
}
