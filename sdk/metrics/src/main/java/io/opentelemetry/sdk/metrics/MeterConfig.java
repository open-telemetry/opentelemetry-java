/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.ScopeConfigurator;
import io.opentelemetry.sdk.common.ScopeConfiguratorBuilder;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Meter}.
 *
 * @see SdkMeterProviderBuilder#setMeterConfigurator(ScopeConfigurator)
 * @see SdkMeterProviderBuilder#addMeterConfiguratorCondition(Predicate, MeterConfig)
 */
@AutoValue
@Immutable
public abstract class MeterConfig {

  private static final MeterConfig DEFAULT_CONFIG = new AutoValue_MeterConfig(/* enabled= */ true);
  private static final MeterConfig DISABLED_CONFIG =
      new AutoValue_MeterConfig(/* enabled= */ false);

  /** Returns a disabled {@link MeterConfig}. */
  public static MeterConfig disabled() {
    return DISABLED_CONFIG;
  }

  /** Returns an enabled {@link MeterConfig}. */
  public static MeterConfig enabled() {
    return DEFAULT_CONFIG;
  }

  /**
   * Returns the default {@link MeterConfig}, which is used when no {@link
   * SdkMeterProviderBuilder#setMeterConfigurator(ScopeConfigurator)} is set or when the meter
   * configurator returns {@code null} for a {@link InstrumentationScopeInfo}.
   */
  public static MeterConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  /**
   * Create a {@link ScopeConfiguratorBuilder} for configuring {@link
   * SdkMeterProviderBuilder#setMeterConfigurator(ScopeConfigurator)}.
   */
  public static ScopeConfiguratorBuilder<MeterConfig> configuratorBuilder() {
    return ScopeConfigurator.builder();
  }

  MeterConfig() {}

  /** Returns {@code true} if this meter is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
