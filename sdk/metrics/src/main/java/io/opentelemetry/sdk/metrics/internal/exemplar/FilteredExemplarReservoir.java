/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.List;

/** A reservoir that has a pre-filter on measurements. */
class FilteredExemplarReservoir implements ExemplarReservoir {
  private final ExemplarFilter filter;
  private final ExemplarReservoir reservoir;

  FilteredExemplarReservoir(ExemplarFilter filter, ExemplarReservoir reservoir) {
    this.filter = filter;
    this.reservoir = reservoir;
  }

  @Override
  public void offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerDoubleMeasurement(value, attributes, context);
    }
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerLongMeasurement(value, attributes, context);
    }
  }

  @Override
  public List<DoubleExemplarData> collectAndResetDoubles(Attributes pointAttributes) {
    return reservoir.collectAndResetDoubles(pointAttributes);
  }

  @Override
  public List<LongExemplarData> collectAndResetLongs(Attributes pointAttributes) {
    return reservoir.collectAndResetLongs(pointAttributes);
  }
}
