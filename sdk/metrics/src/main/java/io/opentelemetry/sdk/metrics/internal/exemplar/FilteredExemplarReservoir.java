/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

/** A reservoir that has a pre-filter on measurements. */
class FilteredExemplarReservoir<T extends ExemplarData> implements ExemplarReservoir<T> {
  private final ExemplarFilter filter;
  private final ExemplarReservoir<T> reservoir;

  FilteredExemplarReservoir(ExemplarFilter filter, ExemplarReservoir<T> reservoir) {
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
  public List<T> collectAndReset(Attributes pointAttributes) {
    return reservoir.collectAndReset(pointAttributes);
  }
}
