/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder for {@link LogRecord}. */
public final class LogRecordBuilder {
  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  private long epochNanos;
  @Nullable private String traceId;
  @Nullable private String spanId;
  private int flags;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  @Nullable private String name;
  private Body body = Body.stringBody("");
  private final AttributesBuilder attributeBuilder = Attributes.builder();

  LogRecordBuilder(Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.resource = resource;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  public LogRecordBuilder setEpochNanos(long timestamp) {
    this.epochNanos = timestamp;
    return this;
  }

  public LogRecordBuilder setEpochMillis(long timestamp) {
    return setEpochNanos(TimeUnit.MILLISECONDS.toNanos(timestamp));
  }

  public LogRecordBuilder setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public LogRecordBuilder setSpanId(String spanId) {
    this.spanId = spanId;
    return this;
  }

  public LogRecordBuilder setFlags(int flags) {
    this.flags = flags;
    return this;
  }

  public LogRecordBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  public LogRecordBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  public LogRecordBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public LogRecordBuilder setBody(Body body) {
    this.body = body;
    return this;
  }

  public LogRecordBuilder setBody(String body) {
    return setBody(Body.stringBody(body));
  }

  public LogRecordBuilder setAttributes(Attributes attributes) {
    this.attributeBuilder.putAll(attributes);
    return this;
  }

  /**
   * Build a LogRecord instance.
   *
   * @return value object being built
   */
  public LogRecord build() {
    if (epochNanos == 0) {
      epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }
    return LogRecord.create(
        resource,
        instrumentationLibraryInfo,
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
