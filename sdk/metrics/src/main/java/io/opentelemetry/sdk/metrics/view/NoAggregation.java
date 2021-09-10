/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;

/** Configuration representing no aggregation. */
public class NoAggregation extends Aggregation {

  NoAggregation() {}

  @Override
  public AggregatorFactory getFactory(InstrumentDescriptor instrument) {
    return null;
  }

  @Override
  public Aggregation resolve(InstrumentDescriptor instrument) {
    return this;
  }
}
