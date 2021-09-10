/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;

/** Last-value aggregation configuration. */
public class LastValueAggregation extends Aggregation {
  LastValueAggregation() {}

  @Override
  public AggregatorFactory getFactory(InstrumentDescriptor instrument) {
    return AggregatorFactory.lastValue();
  }

  @Override
  public Aggregation resolve(InstrumentDescriptor instrument) {
    return this;
  }

  @Override
  public String toString() {
    return "lastValue";
  }
}
