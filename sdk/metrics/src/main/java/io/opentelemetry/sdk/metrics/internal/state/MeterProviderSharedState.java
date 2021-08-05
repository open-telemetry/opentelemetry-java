/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.ViewRegistry;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class MeterProviderSharedState {
  public static MeterProviderSharedState create(
      Clock clock, Resource resource, ViewRegistry viewRegistry) {
    return new AutoValue_MeterProviderSharedState(clock, resource, viewRegistry, clock.now());
  }

  public abstract Clock getClock();

  abstract Resource getResource();

  abstract ViewRegistry getViewRegistry();

  abstract long getStartEpochNanos();

  // TODO: Move this.
  public <T> Aggregator<T> getAggregator(
      MeterSharedState meterSharedState, InstrumentDescriptor descriptor) {
    return getViewRegistry()
        .findView(descriptor)
        .getAggregatorFactory()
        .create(getResource(), meterSharedState.getInstrumentationLibraryInfo(), descriptor);
  }

  // TODO: Move this.
  public LabelsProcessor getLabelsProcessor(
      MeterSharedState meterSharedState, InstrumentDescriptor descriptor) {
    return getViewRegistry()
        .findView(descriptor)
        .getLabelsProcessorFactory()
        .create(getResource(), meterSharedState.getInstrumentationLibraryInfo(), descriptor);
  }
}
