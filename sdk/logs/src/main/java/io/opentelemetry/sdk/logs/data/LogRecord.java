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

  /**
   * Returns the epoch timestamp in nanos when the log was recorded.
   *
   * @return the epoch timestamp in nanos
   */
  long getEpochNanos();

  /**
   * Returns the trace id for this log.
   *
   * @return the trace id
   */
  @Nullable
  String getTraceId();

  /**
   * Returns the span id for this log.
   *
   * @return the span id
   */
  @Nullable
  String getSpanId();

  /**
   * Returns the flags for this log.
   *
   * @return the flags
   */
  int getFlags();

  /**
   * Returns the severity for this log.
   *
   * @return the severity
   */
  Severity getSeverity();

  /**
   * Returns the severity text for this log.
   *
   * @return the severity text
   */
  @Nullable
  String getSeverityText();

  /**
   * Returns the name for this log.
   *
   * @return the name
   */
  @Nullable
  String getName();

  /**
   * Returns the body for this log.
   *
   * @return the body
   */
  Body getBody();

  /**
   * Returns the attributes for this log.
   *
   * @return the attributes
   */
  Attributes getAttributes();
}
