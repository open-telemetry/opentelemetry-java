/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;

final class DoubleUpDownSumObserverSdk extends AbstractAsynchronousInstrument {

  DoubleUpDownSumObserverSdk(
      InstrumentDescriptor descriptor, AsynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }
}
