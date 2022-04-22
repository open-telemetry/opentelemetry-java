/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;

abstract class AbstractSumAggregator<T, U extends ExemplarData> implements Aggregator<T, U> {
  private final boolean isMonotonic;

  AbstractSumAggregator(InstrumentDescriptor instrumentDescriptor) {
    this.isMonotonic = MetricDataUtils.isMonotonicInstrument(instrumentDescriptor);
  }

  final boolean isMonotonic() {
    return isMonotonic;
  }
}
