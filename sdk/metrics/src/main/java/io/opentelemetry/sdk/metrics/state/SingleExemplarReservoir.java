/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** A resorvoir of exemplars having exactly a size of 1. */
public final class SingleExemplarReservoir extends AbstractExemplarReservoir {
  // TODO: Find a storage mechanism that doesn't involve allocations.
  private final AtomicReference<Exemplar> current = new AtomicReference<>();

  public SingleExemplarReservoir(Clock clock) {
    super(clock);
  }


  @Override
  public void offerMeasurementLong(long value, Attributes attributes, Context context) {
    if (hasSamplingSpan(context)) {
      current.lazySet(makeLongExmeplar(value, attributes, context));
    }
  }

  @Override
  public void offerMeasurementDouble(double value, Attributes attributes, Context context) {
    if (hasSamplingSpan(context)) {
      current.lazySet(makeDoubleExmeplar(value, attributes, context));
    }
  }

  @Override
  public List<Exemplar> collectAndReset(Attributes pointAttributes) {
    Exemplar sample = current.getAndSet(null);
    if (sample != null) {
      return Collections.singletonList(sample.filterAttributes(pointAttributes));
    }
    return Collections.emptyList();
  }
}
