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

class LongToDoubleExemplarReservoir implements ExemplarReservoir {

  private final ExemplarReservoir delegate;

  LongToDoubleExemplarReservoir(ExemplarReservoir delegate) {
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
  public List<DoubleExemplarData> collectAndResetDoubles(Attributes pointAttributes) {
    return delegate.collectAndResetDoubles(pointAttributes);
  }

  @Override
  public List<LongExemplarData> collectAndResetLongs(Attributes pointAttributes) {
    throw new UnsupportedOperationException(
        "This exemplar reservoir does not support collecting long values.");
  }
}
