/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;

final class LongUpDownSumObserverSdk extends AbstractAsynchronousInstrument {

  LongUpDownSumObserverSdk(
      InstrumentDescriptor descriptor, AsynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }
}
