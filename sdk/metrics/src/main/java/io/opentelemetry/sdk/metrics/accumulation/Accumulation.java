/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.accumulation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/** An immutable representation of an accumulated value by an {@code Aggregator}. */
@Immutable
public interface Accumulation {
  /**
   * Returns the {@code Point} with the given properties and the value from this Accumulation.
   *
   * @param startEpochNanos the startEpochNanos for the {@code Point}.
   * @param epochNanos the epochNanos for the {@code Point}.
   * @param labels the labels for the {@code Point}.
   * @return the {@code Point} with the value from this Aggregation.
   */
  MetricData.Point toPoint(long startEpochNanos, long epochNanos, Labels labels);
}
