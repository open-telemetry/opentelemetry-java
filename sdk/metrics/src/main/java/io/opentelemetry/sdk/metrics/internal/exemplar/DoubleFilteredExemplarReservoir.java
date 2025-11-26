/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.List;

/** A reservoir that has a pre-filter on measurements. */
class DoubleFilteredExemplarReservoir implements DoubleExemplarReservoir {
  private final ExemplarFilterInternal filter;
  private final DoubleExemplarReservoir reservoir;

  DoubleFilteredExemplarReservoir(
      ExemplarFilterInternal filter, DoubleExemplarReservoir reservoir) {
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
  public List<DoubleExemplarData> collectAndResetDoubles(Attributes pointAttributes) {
    return reservoir.collectAndResetDoubles(pointAttributes);
  }
}
