/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.view.AttributesProcessor;
import java.util.List;
import java.util.function.Consumer;

/** A storage location for metric data collected by a specific instrument instance. */
public interface InstrumentStorage {
  /** Collects the metrics from this storage and resets for the next collection period. */
  List<MetricData> collectAndReset(long epochNanos);

  /**
   * Construct storage for a synchronous insturment.
   *
   * <p>This guarantees a high-concurrency friendly implementation.
   */
  public static <T> WriteableInstrumentStorage createSynchronous(
      Aggregator<T> aggregator, AttributesProcessor processor) {
    return SynchronousInstrumentStorage.create(aggregator, processor);
  }

  /**
   * Construct a "storage" for aysnchronous instrument.
   *
   * <p>This storage will poll for data when asked.
   */
  public static <T> InstrumentStorage createAsynchronous(
      Consumer<? extends ObservableMeasurement> callback,
      Aggregator<T> aggregator,
      AttributesProcessor processor) {
    return AsynchronousInstrumentStorage.create(callback, aggregator, processor);
  }
}
