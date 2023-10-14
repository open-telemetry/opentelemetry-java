/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.Collections;
import java.util.List;

/** A reservoir that keeps no exemplars. */
class NoopExemplarReservoir<T extends ExemplarData> implements ExemplarReservoir<T> {

  static final NoopExemplarReservoir<LongExemplarData> LONG_INSTANCE =
      new NoopExemplarReservoir<>();
  static final NoopExemplarReservoir<DoubleExemplarData> DOUBLE_INSTANCE =
      new NoopExemplarReservoir<>();

  private NoopExemplarReservoir() {}

  @Override
  public boolean offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    // Do nothing
    return false;
  }

  @Override
  public boolean offerLongMeasurement(long value, Attributes attributes, Context context) {
    // Do nothing
    return false;
  }

  @Override
  public List<T> collectAndReset(Attributes pointAttributes) {
    return Collections.emptyList();
  }
}
