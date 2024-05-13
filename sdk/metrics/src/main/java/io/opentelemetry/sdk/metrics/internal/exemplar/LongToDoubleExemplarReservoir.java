/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

class LongToDoubleExemplarReservoir<T extends ExemplarData> implements ExemplarReservoir<T> {

  private final ExemplarReservoir<T> delegate;

  LongToDoubleExemplarReservoir(ExemplarReservoir<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    delegate.offerDoubleMeasurement(value, attributes, context);
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    offerDoubleMeasurement((double) value, attributes, context);
  }

  @Override
  public List<T> collectAndReset(Attributes pointAttributes) {
    return delegate.collectAndReset(pointAttributes);
  }
}
