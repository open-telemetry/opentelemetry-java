/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import java.util.List;

/** A reservoir that records the latest measurement for each histogram bucket. */
class HistogramExemplarReservoir extends FixedSizeExemplarReservoir {

  HistogramExemplarReservoir(Clock clock, List<Double> boundaries) {
    super(clock, boundaries.size() + 1, new HistogramCellSelector(boundaries));
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    super.offerDoubleMeasurement((double) value, attributes, context);
  }

  static class HistogramCellSelector implements ReservoirCellSelector {

    private final double[] boundaries;

    private HistogramCellSelector(List<Double> boundaries) {
      this.boundaries = ExplicitBucketHistogramUtils.createBoundaryArray(boundaries);
    }

    @Override
    public int reservoirCellIndexFor(
        ReservoirCell[] cells, long value, Attributes attributes, Context context) {
      return reservoirCellIndexFor(cells, (double) value, attributes, context);
    }

    @Override
    public int reservoirCellIndexFor(
        ReservoirCell[] cells, double value, Attributes attributes, Context context) {
      return ExplicitBucketHistogramUtils.findBucketIndex(boundaries, value);
    }

    @Override
    public void reset() {
      // Do nothing
    }
  }
}
