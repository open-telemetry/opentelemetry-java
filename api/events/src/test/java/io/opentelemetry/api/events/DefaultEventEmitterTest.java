/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class DefaultEventEmitterTest {

  @Test
  void emit() {
    assertThatCode(() -> DefaultEventEmitter.getInstance().emit("namespace.event-name"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                DefaultEventEmitter.getInstance()
                    .emit(
                        "namespace.event-name",
                        AnyValue.of(Collections.singletonMap("key1", AnyValue.of("value1")))))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                DefaultEventEmitter.getInstance()
                    .emit(
                        "namespace.event-name",
                        Collections.singletonMap("key1", AnyValue.of("value1"))))
        .doesNotThrowAnyException();
  }

  @Test
  void builder() {
    EventEmitter emitter = DefaultEventEmitter.getInstance();
    assertThatCode(
            () ->
                emitter
                    .builder("namespace.myEvent")
                    .setPayload(AnyValue.of("payload"))
                    .setPayload(Collections.singletonMap("key1", AnyValue.of("value1")))
                    .setTimestamp(123456L, TimeUnit.NANOSECONDS)
                    .setTimestamp(Instant.now())
                    .setContext(Context.current())
                    .setSeverity(Severity.DEBUG)
                    .setAttributes(Attributes.empty())
                    .emit())
        .doesNotThrowAnyException();
  }
}
