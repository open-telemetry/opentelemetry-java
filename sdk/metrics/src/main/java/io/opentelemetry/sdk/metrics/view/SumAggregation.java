/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;

/** A sum aggregation configuration. */
public class SumAggregation extends Aggregation {
  private final AggregationTemporality temporality;

  SumAggregation(AggregationTemporality temporality) {
    this.temporality = temporality;
  }

  /** Returns the configured temporality for the sum aggregation. */
  public AggregationTemporality getTemporality() {
    return temporality;
  }

  @Override
  public AggregatorFactory getFactory(InstrumentDescriptor instrument) {
    return AggregatorFactory.sum(temporality);
  }

  @Override
  public Aggregation resolve(InstrumentDescriptor instrument) {
    return this;
  }

  @Override
  public String toString() {
    return "sum(" + temporality + ")";
  }
}
