/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.Supplier;
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

  public static MeterProviderSharedState create(
      Clock clock,
      Supplier<Resource> resourceSupplier,
      ExemplarFilter exemplarFilter,
      long startEpochNanos) {
    MeterProviderSharedState sharedState =
        new AutoValue_MeterProviderSharedState(
            clock, resourceSupplier, startEpochNanos, exemplarFilter);
    return sharedState;
  }

  MeterProviderSharedState() {}

  /** Returns the {@link Clock} used for measurements. */
  public abstract Clock getClock();

  /** Returns the {@link Resource} to attach telemetry to. */
  public Resource getResource() {
    return getResourceSupplier().get();
  }

  abstract Supplier<Resource> getResourceSupplier();

  /** Returns the timestamp when the {@link SdkMeterProvider} was started, in epoch nanos. */
  public abstract long getStartEpochNanos();

  /** Returns the {@link ExemplarFilter} for remembering synchronous measurements. */
  public abstract ExemplarFilter getExemplarFilter();
}
