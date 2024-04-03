/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.incubator.logs.AnyValue;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SdkEventLoggerProviderTest {

  private static final Resource RESOURCE =
      Resource.builder().put("resource-key", "resource-value").build();

  private final Clock clock = mock(Clock.class);
  private final AtomicReference<ReadWriteLogRecord> seenLog = new AtomicReference<>();
  private final SdkEventLoggerProvider eventEmitterProvider =
      SdkEventLoggerProvider.create(
          SdkLoggerProvider.builder()
              .setResource(RESOURCE)
              .addLogRecordProcessor((context, logRecord) -> seenLog.set(logRecord))
              .build(),
          clock);

  @Test
  void builder() {
    when(clock.now()).thenReturn(10L);

    long yesterday = System.nanoTime() - TimeUnit.DAYS.toNanos(1);
    EventLogger eventLogger = eventEmitterProvider.eventLoggerBuilder("test-scope").build();

    eventLogger
        .builder("namespace.event-name")
        .put("key1", "value1")
        .setTimestamp(yesterday, TimeUnit.NANOSECONDS)
        .setSeverity(Severity.DEBUG)
        .setAttributes(Attributes.builder().put("extra-attribute", "value").build())
        .emit();

    assertThat(seenLog.get().toLogRecordData())
        .hasResource(RESOURCE)
        .hasInstrumentationScope(InstrumentationScopeInfo.create("test-scope"))
        .hasTimestamp(yesterday)
        .hasSeverity(Severity.DEBUG)
        .hasAttributes(
            Attributes.builder()
                .put("event.name", "namespace.event-name")
                .put("extra-attribute", "value")
                .build());
    assertThat(seenLog.get().toLogRecordData().getObservedTimestampEpochNanos()).isPositive();
    AnyValue<?> expectedPayload =
        AnyValue.of(Collections.singletonMap("key1", AnyValue.of("value1")));
    assertThat(((AnyValueBody) seenLog.get().toLogRecordData().getBody()).asAnyValue())
        .isEqualTo(expectedPayload);
  }

  @Test
  void eventBuilder_FullPayload() {
    EventLogger eventLogger = eventEmitterProvider.get("test-scoe");

    eventLogger
        .builder("namespace.my-event-name")
        // Helper methods to set primitive types
        .put("stringKey", "value")
        .put("longKey", 1L)
        .put("doubleKey", 1.0)
        .put("boolKey", true)
        // Helper methods to set primitive array types
        .put("stringArrKey", "value1", "value2")
        .put("longArrKey", 1L, 2L)
        .put("doubleArrKey", 1.0, 2.0)
        .put("boolArrKey", true, false)
        // Set AnyValue types to encode complex data
        .put(
            "anyValueKey",
            AnyValue.of(
                ImmutableMap.of(
                    "childKey1", AnyValue.of("value"),
                    "childKey2", AnyValue.of("value"))))
        .emit();

    Map<String, AnyValue<?>> expectedPayload = new HashMap<>();
    expectedPayload.put("stringKey", AnyValue.of("value"));
    expectedPayload.put("longKey", AnyValue.of(1L));
    expectedPayload.put("doubleKey", AnyValue.of(1.0));
    expectedPayload.put("boolKey", AnyValue.of(true));
    expectedPayload.put(
        "stringArrKey", AnyValue.of(Arrays.asList(AnyValue.of("value1"), AnyValue.of("value2"))));
    expectedPayload.put("longArrKey", AnyValue.of(Arrays.asList(AnyValue.of(1L), AnyValue.of(2L))));
    expectedPayload.put(
        "doubleArrKey", AnyValue.of(Arrays.asList(AnyValue.of(1.0), AnyValue.of(2.0))));
    expectedPayload.put(
        "boolArrKey", AnyValue.of(Arrays.asList(AnyValue.of(true), AnyValue.of(false))));
    expectedPayload.put(
        "anyValueKey",
        AnyValue.of(
            ImmutableMap.of(
                "childKey1", AnyValue.of("value"),
                "childKey2", AnyValue.of("value"))));
    assertThat(((AnyValueBody) seenLog.get().toLogRecordData().getBody()).asAnyValue())
        .isEqualTo(AnyValue.of(expectedPayload));
  }
}
