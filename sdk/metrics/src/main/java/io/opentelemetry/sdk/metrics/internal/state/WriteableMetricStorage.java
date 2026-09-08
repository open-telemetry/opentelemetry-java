/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.MetricData;

/**
 * Stores {@link MetricData} and allows synchronous writes of measurements.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface WriteableMetricStorage {

  /** Records a measurement. */
  void recordLong(long value, Attributes attributes, Context context);

  /** Records a measurement. */
  void recordDouble(double value, Attributes attributes, Context context);

  /**
   * Binds the given {@code attributes}, returning a {@link BoundStorageHandle} that records
   * directly to the corresponding timeseries.
   *
   * <p>When the view's {@link
   * io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor#usesContext()} is false, the
   * series is resolved once here, and subsequent records via the returned handle skip per-recording
   * attribute processing and series lookup. Otherwise (e.g. a baggage-derived view), the series
   * cannot be fixed at bind time: the returned handle re-runs attribute processing and series
   * resolution on every record call, using the {@link Context} supplied to that call, same as an
   * unbound {@link #recordLong}/{@link #recordDouble} call.
   */
  BoundStorageHandle bind(Attributes attributes);

  /**
   * Returns {@code true} if the storage is actively recording measurements, and {@code false}
   * otherwise (i.e. noop / empty metric storage is installed).
   */
  boolean isEnabled();
}
