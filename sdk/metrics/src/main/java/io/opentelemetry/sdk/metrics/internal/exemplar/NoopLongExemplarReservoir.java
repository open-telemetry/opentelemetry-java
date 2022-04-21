/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;

import java.util.Collections;
import java.util.List;

/** Implementation of a reservoir that keeps no exemplars. */
class NoopLongExemplarReservoir implements LongExemplarReservoir {

  static final NoopLongExemplarReservoir INSTANCE = new NoopLongExemplarReservoir();

  private NoopLongExemplarReservoir() {}

  @Override
  public void offerMeasurement(long value, Attributes attributes, Context context) {
    // Stores nothing.
  }

  @Override
  public List<LongExemplarData> collectAndReset(Attributes pointAttributes) {
    return Collections.emptyList();
  }
}
