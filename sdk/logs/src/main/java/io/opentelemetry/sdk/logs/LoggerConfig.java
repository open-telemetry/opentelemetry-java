/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class LoggerConfig {

  private static final LoggerConfig DEFAULT_CONFIG =
      new AutoValue_LoggerConfig(/* enabled= */ true);

  public static LoggerConfig disabled() {
    return new AutoValue_LoggerConfig(/* enabled= */ false);
  }

  public static LoggerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  public static LoggerConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  LoggerConfig() {}

  /** Returns {@code true} if this logger is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
