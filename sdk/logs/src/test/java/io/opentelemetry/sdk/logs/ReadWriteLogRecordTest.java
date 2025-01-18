/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ReadWriteLogRecordTest {

  private static final Severity severity = Severity.DEBUG;
  private static final String severityText = "buggin";
  private static final InstrumentationScopeInfo scope = InstrumentationScopeInfo.create("test");
  private static final SpanContext spanContext = SpanContext.getInvalid();
  private static final Value<?> body = Value.of("bod");
  private static final long observedTimestampEpochNanos = 0L;
  private static final long timestampEpochNanos = 0L;

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
  void checkProperties() {
    SdkReadWriteLogRecord logRecord = buildLogRecord();
    assertThat(logRecord.getSeverity()).isEqualTo(severity);
    assertThat(logRecord.getSeverityText()).isEqualTo(severityText);
    assertThat(logRecord.getInstrumentationScopeInfo()).isEqualTo(scope);
    assertThat(logRecord.getSpanContext()).isEqualTo(spanContext);
    assertThat(logRecord.getBodyValue()).isEqualTo(body);
    assertThat(logRecord.getObservedTimestampEpochNanos()).isEqualTo(0L);
    assertThat(logRecord.getTimestampEpochNanos()).isEqualTo(timestampEpochNanos);
    assertThat(logRecord.getObservedTimestampEpochNanos()).isEqualTo(observedTimestampEpochNanos);
    assertThat(logRecord.getAttribute(AttributeKey.stringKey("foo")))
        .isEqualTo("aaiosjfjioasdiojfjioasojifja");
    assertThat(logRecord.getAttribute(AttributeKey.stringKey("untouched"))).isEqualTo("yes");
    assertThat(logRecord.toLogRecordData().getAttributes().get(AttributeKey.stringKey("untouched")))
        .isEqualTo("yes");
  }

  SdkReadWriteLogRecord buildLogRecord() {
    LogLimits limits = LogLimits.getDefault();
    Resource resource = Resource.empty();

    SdkReadWriteLogRecord record =
        SdkReadWriteLogRecord.create(
            limits, resource, scope, 0L, 0L, spanContext, severity, severityText, body, null);
    record.setAttribute(AttributeKey.stringKey("foo"), "aaiosjfjioasdiojfjioasojifja");
    record.setAttribute(AttributeKey.stringKey("untouched"), "yes");
    record.setAttribute(AttributeKey.stringKey(""), "yes");
    return record;
  }
}
