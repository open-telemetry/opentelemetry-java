/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventEmitter;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkEventEmitterProviderTest {

  private static final Resource RESOURCE =
      Resource.builder().put("resource-key", "resource-value").build();

  private final Clock clock = mock(Clock.class);
  private final AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
  private final SdkEventEmitterProvider eventEmitterProvider =
      SdkEventEmitterProvider.create(
          SdkLoggerProvider.builder()
              .setResource(RESOURCE)
              .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
              .build(),
          clock);

  @Test
  void emit_NoPayload() {
    when(clock.now()).thenReturn(10L);

    eventEmitterProvider.eventEmitterBuilder("test-scope").build().emit("namespace.event-name");

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(10L)
        .hasObservedTimestamp(10L)
        .hasSeverity(Severity.INFO)
        .hasAttributes(Attributes.builder().put("event.name", "namespace.event-name").build());
    assertThat(seenLog.get().toLogRecordData().getBody()).isEqualTo(Body.empty());
  }

  @Test
  void emit_WithPayload() {
    when(clock.now()).thenReturn(10L);

    AnyValue<?> payload = AnyValue.of(Collections.singletonMap("key1", AnyValue.of("value1")));

    eventEmitterProvider
        .eventEmitterBuilder("test-scope")
        .build()
        .emit("namespace.event-name", payload);

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(10L)
        .hasObservedTimestamp(10L)
        .hasSeverity(Severity.INFO)
        .hasAttributes(Attributes.builder().put("event.name", "namespace.event-name").build());
    assertThat(((AnyValueBody) seenLog.get().toLogRecordData().getBody()).asAnyValue())
        .isEqualTo(payload);
  }

  @Test
  void builder() {
    when(clock.now()).thenReturn(10L);

    long yesterday = System.nanoTime() - TimeUnit.DAYS.toNanos(1);
    EventEmitter emitter = eventEmitterProvider.eventEmitterBuilder("test-scope").build();

    AnyValue<?> payload = AnyValue.of(Collections.singletonMap("key1", AnyValue.of("value1")));
    emitter
        .builder("namespace.event-name")
        .setPayload(payload)
        .setTimestamp(yesterday, TimeUnit.NANOSECONDS)
        .setSeverity(Severity.DEBUG)
        .setAttributes(Attributes.builder().put("extra-attribute", "value").build())
        .emit();

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(yesterday)
        .hasObservedTimestamp(10L)
        .hasSeverity(Severity.DEBUG)
        .hasAttributes(
            Attributes.builder()
                .put("event.name", "namespace.event-name")
                .put("extra-attribute", "value")
                .build());
    assertThat(((AnyValueBody) seenLog.get().toLogRecordData().getBody()).asAnyValue())
        .isEqualTo(payload);
  }
}
