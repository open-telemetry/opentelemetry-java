/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import java.util.function.Function;


/**
 * Configuration for how to perform aggregation of measurements.
 */
public abstract class AggregationConfig {
  // Don't allow out-of-package instantiation.
  AggregationConfig() {}

  /**
   * Returns the appropriate aggregator factory for a given instrument.
   *
   * @return The AggregatorFactory or {@code null} if none.
   */
  public abstract AggregatorFactory config(InstrumentDescriptor instrument);

  static AggregationConfig make(
      String name, Function<InstrumentDescriptor, AggregatorFactory> factory) {
    return new AggregationConfig() {

      @Override
      public AggregatorFactory config(InstrumentDescriptor instrument) {
        return factory.apply(instrument);
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }
}
