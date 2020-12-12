/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Aggregation} is the process of combining a certain set of recorded measurements for a
 * given {@code Instrument} into the equivalent {@code MetricData}.
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
   * Returns the {@link MetricData} that this {@code Aggregation} will produce.
   *
   * @param resource the Resource associated with the {@code Instrument}.
   * @param instrumentationLibraryInfo the InstrumentationLibraryInfo associated with the {@code
   *     Instrument}.
   * @param descriptor the InstrumentDescriptor of the {@code Instrument}.
   * @param aggregatorMap the map of Labels to Aggregators.
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @return the {@link MetricData.Type} that this {@code Aggregation} will produce.
   */
  @Nullable
  MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, Aggregator> aggregatorMap,
      long startEpochNanos,
      long epochNanos);
}
