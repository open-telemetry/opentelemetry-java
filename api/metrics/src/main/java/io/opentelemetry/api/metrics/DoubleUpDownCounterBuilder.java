/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link DoubleUpDownCounter}. */
public interface DoubleUpDownCounterBuilder extends SynchronousInstrumentBuilder {
  @Override
  DoubleUpDownCounterBuilder setDescription(String description);

  @Override
  DoubleUpDownCounterBuilder setUnit(String unit);

  @Override
  DoubleUpDownCounter build();
}
