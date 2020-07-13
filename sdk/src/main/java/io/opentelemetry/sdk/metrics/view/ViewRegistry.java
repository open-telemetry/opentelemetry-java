/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.Batcher;
import io.opentelemetry.sdk.metrics.Batchers;
import io.opentelemetry.sdk.metrics.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.MeterSharedState;

// notes:
//  specify by pieces of the descriptor.
//    instrument type
//    instrument value type
//    instrument name  (wildcards allowed?)
//    constant labels (?)
//    units (?)

// what you can choose:
//   aggregation
//   all labels vs. a list of labels
//   delta vs. cumulative

/**
 * Central location for Views to be registered. Registration of a view should eventually be done via
 * the {@link io.opentelemetry.sdk.metrics.MeterSdkProvider}.
 */
public class ViewRegistry {

  /** Create a new {@link Batcher} for use in metric recording aggregation. */
  public Batcher createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    Aggregation aggregation = getRegisteredAggregation(descriptor);

    // todo: don't just use the defaults!
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case SUM_OBSERVER:
      case UP_DOWN_SUM_OBSERVER:
        return Batchers.getCumulativeAllLabels(
            descriptor, meterProviderSharedState, meterSharedState, aggregation);
      case VALUE_RECORDER:
        // TODO: Revisit the batcher used here for value observers,
        // currently this does not remove duplicate records in the same cycle.
      case VALUE_OBSERVER:
        return Batchers.getDeltaAllLabels(
            descriptor, meterProviderSharedState, meterSharedState, aggregation);
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }

  private static Aggregation getRegisteredAggregation(InstrumentDescriptor descriptor) {
    // todo look up based on fields of the descriptor.
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
        return Aggregations.sum();
      case VALUE_RECORDER:
      case VALUE_OBSERVER:
        return Aggregations.minMaxSumCount();
      case SUM_OBSERVER:
      case UP_DOWN_SUM_OBSERVER:
        return Aggregations.lastValue();
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }
}
