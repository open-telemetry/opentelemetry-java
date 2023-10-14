/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

public class SpanUpdatingExemplarReservoir<T extends ExemplarData> implements ExemplarReservoir<T> {
  private static final AttributeKey<Boolean> KEY_EXEMPLAR =
      AttributeKey.booleanKey("otel.exemplar");
  private final ExemplarReservoir<T> reservoir;

  SpanUpdatingExemplarReservoir(ExemplarReservoir<T> reservoir) {
    this.reservoir = reservoir;
  }

  @Override
  public boolean offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    return updateSpan(reservoir.offerDoubleMeasurement(value, attributes, context), context);
  }

  @Override
  public boolean offerLongMeasurement(long value, Attributes attributes, Context context) {
    return updateSpan(reservoir.offerLongMeasurement(value, attributes, context), context);
  }

  @Override
  public List<T> collectAndReset(Attributes pointAttributes) {
    return reservoir.collectAndReset(pointAttributes);
  }

  private static boolean updateSpan(boolean success, Context context) {
    if (success) {
      Span.fromContext(context).setAttribute(KEY_EXEMPLAR, true);
    }
    return success;
  }
}
