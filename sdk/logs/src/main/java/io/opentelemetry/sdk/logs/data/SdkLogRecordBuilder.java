/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder for {@link SdkLogRecordBuilder}. */
final class SdkLogRecordBuilder implements LogRecordBuilder {

  private long epochNanos;
  @Nullable private String traceId;
  @Nullable private String spanId;
  private int flags;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  @Nullable private String name;
  private Body body = Body.stringBody("");
  private final AttributesBuilder attributeBuilder = Attributes.builder();

  SdkLogRecordBuilder() {}

  @Override
  public SdkLogRecordBuilder setEpochNanos(long timestamp) {
    this.epochNanos = timestamp;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setEpochMillis(long timestamp) {
    return setEpochNanos(TimeUnit.MILLISECONDS.toNanos(timestamp));
  }

  @Override
  public SdkLogRecordBuilder setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSpanId(String spanId) {
    this.spanId = spanId;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setFlags(int flags) {
    this.flags = flags;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setBody(Body body) {
    this.body = body;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setBody(String body) {
    return setBody(Body.stringBody(body));
  }

  @Override
  public SdkLogRecordBuilder setAttributes(Attributes attributes) {
    this.attributeBuilder.putAll(attributes);
    return this;
  }

  @Override
  public SdkLogRecord build() {
    if (epochNanos == 0) {
      epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }
    return SdkLogRecord.create(
        epochNanos,
        traceId,
        spanId,
        flags,
        severity,
        severityText,
        name,
        body,
        attributeBuilder.build());
  }
}
