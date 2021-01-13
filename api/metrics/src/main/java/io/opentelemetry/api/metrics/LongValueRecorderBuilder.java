/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link LongValueRecorder}. */
public interface LongValueRecorderBuilder extends SynchronousInstrumentBuilder {
  @Override
  LongValueRecorderBuilder setDescription(String description);

  @Override
  LongValueRecorderBuilder setUnit(String unit);

  @Override
  LongValueRecorder build();
}
