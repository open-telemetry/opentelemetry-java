/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.logs.Severity;

/**
 * Builder for {@link LoggerConfig}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class LoggerConfigBuilder {
  private boolean enabled = true;
  private Severity minimumSeverity = Severity.UNDEFINED_SEVERITY_NUMBER;
  private boolean traceBased = false;

  LoggerConfigBuilder() {}

  /**
   * Sets whether the logger is enabled.
   *
   * @param enabled whether the logger is enabled
   * @return this builder
   */
  public LoggerConfigBuilder setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Sets the minimum severity level for log records to be processed.
   *
   * <p>Log records with a severity number less than this value will be dropped. Log records without
   * a specified severity are not affected by this setting.
   *
   * @param minimumSeverity minimum severity level for log records to be processed
   * @return this builder
   */
  public LoggerConfigBuilder setMinimumSeverity(Severity minimumSeverity) {
    this.minimumSeverity = minimumSeverity;
    return this;
  }

  /**
   * Sets whether to only process log records from traces when the trace is sampled.
   *
   * <p>When enabled, log records from unsampled traces will be dropped. Log records that are not
   * associated with a trace context are unaffected.
   *
   * @param traceBased whether to only process log records from traces when the trace is sampled
   * @return this builder
   */
  public LoggerConfigBuilder setTraceBased(boolean traceBased) {
    this.traceBased = traceBased;
    return this;
  }

  /** Builds and returns a {@link LoggerConfig}. */
  public LoggerConfig build() {
    return LoggerConfig.create(enabled, minimumSeverity, traceBased);
  }
}
