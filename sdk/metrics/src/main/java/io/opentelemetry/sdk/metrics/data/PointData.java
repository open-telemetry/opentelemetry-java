/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/**
 * A point in the "Metric stream" data model.
 *
 * <p>This is distinguished from {@code Measurement} in that it may have aggregated data, and has
 * its type defined by the metric data model (no longer an instrument).
 */
@Immutable
public interface PointData {
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
   * Returns the attributes associated with this {@code Point}.
   *
   * @return the attributes associated with this {@code Point}.
   */
  Attributes getAttributes();
  /**
   * List of exemplars collected from measurements that were used to form the data point.
   */
  Collection<Exemplar> getExemplars();
}
