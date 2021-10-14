/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * The interface for a log as defined in the <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/logs/data-model.md">OpenTelemetry
 * logging model</a>.
 */
@Immutable
public interface LogData {

  /**
   * Returns the resource of this log.
   *
   * @return the resource.
   */
  Resource getResource();

  /**
   * Returns the instrumentation library that generated this log.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}.
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the epoch timestamp in nanos when the log was recorded.
   *
   * @return the epoch timestamp in nanos.
   */
  long getEpochNanos();

  /**
   * Returns the trace id for this log.
   *
   * @return the trace id.
   */
  @Nullable
  String getTraceId();

  /**
   * Returns the span id for this log.
   *
   * @return the span id.
   */
  @Nullable
  String getSpanId();

  /**
   * Returns the flags for this log.
   *
   * @return the flags.
   */
  int getFlags();

  /**
   * Returns the severity for this log.
   *
   * @return the severity.
   */
  Severity getSeverity();

  /**
   * Returns the severity text for this log.
   *
   * @return the severity text.
   */
  @Nullable
  String getSeverityText();

  /**
   * Returns the name for this log.
   *
   * @return the name.
   */
  @Nullable
  String getName();

  /**
   * Returns the body for this log.
   *
   * @return the body.
   */
  Body getBody();

  /**
   * Returns the attributes for this log.
   *
   * @return the attributes.
   */
  Attributes getAttributes();
}
