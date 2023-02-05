/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Collection;
import java.util.Collections;

/**
 * Auto value implementation of {@link ExponentialHistogramData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class MutableExponentialHistogramData implements ExponentialHistogramData {

  private AggregationTemporality temporality = AggregationTemporality.CUMULATIVE;
  private Collection<ExponentialHistogramPointData> pointData = Collections.emptyList();

  public MutableExponentialHistogramData() {}

  @Override
  public AggregationTemporality getAggregationTemporality() {
    return temporality;
  }

  @Override
  public Collection<ExponentialHistogramPointData> getPoints() {
    return pointData;
  }

  /** Set the values. */
  public void set(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> pointData) {
    this.temporality = temporality;
    this.pointData = pointData;
  }
}
