/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeLimits;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ReadWriteLogRecordTest {

  @Test
  void addAllAttributes() {
    Attributes newAttributes = Attributes.of(stringKey("foo"), "bar", stringKey("bar"), "buzz");
    SdkReadWriteLogRecord logRecord = buildLogRecord();

    logRecord.setAllAttributes(newAttributes);

    Attributes result = logRecord.getAttributes();
    assertThat(result.get(stringKey("foo"))).isEqualTo("bar");
    assertThat(result.get(stringKey("bar"))).isEqualTo("buzz");
    assertThat(result.get(stringKey("untouched"))).isEqualTo("yes");
  }

  @Test
  void addAllHandlesNull() {
    SdkReadWriteLogRecord logRecord = buildLogRecord();
    Attributes originalAttributes = logRecord.getAttributes();
    ReadWriteLogRecord result = logRecord.setAllAttributes(null);
    assertThat(result.getAttributes()).isEqualTo(originalAttributes);
  }

  @Test
  void allHandlesEmpty() {
    SdkReadWriteLogRecord logRecord = buildLogRecord();
    Attributes originalAttributes = logRecord.getAttributes();
    ReadWriteLogRecord result = logRecord.setAllAttributes(Attributes.empty());
    assertThat(result.getAttributes()).isEqualTo(originalAttributes);
  }

  @Test
  void defaultGetObservedTimestampEpochNanos_returnsObservedTimestamp() {
    LogRecordData data =
        TestLogRecordData.builder()
            .setTimestamp(100, TimeUnit.NANOSECONDS)
            .setObservedTimestamp(200, TimeUnit.NANOSECONDS)
            .build();
    // A ReadWriteLogRecord that relies on the interface default methods, implementing only the
    // abstract methods. The SDK's SdkReadWriteLogRecord overrides the getters, so the default
    // method behavior must be exercised through a custom implementation.
    ReadWriteLogRecord logRecord =
        new ReadWriteLogRecord() {
          @Override
          public <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
            return this;
          }

          @Override
          public LogRecordData toLogRecordData() {
            return data;
          }
        };

    assertThat(logRecord.getObservedTimestampEpochNanos()).isEqualTo(200);
    assertThat(logRecord.getTimestampEpochNanos()).isEqualTo(100);
  }

  SdkReadWriteLogRecord buildLogRecord() {
    Value<?> body = Value.of("bod");
    AttributesBuilder initialAttributes =
        Attributes.builder(
            AttributeLimits.builder().setCountLimit(100).setValueLengthLimit(200).build());
    initialAttributes.put(stringKey("foo"), "aaiosjfjioasdiojfjioasojifja");
    initialAttributes.put(stringKey("untouched"), "yes");
    LogLimits limits = LogLimits.getDefault();
    Resource resource = Resource.empty();
    InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("test");
    SpanContext spanContext = SpanContext.getInvalid();

    return SdkReadWriteLogRecord.create(
        limits,
        resource,
        scope,
        0L,
        0L,
        spanContext,
        Severity.DEBUG,
        "buggin",
        body,
        initialAttributes,
        2,
        "my.event.name");
  }
}
