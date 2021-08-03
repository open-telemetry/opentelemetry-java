/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/** Stores {@link MetricData} and allows synchronous writes of measurements. */
public interface WriteableMetricStorage extends MetricStorage {
  /** Bind an efficient storage handle for a set of attributes. */
  BoundStorageHandle bind(Attributes attributes);

  /** Records a measurement. */
  void recordLong(long value, Attributes attributes, Context context);
  /** Records a measurement. */
  void recordDouble(double value, Attributes attributes, Context context);
}
