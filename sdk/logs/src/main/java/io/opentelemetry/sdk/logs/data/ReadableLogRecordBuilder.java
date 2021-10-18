/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder for {@link ReadableLogRecordBuilder}. */
public final class ReadableLogRecordBuilder {

  private long epochNanos;
  @Nullable private String traceId;
  @Nullable private String spanId;
  private int flags;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  @Nullable private String name;
  private Body body = Body.stringBody("");
  private final AttributesBuilder attributeBuilder = Attributes.builder();

  ReadableLogRecordBuilder() {}

  public ReadableLogRecordBuilder setEpochNanos(long timestamp) {
    this.epochNanos = timestamp;
    return this;
  }

  public ReadableLogRecordBuilder setEpochMillis(long timestamp) {
    return setEpochNanos(TimeUnit.MILLISECONDS.toNanos(timestamp));
  }

  public ReadableLogRecordBuilder setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public ReadableLogRecordBuilder setSpanId(String spanId) {
    this.spanId = spanId;
    return this;
  }

  public ReadableLogRecordBuilder setFlags(int flags) {
    this.flags = flags;
    return this;
  }

  public ReadableLogRecordBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  public ReadableLogRecordBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  public ReadableLogRecordBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public ReadableLogRecordBuilder setBody(Body body) {
    this.body = body;
    return this;
  }

  public ReadableLogRecordBuilder setBody(String body) {
    return setBody(Body.stringBody(body));
  }

  public ReadableLogRecordBuilder setAttributes(Attributes attributes) {
    this.attributeBuilder.putAll(attributes);
    return this;
  }

  /**
   * Build a {@link ReadableLogRecord} instance.
   *
   * @return the instance
   */
  public ReadableLogRecord build() {
    if (epochNanos == 0) {
      epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }
    return ReadableLogRecord.create(
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
