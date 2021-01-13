/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/** Builder class for {@link AsynchronousInstrument}. */
public interface AsynchronousInstrumentBuilder<R> extends InstrumentBuilder {
  /**
   * Sets a consumer that gets executed every collection interval.
   *
   * <p>Evaluation is deferred until needed, if this {@code AsynchronousInstrument} metric is not
   * exported then it will never be called.
   *
   * @param updater the consumer to be executed before export.
   */
  AsynchronousInstrumentBuilder<R> setUpdater(Consumer<R> updater);

  @Override
  AsynchronousInstrument build();
}
