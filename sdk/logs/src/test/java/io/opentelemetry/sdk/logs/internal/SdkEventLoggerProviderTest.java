/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.events.EventLogger;
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
    Value<?> expectedPayload = Value.of(Collections.singletonMap("key1", Value.of("value1")));
    assertThat(seenLog.get().toLogRecordData().getBodyValue()).isEqualTo(expectedPayload);
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
        // Set complex data
        .put(
            "valueKey",
            Value.of(
                ImmutableMap.of(
                    "childKey1", Value.of("value"),
                    "childKey2", Value.of("value"))))
        // Helper methods to set AttributeKey<T> types
        .put(AttributeKey.stringKey("attrStringKey"), "value")
        .put(AttributeKey.longKey("attrLongKey"), 1L)
        .put(AttributeKey.doubleKey("attrDoubleKey"), 1.0)
        .put(AttributeKey.booleanKey("attrBoolKey"), true)
        .put(AttributeKey.stringArrayKey("attrStringArrKey"), Arrays.asList("value1", "value2"))
        .put(AttributeKey.longArrayKey("attrLongArrKey"), Arrays.asList(1L, 2L))
        .put(AttributeKey.doubleArrayKey("attrDoubleArrKey"), Arrays.asList(1.0, 2.0))
        .put(AttributeKey.booleanArrayKey("attrBoolArrKey"), Arrays.asList(true, false))
        .emit();

    Map<String, Value<?>> expectedPayload = new HashMap<>();
    expectedPayload.put("stringKey", Value.of("value"));
    expectedPayload.put("longKey", Value.of(1L));
    expectedPayload.put("doubleKey", Value.of(1.0));
    expectedPayload.put("boolKey", Value.of(true));
    expectedPayload.put(
        "stringArrKey", Value.of(Arrays.asList(Value.of("value1"), Value.of("value2"))));
    expectedPayload.put("longArrKey", Value.of(Arrays.asList(Value.of(1L), Value.of(2L))));
    expectedPayload.put("doubleArrKey", Value.of(Arrays.asList(Value.of(1.0), Value.of(2.0))));
    expectedPayload.put("boolArrKey", Value.of(Arrays.asList(Value.of(true), Value.of(false))));
    expectedPayload.put(
        "valueKey",
        Value.of(
            ImmutableMap.of(
                "childKey1", Value.of("value"),
                "childKey2", Value.of("value"))));
    expectedPayload.put("attrStringKey", Value.of("value"));
    expectedPayload.put("attrLongKey", Value.of(1L));
    expectedPayload.put("attrDoubleKey", Value.of(1.0));
    expectedPayload.put("attrBoolKey", Value.of(true));
    expectedPayload.put(
        "attrStringArrKey", Value.of(Arrays.asList(Value.of("value1"), Value.of("value2"))));
    expectedPayload.put("attrLongArrKey", Value.of(Arrays.asList(Value.of(1L), Value.of(2L))));
    expectedPayload.put("attrDoubleArrKey", Value.of(Arrays.asList(Value.of(1.0), Value.of(2.0))));
    expectedPayload.put("attrBoolArrKey", Value.of(Arrays.asList(Value.of(true), Value.of(false))));
    assertThat(seenLog.get().toLogRecordData().getBodyValue()).isEqualTo(Value.of(expectedPayload));
  }
}
