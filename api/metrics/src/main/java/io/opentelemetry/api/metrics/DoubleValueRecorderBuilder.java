/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link DoubleValueRecorder}. */
public interface DoubleValueRecorderBuilder extends SynchronousInstrumentBuilder {
  @Override
  DoubleValueRecorderBuilder setDescription(String description);

  @Override
  DoubleValueRecorderBuilder setUnit(String unit);

  @Override
  DoubleValueRecorder build();
}
