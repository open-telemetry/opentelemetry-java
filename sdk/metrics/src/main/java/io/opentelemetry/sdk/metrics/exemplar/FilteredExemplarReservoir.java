/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

/** Implementation of a reservoir that has a pre-filter on measurements. */
class FilteredExemplarReservoir implements ExemplarReservoir {
  private final ExemplarFilter filter;
  private final ExemplarReservoir reservoir;

  FilteredExemplarReservoir(ExemplarFilter filter, ExemplarReservoir reservoir) {
    this.filter = filter;
    this.reservoir = reservoir;
  }

  @Override
  public void offerMeasurement(long value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerMeasurement(value, attributes, context);
    }
  }

  @Override
  public void offerMeasurement(double value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerMeasurement(value, attributes, context);
    }
  }

  @Override
  public List<ExemplarData> collectAndReset(Attributes pointAttributes) {
    return reservoir.collectAndReset(pointAttributes);
  }
}
