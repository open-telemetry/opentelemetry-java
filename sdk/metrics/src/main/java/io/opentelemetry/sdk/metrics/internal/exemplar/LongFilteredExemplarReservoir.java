/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.List;

/** A reservoir that has a pre-filter on measurements. */
class LongFilteredExemplarReservoir implements LongExemplarReservoir {
  private final ExemplarFilterInternal filter;
  private final LongExemplarReservoir reservoir;

  LongFilteredExemplarReservoir(ExemplarFilterInternal filter, LongExemplarReservoir reservoir) {
    this.filter = filter;
    this.reservoir = reservoir;
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    if (filter.shouldSampleMeasurement(value, attributes, context)) {
      reservoir.offerLongMeasurement(value, attributes, context);
    }
  }

  @Override
  public List<LongExemplarData> collectAndResetLongs(Attributes pointAttributes) {
    return reservoir.collectAndResetLongs(pointAttributes);
  }
}
