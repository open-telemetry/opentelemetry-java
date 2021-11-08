/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Log definition as described in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md">OpenTelemetry
 * Log Data Model</a>.
 */
@Immutable
public interface LogData {

  /** Returns a new {@link LogDataBuilder}. */
  static LogDataBuilder builder(
      Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new LogDataBuilder(resource, instrumentationLibraryInfo);
  }

  /** Returns the resource of this log. */
  Resource getResource();

  /** Returns the instrumentation library that generated this log. */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /** Returns the epoch timestamp in nanos when the log was recorded. */
  long getEpochNanos();

  /** Return the span context for this log, or {@link SpanContext#getInvalid()} if unset. */
  SpanContext getSpanContext();

  /** Returns the severity for this log, or {@link Severity#UNDEFINED_SEVERITY_NUMBER} if unset. */
  Severity getSeverity();

  /** Returns the severity text for this log, or null if unset. */
  @Nullable
  String getSeverityText();

  /** Returns the name for this log, or null if unset. */
  @Nullable
  String getName();

  /** Returns the body for this log, or {@link Body#emptyBody()} if unset. */
  Body getBody();

  /** Returns the attributes for this log, or {@link Attributes#empty()} if unset. */
  Attributes getAttributes();
}
