/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.metrics.ObservableMeasurement;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import java.util.List;
import java.util.function.Consumer;

/** A storage location for metric data collected by a specific instrument instance. */
public interface InstrumentStorage {
  /** Returns the descriptor for this instrument. */
  public InstrumentDescriptor getDescriptor();

  /** Collects the metrics from this storage and resets for the next collection period. */
  List<MetricData> collectAndReset(long epochNanos);

  /**
   * Construct storage for a synchronous insturment.
   *
   * <p>This guarantees a high-concurrnecy friendly implementation.
   */
  public static <T> WriteableInstrumentStorage createSynchronous(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregator<T> aggregator) {
    return SynchronousInstrumentStorage.create(
        descriptor, meterProviderSharedState, meterSharedState, aggregator);
  }

  public static <T> InstrumentStorage createAsynchronous(
      InstrumentDescriptor descriptor,
      Consumer<? extends ObservableMeasurement> callback,
      Aggregator<T> aggregator) {
    return AsynchronousInstrumentStorage.create(descriptor, callback, aggregator);
  }
}
