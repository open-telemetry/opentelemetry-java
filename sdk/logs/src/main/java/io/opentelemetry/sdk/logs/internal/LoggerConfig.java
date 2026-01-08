/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Logger}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 *
 * @see SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)
 * @see SdkLoggerProviderUtil#addLoggerConfiguratorCondition(SdkLoggerProviderBuilder, Predicate,
 *     LoggerConfig)
 */
@AutoValue
@Immutable
public abstract class LoggerConfig {

  private static final LoggerConfig DEFAULT_CONFIG =
      new AutoValue_LoggerConfig(
          /* enabled= */ true,
          /* minimumSeverity= */ Severity.UNDEFINED_SEVERITY_NUMBER,
          /* traceBased= */ false);
  private static final LoggerConfig DISABLED_CONFIG =
      new AutoValue_LoggerConfig(
          /* enabled= */ false,
          /* minimumSeverity= */ Severity.UNDEFINED_SEVERITY_NUMBER,
          /* traceBased= */ false);

  /** Returns a disabled {@link LoggerConfig}. */
  public static LoggerConfig disabled() {
    return DISABLED_CONFIG;
  }

  /** Returns an enabled {@link LoggerConfig}. */
  public static LoggerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  /** Returns a new {@link LoggerConfigBuilder} for creating a {@link LoggerConfig}. */
  public static LoggerConfigBuilder builder() {
    return new LoggerConfigBuilder();
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

  static LoggerConfig create(boolean enabled, Severity minimumSeverity, boolean traceBased) {
    return new AutoValue_LoggerConfig(enabled, minimumSeverity, traceBased);
  }

  /** Returns {@code true} if this logger is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();

  /**
   * Returns the minimum severity level for log records to be processed.
   *
   * <p>Log records with a severity number less than this value will be dropped. Log records without
   * a specified severity are not affected by this setting.
   *
   * <p>Defaults to {@link Severity#UNDEFINED_SEVERITY_NUMBER}.
   */
  public abstract Severity getMinimumSeverity();

  /**
   * Returns {@code true} if this logger should only process log records from traces when the trace
   * is sampled.
   *
   * <p>When enabled, log records from unsampled traces will be dropped. Log records that are not
   * associated with a trace context are unaffected.
   *
   * <p>Defaults to {@code false}.
   */
  public abstract boolean isTraceBased();
}
