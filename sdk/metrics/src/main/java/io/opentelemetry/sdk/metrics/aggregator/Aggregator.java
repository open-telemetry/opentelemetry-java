/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import java.util.List;

/**
 * A processor that can generate aggregators for metrics streams while also combining those streams
 * into {@link MetricData}
 *
 * <p>Aggregators have the following lifecycle:
 *
 * <ul>
 *   <li>During usage of synchronous instruments, {@link #createStreamStorage} is called for unique
 *       {@link Attributes}. This method does not need to be threadsafe, but the returned handle
 *       must support high concurrency aggregation.
 *   <li>During a colletion phase, the aggregator will receive the following calls:
 *       <ol>
 *         <li>{@link #batchRecord} (for asynchronous instruments) or {@link
 *             #batchStreamAccumulation} for synchronous instruments. These can be called multiple
 *             times.
 *         <li>{@link #completeCollectionCycle} - This is expected to return {@link MetricData} for
 *             all metrics created by this aggregator. Additionally, the aggregator is expected to
 *             reset its state for the next round of collection.
 *       </ol>
 *       The collection phase is considered "single threaded", and does not require (additonal)
 *       locking.
 * </ul>
 */
public interface Aggregator<T> {
  /**
   * Construct a handle for storing highly-concurrent measurement input.
   *
   * <p>SynchronousHandle instances *must* be threadsafe and allow for high contention across
   * threads.
   */
  public SynchronousHandle<T> createStreamStorage();

  /**
   * Record an asynchronous measurement for this collection cycle.
   *
   * <p>Asynchronous measurements are sent within "a single thread" (or effectively single thread)
   * for this aggregator.
   */
  public void batchRecord(Measurement measurement);

  /**
   * Batches multiple entries together that are part of the same metric. It may remove attributes
   * from the {@link Attributes} and merge aggregations together.
   *
   * @param attributes the {@link Attributes} associated with this {@code MetricStreamProcessor}.
   * @param accumulation the accumulation of measurments produced by the StorageHandle.
   */
  public void batchStreamAccumulation(Attributes attributes, T accumulation);

  /**
   * Ends the current collection cycle and returns the list of metrics batched in this Batcher.
   *
   * <p>There may be more than one MetricData in case a multi aggregator is configured.
   *
   * <p>Based on the configured options this method may reset the internal state to produce deltas,
   * or keep the internal state to produce cumulative metrics.
   *
   * @return the list of metrics batched in this prcoessor.
   */
  public List<MetricData> completeCollectionCycle(long epochNanos);
}
