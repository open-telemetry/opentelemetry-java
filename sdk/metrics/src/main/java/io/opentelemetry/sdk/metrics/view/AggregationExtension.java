/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import java.util.function.Function;

/**
 * Extensions for configuring aggregation on Views.
 *
 * <p>This class provides aggregation not included in the OpenTelemetry metrics SDK specification.
 */
public class AggregationExtension {
  private AggregationExtension() {}

  /**
   * Records a count of all measurements seen, reported as a monotonic Sum.
   *
   * @param temporality either DELTA or CUMULATIVE reporting.
   */
  public static Aggregation count(AggregationTemporality temporality) {
    return Aggregation.make("count", i -> AggregatorFactory.count(temporality));
  }

  /**
   * Aggregates measurements, preserving a count of all measurements seen, a sum of all measurements
   * and the maximum and minimum values.
   *
   * <p>Reports as a Summary metric.
   */
  public static Aggregation minMaxSumCount() {
    return Aggregation.make("minMaxSumCount", i -> AggregatorFactory.minMaxSumCount());
  }

  /**
   * Constructs a custom aggregation of measurements into metrics.
   *
   * @param name The name of the aggregation.
   * @param factory Constructor of AggregatorFactory per-instrument.
   */
  public static Aggregation custom(
      String name, Function<InstrumentDescriptor, AggregatorFactory> factory) {
    return Aggregation.make(name, factory);
  }
}
