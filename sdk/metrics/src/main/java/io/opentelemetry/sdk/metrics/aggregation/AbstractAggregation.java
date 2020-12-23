/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;

abstract class AbstractAggregation<T extends Accumulation> implements Aggregation<T> {
  private final AggregatorFactory<T> aggregatorFactory;

  AbstractAggregation(AggregatorFactory<T> aggregatorFactory) {
    this.aggregatorFactory = aggregatorFactory;
  }

  @Override
  public final AggregatorFactory<T> getAggregatorFactory() {
    return aggregatorFactory;
  }
}
