/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventEmitter;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.resources.Resource;
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
  void emit_WithDomain() {
    when(clock.now()).thenReturn(10L);

    eventEmitterProvider
        .eventEmitterBuilder("test-scope")
        .setEventDomain("event-domain")
        .build()
        .emit(
            "event-name",
            Attributes.builder()
                .put("key1", "value1")
                // should be overridden by the eventName argument passed to emit
                .put("event.name", "foo")
                // should be overridden by the eventDomain
                .put("event.domain", "foo")
                .build());

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(10L)
        .hasAttributes(
            Attributes.builder()
                .put("key1", "value1")
                .put("event.domain", "event-domain")
                .put("event.name", "event-name")
                .build());
  }

  @Test
  void emit_NoDomain() {
    when(clock.now()).thenReturn(10L);

    eventEmitterProvider
        .eventEmitterBuilder("test-scope")
        .build()
        .emit(
            "event-name",
            Attributes.builder()
                .put("key1", "value1")
                // should be overridden by the eventName argument passed to emit
                .put("event.name", "foo")
                // should be overridden by the default eventDomain
                .put("event.domain", "foo")
                .build());

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(10L)
        .hasAttributes(
            Attributes.builder()
                .put("key1", "value1")
                .put("event.domain", "unknown")
                .put("event.name", "event-name")
                .build());
  }

  @Test
  void emit_withTimestamp() {
    long yesterday = System.nanoTime() - TimeUnit.DAYS.toNanos(1);
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");

    EventEmitter emitter = eventEmitterProvider.eventEmitterBuilder("test-scope").build();

    emitter.emit(yesterday, "testing", attributes);

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(yesterday)
        .hasAttributes(
            attributes.toBuilder()
                .put("event.domain", "unknown")
                .put("event.name", "testing")
                .build());
  }

  @Test
  void builder() {
    long yesterday = System.nanoTime() - TimeUnit.DAYS.toNanos(1);
    Attributes attributes = Attributes.of(stringKey("foo"), "bar");

    EventEmitter emitter = eventEmitterProvider.eventEmitterBuilder("test-scope").build();

    emitter.builder("testing", attributes).setTimestamp(yesterday).emit();
    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(yesterday)
        .hasAttributes(
            attributes.toBuilder()
                .put("event.domain", "unknown")
                .put("event.name", "testing")
                .build());
  }
}
