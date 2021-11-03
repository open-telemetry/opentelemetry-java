/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
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
  @Nullable private String traceId;
  @Nullable private String spanId;
  private int flags;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  @Nullable private String name;
  private Body body = Body.stringBody("");
  private Clock clock = Clock.getDefault();
  private final AttributesBuilder attributeBuilder = Attributes.builder();

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

  /** Set the trace id. */
  public LogDataBuilder setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  /** Set the span id. */
  public LogDataBuilder setSpanId(String spanId) {
    this.spanId = spanId;
    return this;
  }

  /** Set the flags. */
  public LogDataBuilder setFlags(int flags) {
    this.flags = flags;
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

  /** Set the body. */
  public LogDataBuilder setBody(Body body) {
    this.body = body;
    return this;
  }

  /** Set the body string. */
  public LogDataBuilder setBody(String body) {
    return setBody(Body.stringBody(body));
  }

  /** Set the attributes. */
  public LogDataBuilder setAttributes(Attributes attributes) {
    this.attributeBuilder.putAll(attributes);
    return this;
  }

  /** Sets the clock to be used for the current epoch nanos (if it is not set) */
  public LogDataBuilder setClock(Clock clock) {
    this.clock = clock;
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
