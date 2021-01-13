/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Labels;
import javax.annotation.concurrent.Immutable;

@Immutable
public interface Point {
  /**
   * Returns the start epoch timestamp in nanos of this {@code Instrument}, usually the time when
   * the metric was created or an aggregation was enabled.
   *
   * @return the start epoch timestamp in nanos.
   */
  long getStartEpochNanos();

  /**
   * Returns the epoch timestamp in nanos when data were collected, usually it represents the moment
   * when {@code Instrument.getData()} was called.
   *
   * @return the epoch timestamp in nanos.
   */
  long getEpochNanos();

  /**
   * Returns the labels associated with this {@code Point}.
   *
   * @return the labels associated with this {@code Point}.
   */
  Labels getLabels();
}
