/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import javax.annotation.concurrent.Immutable;

/** Factory class for {@link Aggregator}. */
@Immutable
public interface AggregatorFactory {

  /**
   * Returns a new {@link Aggregator}.
   *
   * @return a new {@link Aggregator}.
   */
  Aggregator getAggregator();
}
