/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/**
 * A reference to an observable instrument registered with {@link
 * DoubleCounterBuilder#buildWithCallback(Consumer)}.
 *
 * @since 1.10.0
 */
public interface ObservableDoubleCounter extends AutoCloseable {

  /**
   * Remove the callback registered via {@link DoubleCounterBuilder#buildWithCallback(Consumer)}.
   * After this is called, the callback won't be invoked on future collections. Subsequent calls to
   * {@link #close()} have no effect.
   *
   * <p>Note: other callbacks registered to the instrument with the same identity are unaffected.
   *
   * @since 1.12.0
   */
  @Override
  default void close() {}
}
