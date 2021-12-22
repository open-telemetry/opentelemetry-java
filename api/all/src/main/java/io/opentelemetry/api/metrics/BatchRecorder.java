/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

public interface BatchRecorder {

  /** Record the value to the instruments when record is called. */
  default BatchRecorder addMeasurements(long value, LongInstrument... instruments) {
    return this;
  }

  /** Record the value to the instruments when record is called. */
  default BatchRecorder addMeasurements(double value, DoubleInstrument... instruments) {
    return this;
  }

  /** Record the measurements. */
  default void record(Attributes attributes) {
    record(attributes, Context.current());
  }

  /** Record the measurements. */
  default void record(Attributes attributes, Context context) {}
}
