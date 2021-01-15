/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link LongUpDownCounter}. */
public interface LongUpDownCounterBuilder extends SynchronousInstrumentBuilder {
  @Override
  LongUpDownCounterBuilder setDescription(String description);

  @Override
  LongUpDownCounterBuilder setUnit(String unit);

  @Override
  LongUpDownCounter build();
}
