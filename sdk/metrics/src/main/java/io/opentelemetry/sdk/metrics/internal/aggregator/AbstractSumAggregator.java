/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;

abstract class AbstractSumAggregator<T> implements Aggregator<T> {
  private final boolean isMonotonic;

  AbstractSumAggregator(InstrumentDescriptor instrumentDescriptor) {
    this.isMonotonic = MetricDataUtils.isMonotonicInstrument(instrumentDescriptor);
  }

  final boolean isMonotonic() {
    return isMonotonic;
  }
}
