/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

abstract class AbstractSynchronousInstrument<B extends AbstractBoundInstrument>
    extends AbstractInstrument {
  private final SynchronousInstrumentAccumulator<B> accumulator;

  AbstractSynchronousInstrument(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<B> accumulator) {
    super(descriptor);
    this.accumulator = accumulator;
  }

  @Override
  final List<MetricData> collectAll() {
    return accumulator.collectAll();
  }

  public B bind(Labels labels) {
    return accumulator.bind(labels);
  }
}
