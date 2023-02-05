/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.sdk.testing.assertj.LogAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkEventEmitterProviderTest {

  private static final Resource RESOURCE =
      Resource.builder().put("resource-key", "resource-value").build();

  private final AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
  private final SdkEventEmitterProvider eventEmitterProvider =
      SdkEventEmitterProvider.create(
          SdkLoggerProvider.builder()
              .setResource(RESOURCE)
              .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
              .build());

  @Test
  void emit_WithDomain() {
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
        .hasAttributes(
            Attributes.builder()
                .put("key1", "value1")
                .put("event.domain", "event-domain")
                .put("event.name", "event-name")
                .build());
  }

  @Test
  void emit_NoDomain() {
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
        .hasAttributes(
            Attributes.builder()
                .put("key1", "value1")
                .put("event.domain", "unknown")
                .put("event.name", "event-name")
                .build());
  }
}
