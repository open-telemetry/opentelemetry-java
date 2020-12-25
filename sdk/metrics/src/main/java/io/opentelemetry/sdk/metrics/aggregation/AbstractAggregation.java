/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.sdk.metrics.aggregator.Aggregator;

abstract class AbstractAggregation<T extends Accumulation> implements Aggregation<T> {
  private final Aggregator<T> aggregator;

  AbstractAggregation(Aggregator<T> aggregator) {
    this.aggregator = aggregator;
  }

  @Override
  public final Aggregator<T> getAggregator() {
    return aggregator;
  }
}
