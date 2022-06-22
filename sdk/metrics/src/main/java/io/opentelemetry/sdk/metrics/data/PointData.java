/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A point in the metric data model.
 *
 * <p>A point represents the aggregation of measurements recorded with a particular set of {@link
 * Attributes} over some time interval.
 *
 * @since 1.14.0
 */
@Immutable
public interface PointData {
  /** Returns the start time of the aggregation in epoch nanos. */
  long getStartEpochNanos();

  /** Returns the end time of the aggregation in epoch nanos. */
  long getEpochNanos();

  /** Returns the attributes of the aggregation. */
  Attributes getAttributes();

  /** List of exemplars collected from measurements aggregated into this point. */
  List<? extends ExemplarData> getExemplars();
}
