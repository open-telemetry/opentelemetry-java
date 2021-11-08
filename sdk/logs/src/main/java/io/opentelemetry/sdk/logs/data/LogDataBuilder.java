/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder for {@link LogData}. */
public final class LogDataBuilder {

  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  private long epochNanos;
  private SpanContext spanContext = SpanContext.getInvalid();
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  @Nullable private String name;
  private Body body = Body.emptyBody();
  private Attributes attributes = Attributes.empty();

  LogDataBuilder(Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.resource = resource;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  /** Set the epoch timestamp using the timestamp and unit. */
  public LogDataBuilder setEpoch(long timestamp, TimeUnit unit) {
    this.epochNanos = unit.toNanos(timestamp);
    return this;
  }

  /** Set the epoch timestamp using the instant. */
  public LogDataBuilder setEpoch(Instant instant) {
    this.epochNanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  /** Set the context. */
  public LogDataBuilder setContext(Context context) {
    this.spanContext = Span.fromContext(context).getSpanContext();
    return this;
  }

  /** Set the severity. */
  public LogDataBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  /** Set the severity text. */
  public LogDataBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  /** Set the name. */
  public LogDataBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /** Set the body string. */
  public LogDataBuilder setBody(String body) {
    this.body = Body.stringBody(body);
    return this;
  }

  /** Set the attributes. */
  public LogDataBuilder setAttributes(Attributes attributes) {
    this.attributes = attributes;
    return this;
  }

  /** Build a {@link LogData} instance from the configured properties. */
  public LogData build() {
    if (epochNanos == 0) {
      epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    }
    return LogDataImpl.create(
        resource,
        instrumentationLibraryInfo,
        epochNanos,
        spanContext,
        severity,
        severityText,
        name,
        body,
        attributes);
  }
}
