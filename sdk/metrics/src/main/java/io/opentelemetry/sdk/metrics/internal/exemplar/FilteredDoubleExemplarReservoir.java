/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.List;

/** Implementation of a reservoir that has a pre-filter on measurements. */
class FilteredDoubleExemplarReservoir implements DoubleExemplarReservoir {
  private final ExemplarFilter filter;
  private final DoubleExemplarReservoir reservoir;

  FilteredDoubleExemplarReservoir(ExemplarFilter filter, DoubleExemplarReservoir reservoir) {
    this.filter = filter;
    this.reservoir = reservoir;
  }

  @Override
  public void offerMeasurement(double value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerMeasurement(value, attributes, context);
    }
  }

  @Override
  public List<DoubleExemplarData> collectAndReset(Attributes pointAttributes) {
    return reservoir.collectAndReset(pointAttributes);
  }
}
