/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/** State for a {@code MeterProvider}. */
@AutoValue
@Immutable
public abstract class MeterProviderSharedState {
  public static MeterProviderSharedState create(
      Clock clock, Resource resource, ViewRegistry viewRegistry) {
    return new AutoValue_MeterProviderSharedState(clock, resource, viewRegistry, clock.now());
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

  // TODO: Move this.
  /** Returns the {@link Aggregator} to use for a given instrument. */
  public <T> Aggregator<T> getAggregator(
      MeterSharedState meterSharedState, InstrumentDescriptor descriptor) {
    return getViewRegistry()
        .findView(descriptor)
        .getAggregatorFactory()
        .create(getResource(), meterSharedState.getInstrumentationLibraryInfo(), descriptor);
  }

  // TODO: Move this.
  /** Returns the {@link LabelsProcessor} to use for a given instrument. */
  public LabelsProcessor getLabelsProcessor(
      MeterSharedState meterSharedState, InstrumentDescriptor descriptor) {
    return getViewRegistry()
        .findView(descriptor)
        .getLabelsProcessorFactory()
        .create(getResource(), meterSharedState.getInstrumentationLibraryInfo(), descriptor);
  }
}
