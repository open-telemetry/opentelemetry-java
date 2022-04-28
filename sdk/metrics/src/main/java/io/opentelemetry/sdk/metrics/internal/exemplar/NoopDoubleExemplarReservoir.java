/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.Collections;
import java.util.List;

/** Implementation of a reservoir that keeps no exemplars. */
class NoopDoubleExemplarReservoir implements DoubleExemplarReservoir {

  static final NoopDoubleExemplarReservoir INSTANCE = new NoopDoubleExemplarReservoir();

  private NoopDoubleExemplarReservoir() {}

  @Override
  public void offerMeasurement(double value, Attributes attributes, Context context) {
    // Stores nothing.
  }

  @Override
  public List<DoubleExemplarData> collectAndReset(Attributes pointAttributes) {
    return Collections.emptyList();
  }
}
