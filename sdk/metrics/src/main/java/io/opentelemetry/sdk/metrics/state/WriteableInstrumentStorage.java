/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/** Instrument storage that allows synchronous instrument writing. */
public interface WriteableInstrumentStorage extends InstrumentStorage {
  /** Bind an efficient storage handle for a set of attributes. */
  StorageHandle bind(Attributes attributes);

  /** Records a measurement. */
  void recordLong(long value, Attributes attributes, Context context);
  /** Records a measurement. */
  void recordDouble(double value, Attributes attributes, Context context);
}
