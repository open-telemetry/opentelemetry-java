/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;

abstract class AbstractSumAggregator<T> extends AbstractAggregator<T> {
  private final boolean isMonotonic;
  private final AggregationTemporality temporality;

  AbstractSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      boolean stateful) {
    super(resource, instrumentationLibraryInfo, instrumentDescriptor, stateful);
    this.isMonotonic =
        instrumentDescriptor.getType() == InstrumentType.COUNTER
            || instrumentDescriptor.getType() == InstrumentType.SUM_OBSERVER;
    AggregationTemporality temp =
        isStateful() ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA;
    if (instrumentDescriptor.getType() == InstrumentType.SUM_OBSERVER
        || instrumentDescriptor.getType() == InstrumentType.UP_DOWN_SUM_OBSERVER) {
      temp = AggregationTemporality.CUMULATIVE;
    }
    this.temporality = temp;
  }

  final boolean isMonotonic() {
    return isMonotonic;
  }

  final AggregationTemporality temporality() {
    return temporality;
  }
}
