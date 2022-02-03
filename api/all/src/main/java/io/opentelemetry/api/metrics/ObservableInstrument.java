/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** An observable instrument. */
public interface ObservableInstrument {

  /**
   * Remove the callback associated with this instrument. After this is called, callbacks won't be
   * invoked on future collections. Subsequent calls to {@link #remove()} will have no effect.
   *
   * @see LongGaugeBuilder#buildWithCallback(Consumer)
   * @see DoubleGaugeBuilder#buildWithCallback(Consumer)
   * @see LongCounterBuilder#buildWithCallback(Consumer)
   * @see DoubleCounterBuilder#buildWithCallback(Consumer)
   * @see LongUpDownCounterBuilder#buildWithCallback(Consumer)
   * @see DoubleUpDownCounterBuilder#buildWithCallback(Consumer)
   */
  default void remove() {}
}
