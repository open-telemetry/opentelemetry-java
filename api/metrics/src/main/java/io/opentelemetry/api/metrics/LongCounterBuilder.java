/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link LongCounter}. */
public interface LongCounterBuilder extends SynchronousInstrumentBuilder {
  @Override
  LongCounterBuilder setDescription(String description);

  @Override
  LongCounterBuilder setUnit(String unit);

  @Override
  LongCounter build();
}
