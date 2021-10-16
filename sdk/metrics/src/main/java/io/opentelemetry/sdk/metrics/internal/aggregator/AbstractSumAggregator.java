/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;

abstract class AbstractSumAggregator<T> implements Aggregator<T> {
  private final boolean isMonotonic;

  AbstractSumAggregator(InstrumentDescriptor instrumentDescriptor) {
    InstrumentType type = instrumentDescriptor.getType();
    this.isMonotonic = type == InstrumentType.COUNTER || type == InstrumentType.OBSERVABLE_SUM;
  }

  final boolean isMonotonic() {
    return isMonotonic;
  }
}
