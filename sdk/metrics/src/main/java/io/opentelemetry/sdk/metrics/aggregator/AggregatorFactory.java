/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

/** Factory class for {@link Aggregator}. */
public interface AggregatorFactory {

  /**
   * Returns a new {@link Aggregator}.
   *
   * @return a new {@link Aggregator}.
   */
  Aggregator getAggregator();
}
