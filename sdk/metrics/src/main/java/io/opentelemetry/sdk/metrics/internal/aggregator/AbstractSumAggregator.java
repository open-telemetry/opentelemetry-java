/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;

abstract class AbstractSumAggregator<T extends PointData, U extends ExemplarData>
    implements Aggregator<T> {
  private final boolean isMonotonic;

  AbstractSumAggregator(InstrumentDescriptor instrumentDescriptor) {
    this.isMonotonic = isMonotonicInstrument(instrumentDescriptor);
  }

  /** Returns true if the instrument does not allow negative measurements. */
  private static boolean isMonotonicInstrument(InstrumentDescriptor descriptor) {
    InstrumentType type = descriptor.getType();
    return type == InstrumentType.HISTOGRAM
        || type == InstrumentType.COUNTER
        || type == InstrumentType.OBSERVABLE_COUNTER;
  }

  final boolean isMonotonic() {
    return isMonotonic;
  }
}
