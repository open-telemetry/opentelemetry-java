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

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.ViewSpecification.Temporality;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
class ViewRegistry {

  public static final ViewSpecification CUMULATIVE_SUM =
      ViewSpecification.create(Aggregations.sum(), Temporality.CUMULATIVE);
  public static final ViewSpecification DELTA_SUMMARY =
      ViewSpecification.create(Aggregations.minMaxSumCount(), Temporality.DELTA);
  public static final ViewSpecification CUMULATIVE_LAST_VALUE =
      ViewSpecification.create(Aggregations.lastValue(), Temporality.CUMULATIVE);

  private final Map<InstrumentSelector, ViewSpecification> configuration =
      new ConcurrentHashMap<>();

  /**
   * Create a new {@link io.opentelemetry.sdk.metrics.Batcher} for use in metric recording
   * aggregation.
   */
  Batcher createBatcher(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor) {

    ViewSpecification specification = findBestMatch(descriptor);

    Aggregation aggregation = specification.aggregation();

    if (Temporality.CUMULATIVE == specification.temporality()) {
      return Batchers.getCumulativeAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    } else if (Temporality.DELTA == specification.temporality()) {
      return Batchers.getDeltaAllLabels(
          descriptor, meterProviderSharedState, meterSharedState, aggregation);
    }
    throw new IllegalStateException("unsupported Temporality: " + specification.temporality());
  }

  // todo: consider moving this method to its own class, for more targetted testing.
  private ViewSpecification findBestMatch(InstrumentDescriptor descriptor) {
    // select based on InstrumentType:
    for (Map.Entry<InstrumentSelector, ViewSpecification> entry : configuration.entrySet()) {
      InstrumentSelector registeredSelector = entry.getKey();
      if (registeredSelector.instrumentType().equals(descriptor.getType())) {
        return entry.getValue();
      }
    }
    // If none found, use the defaults:
    return getDefaultSpecification(descriptor);
  }

  private static ViewSpecification getDefaultSpecification(InstrumentDescriptor descriptor) {
    switch (descriptor.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
        return CUMULATIVE_SUM;
      case VALUE_OBSERVER:
      case VALUE_RECORDER:
        return DELTA_SUMMARY;
      case SUM_OBSERVER:
      case UP_DOWN_SUM_OBSERVER:
        return CUMULATIVE_LAST_VALUE;
    }
    throw new IllegalArgumentException("Unknown descriptor type: " + descriptor.getType());
  }

  /** todo: javadoc me. */
  public void registerView(InstrumentSelector selector, ViewSpecification specification) {
    configuration.put(selector, specification);
  }
}
