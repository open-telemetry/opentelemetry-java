/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Meter}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 *
 * @see SdkMeterProviderUtil#setMeterConfigurator(SdkMeterProviderBuilder, ScopeConfigurator)
 * @see SdkMeterProviderUtil#addMeterConfiguratorCondition(SdkMeterProviderBuilder, Predicate,
 *     MeterConfig)
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
   * Returns the default {@link MeterConfig}, which is used when no configurator is set or when the
   * meter configurator returns {@code null} for a {@link InstrumentationScopeInfo}.
   */
  public static MeterConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  /**
   * Create a {@link ScopeConfiguratorBuilder} for configuring {@link
   * SdkMeterProviderUtil#setMeterConfigurator(SdkMeterProviderBuilder, ScopeConfigurator)}.
   */
  public static ScopeConfiguratorBuilder<MeterConfig> configuratorBuilder() {
    return ScopeConfigurator.builder();
  }

  MeterConfig() {}

  /** Returns {@code true} if this meter is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
