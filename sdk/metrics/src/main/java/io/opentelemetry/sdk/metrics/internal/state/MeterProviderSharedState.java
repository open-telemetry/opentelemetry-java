/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.ScopeSelector;
import io.opentelemetry.sdk.metrics.MeterConfig;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.Immutable;

/**
 * State for a {@link SdkMeterProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MeterProviderSharedState {

  private AtomicReference<LinkedHashMap<ScopeSelector, MeterConfig>> meterConfigMapRef =
      new AtomicReference<>();

  public static MeterProviderSharedState create(
      Clock clock,
      Resource resource,
      ExemplarFilter exemplarFilter,
      long startEpochNanos,
      LinkedHashMap<ScopeSelector, MeterConfig> meterConfigMap) {
    MeterProviderSharedState sharedState =
        new AutoValue_MeterProviderSharedState(clock, resource, startEpochNanos, exemplarFilter);
    sharedState.meterConfigMapRef.set(meterConfigMap);
    return sharedState;
  }

  MeterProviderSharedState() {}

  /** Returns the {@link Clock} used for measurements. */
  public abstract Clock getClock();

  /** Returns the {@link Resource} to attach telemetry to. */
  public abstract Resource getResource();

  /** Returns the timestamp when the {@link SdkMeterProvider} was started, in epoch nanos. */
  public abstract long getStartEpochNanos();

  /** Returns the {@link ExemplarFilter} for remembering synchronous measurements. */
  abstract ExemplarFilter getExemplarFilter();

  public MeterConfig getMeterConfig(InstrumentationScopeInfo instrumentationScopeInfo) {
    LinkedHashMap<ScopeSelector, MeterConfig> meterConfigMap =
        Objects.requireNonNull(meterConfigMapRef.get());
    for (ScopeSelector scopeSelector : meterConfigMap.keySet()) {
      if (scopeSelector.matchesScope(instrumentationScopeInfo)) {
        return meterConfigMap.get(scopeSelector);
      }
    }
    return MeterConfig.defaultConfig();
  }
}
