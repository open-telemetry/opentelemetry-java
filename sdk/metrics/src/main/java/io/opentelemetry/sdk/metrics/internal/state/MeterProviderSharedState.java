/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * State for a {@code MeterProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MeterProviderSharedState {
  public static MeterProviderSharedState create(
      Clock clock, Resource resource, ViewRegistry viewRegistry, ExemplarSampler sampler) {
    return new AutoValue_MeterProviderSharedState(
        clock, resource, viewRegistry, clock.now(), sampler);
  }

  /** Returns the clock used for measurements. */
  public abstract Clock getClock();

  /** Returns the {@link Resource} to attach telemetry to. */
  abstract Resource getResource();

  /** Returns the {@link ViewRegistry} for custom aggregation and metric definitions. */
  abstract ViewRegistry getViewRegistry();

  /**
   * Returns the timestamp when this {@code MeterProvider} was started, in nanoseconds since Unix
   * epoch time.
   */
  abstract long getStartEpochNanos();

  /** Returns the {@link ExemplarSampler} for remembering synchronous measurements. */
  abstract ExemplarSampler getExemplarSampler();
}
