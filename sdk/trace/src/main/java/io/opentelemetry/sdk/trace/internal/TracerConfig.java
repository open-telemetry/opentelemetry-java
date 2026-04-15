/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.util.function.Predicate;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Tracer}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 *
 * @see SdkTracerProviderUtil#setTracerConfigurator(SdkTracerProviderBuilder, ScopeConfigurator)
 * @see SdkTracerProviderUtil#addTracerConfiguratorCondition(SdkTracerProviderBuilder, Predicate,
 *     TracerConfig)
 */
@AutoValue
@Immutable
public abstract class TracerConfig {

  private static final TracerConfig DEFAULT_CONFIG =
      new AutoValue_TracerConfig(/* enabled= */ true);
  private static final TracerConfig DISABLED_CONFIG =
      new AutoValue_TracerConfig(/* enabled= */ false);

  /** Returns a disabled {@link TracerConfig}. */
  public static TracerConfig disabled() {
    return DISABLED_CONFIG;
  }

  /** Returns an enabled {@link TracerConfig}. */
  public static TracerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  /**
   * Returns the default {@link TracerConfig}, which is used when no configurator is set or when the
   * tracer configurator returns {@code null} for a {@link InstrumentationScopeInfo}.
   */
  public static TracerConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  /**
   * Create a {@link ScopeConfiguratorBuilder} for configuring {@link
   * SdkTracerProviderUtil#setTracerConfigurator(SdkTracerProviderBuilder, ScopeConfigurator)}.
   */
  public static ScopeConfiguratorBuilder<TracerConfig> configuratorBuilder() {
    return ScopeConfigurator.builder();
  }

  TracerConfig() {}

  /** Returns {@code true} if this tracer is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
