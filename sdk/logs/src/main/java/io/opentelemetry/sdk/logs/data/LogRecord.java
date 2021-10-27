/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * Implementation of a log as defined in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md">OpenTelemetry
 * Log Data Model</a>.
 */
public interface LogRecord {

  /** Returns a new {@link LogRecordBuilder}. */
  static LogRecordBuilder builder() {
    return new SdkLogRecordBuilder();
  }

  /** Returns the epoch timestamp in nanos when the log was recorded. */
  long getEpochNanos();

  /** Returns the trace id for this log. */
  @Nullable
  String getTraceId();

  /** Returns the span id for this log. */
  @Nullable
  String getSpanId();

  /** Returns the flags for this log. */
  int getFlags();

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
