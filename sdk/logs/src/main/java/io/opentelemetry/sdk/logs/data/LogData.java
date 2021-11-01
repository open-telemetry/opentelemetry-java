/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.TraceFlags;
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

  /** Returns the trace id for this log. */
  @Nullable
  String getTraceId();

  /** Returns the span id for this log. */
  @Nullable
  String getSpanId();

  /** Returns the trace flags for this log. */
  TraceFlags getTraceFlags();

  /** Returns the severity for this log. */
  Severity getSeverity();

  /** Returns the severity text for this log. */
  @Nullable
  String getSeverityText();

  /** Returns the name for this log. */
  @Nullable
  String getName();

  /** Returns the body for this log. */
  Body getBody();

  /** Returns the attributes for this log. */
  Attributes getAttributes();
}
