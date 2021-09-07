/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.Collections;
import java.util.List;

/** Implementation of a reservoir that keeps no exemplars. */
class NoExemplarReservoir implements ExemplarReservoir {

  static final ExemplarReservoir NO_SAMPLES = new NoExemplarReservoir();

  private NoExemplarReservoir() {}

  @Override
  public void offerMeasurement(long value, Attributes attributes, Context context) {
    // Stores nothing
  }

  @Override
  public void offerMeasurement(double value, Attributes attributes, Context context) {
    // Stores nothing.
  }

  @Override
  public List<Exemplar> collectAndReset(Attributes pointAttributes) {
    return Collections.emptyList();
  }
}
