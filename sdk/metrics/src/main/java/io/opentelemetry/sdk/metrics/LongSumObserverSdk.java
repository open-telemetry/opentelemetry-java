/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;

final class LongSumObserverSdk extends AbstractAsynchronousInstrument {

  LongSumObserverSdk(
      InstrumentDescriptor descriptor, AsynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }
}
