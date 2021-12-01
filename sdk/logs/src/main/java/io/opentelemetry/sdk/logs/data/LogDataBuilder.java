/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
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
  private Body body = Body.empty();
  private final Clock clock;
  private Attributes attributes = Attributes.empty();

  private LogDataBuilder(
      Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo, Clock clock) {
    this.resource = resource;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.clock = clock;
  }

  /** Returns a new {@link LogDataBuilder} with the default clock. */
  public static LogDataBuilder create(
      Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return create(resource, instrumentationLibraryInfo, Clock.getDefault());
  }

  /** Returns a new {@link LogDataBuilder}. */
  public static LogDataBuilder create(
      Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo, Clock clock) {
    return new LogDataBuilder(resource, instrumentationLibraryInfo, clock);
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

  /** Sets the context. */
  public LogDataBuilder setContext(Context context) {
    return setSpanContext(Span.fromContext(context).getSpanContext());
  }

  /** Set the span context. */
  public LogDataBuilder setSpanContext(SpanContext spanContext) {
    this.spanContext = spanContext == null ? SpanContext.getInvalid() : spanContext;
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
    this.body = Body.string(body);
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
      epochNanos = clock.now();
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
