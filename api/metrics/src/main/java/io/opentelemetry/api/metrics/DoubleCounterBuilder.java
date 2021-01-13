/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link DoubleCounter}. */
public interface DoubleCounterBuilder extends SynchronousInstrumentBuilder {
  @Override
  DoubleCounterBuilder setDescription(String description);

  @Override
  DoubleCounterBuilder setUnit(String unit);

  @Override
  DoubleCounter build();
}
