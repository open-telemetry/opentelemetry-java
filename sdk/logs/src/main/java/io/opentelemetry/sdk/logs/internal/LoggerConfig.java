/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Logger}.
 *
 * @see SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)
 * @see SdkLoggerProviderUtil#addLoggerConfiguratorCondition(SdkLoggerProviderBuilder, Predicate,
 *     LoggerConfig)
 */
@AutoValue
@Immutable
public abstract class LoggerConfig {

  private static final LoggerConfig DEFAULT_CONFIG =
      new AutoValue_LoggerConfig(/* enabled= */ true);
  private static final LoggerConfig DISABLED_CONFIG =
      new AutoValue_LoggerConfig(/* enabled= */ false);

  /** Returns a disabled {@link LoggerConfig}. */
  public static LoggerConfig disabled() {
    return DISABLED_CONFIG;
  }

  /** Returns an enabled {@link LoggerConfig}. */
  public static LoggerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  /**
   * Returns the default {@link LoggerConfig}, which is used when no configurator is set or when the
   * logger configurator returns {@code null} for a {@link InstrumentationScopeInfo}.
   */
  public static LoggerConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  /**
   * Create a {@link ScopeConfiguratorBuilder} for configuring {@link
   * SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)}.
   */
  public static ScopeConfiguratorBuilder<LoggerConfig> configuratorBuilder() {
    return ScopeConfigurator.builder();
  }

  LoggerConfig() {}

  /** Returns {@code true} if this logger is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
