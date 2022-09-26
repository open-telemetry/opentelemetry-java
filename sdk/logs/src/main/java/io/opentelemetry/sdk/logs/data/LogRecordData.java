/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Log definition as described in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md">OpenTelemetry
 * Log Data Model</a>.
 */
@Immutable
public interface LogRecordData {

  /** Returns the resource of this log. */
  Resource getResource();

  /** Returns the instrumentation scope that generated this log. */
  InstrumentationScopeInfo getInstrumentationScopeInfo();

  /** Returns the epoch timestamp in nanos when the log was recorded. */
  long getEpochNanos();

  /** Return the span context for this log, or {@link SpanContext#getInvalid()} if unset. */
  SpanContext getSpanContext();

  /** Returns the severity for this log, or {@link Severity#UNDEFINED_SEVERITY_NUMBER} if unset. */
  Severity getSeverity();

  /** Returns the severity text for this log, or null if unset. */
  @Nullable
  String getSeverityText();

  /** Returns the body for this log, or {@link Body#empty()} if unset. */
  Body getBody();

  /** Returns the attributes for this log, or {@link Attributes#empty()} if unset. */
  Attributes getAttributes();

  /**
   * Returns the total number of attributes that were recorded on this log.
   *
   * <p>This number may be larger than the number of attributes that are attached to this log, if
   * the total number recorded was greater than the configured maximum value. See {@link
   * LogLimits#getMaxNumberOfAttributes()}.
   */
  int getTotalAttributeCount();
}
