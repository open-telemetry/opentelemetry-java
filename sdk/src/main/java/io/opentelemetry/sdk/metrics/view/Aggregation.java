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

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Aggregation} is the process of combining a certain set of recorded measurements for a
 * given {@code Instrument} into the equivalent {@code MetricData}.
 *
 * @since 0.1.0
 */
@Immutable
public interface Aggregation {

  /**
   * Returns an {@code AggregationFactory} that can be used to produce the {@link
   * io.opentelemetry.sdk.metrics.aggregator.Aggregator} that needs to be used to aggregate all the
   * values to produce this {@code Aggregation}.
   *
   * @param instrumentValueType the type of recorded values for the {@code Instrument}.
   * @return the {@code AggregationFactory}.
   */
  AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType);

  /**
   * Returns the {@link MetricData.Descriptor.Type} that this {@code Aggregation} will produce.
   *
   * @param instrumentType the type of the {@code Instrument}.
   * @param instrumentValueType the type of recorded values for the {@code Instrument}.
   * @return the {@link MetricData.Descriptor.Type} that this {@code Aggregation} will produce.
   */
  MetricData.Descriptor.Type getDescriptorType(
      InstrumentType instrumentType, InstrumentValueType instrumentValueType);

  /**
   * Returns the unit that this {@code Aggregation} will produce.
   *
   * @param initialUnit the initial unit for the {@code Instrument}'s measurements.
   * @return the unit that this {@code Aggregation} will produce.
   */
  String getUnit(String initialUnit);

  /**
   * Returns {@code true} if this {@code Aggregation} can be applied to the given {@code
   * InstrumentType}.
   *
   * @param instrumentType the type of the {@code Instrument}.
   * @return {@code true} if this {@code Aggregation} can be applied to the given {@code
   *     InstrumentType}.
   */
  boolean availableForInstrument(InstrumentType instrumentType);
}
