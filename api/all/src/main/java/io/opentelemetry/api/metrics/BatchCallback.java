/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/**
 * A reference to a batch callback registered via {@link Meter#batchCallback(Runnable,
 * ObservableMeasurement, ObservableMeasurement...)}.
 *
 * @since 1.15.0
 */
public interface BatchCallback extends AutoCloseable {

  /**
   * Remove the callback registered via {@link Meter#batchCallback(Runnable, ObservableMeasurement,
   * ObservableMeasurement...)}. After this is called, the callback won't be invoked on future
   * collections. Subsequent calls to {@link #close()} have no effect.
   */
  @Override
  default void close() {}
}
