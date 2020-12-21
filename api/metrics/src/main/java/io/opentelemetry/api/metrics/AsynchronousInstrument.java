/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Labels;
import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code AsynchronousInstrument} is an interface that defines a type of instruments that are used
 * to report measurements asynchronously.
 *
 * <p>They are reported by a callback, once per collection interval, and lack Context. They are
 * permitted to report only one value per distinct label set per period. If the application observes
 * multiple values for the same label set, in a single callback, the last value is the only value
 * kept.
 */
@ThreadSafe
public interface AsynchronousInstrument extends Instrument {
  /** Builder class for {@link AsynchronousInstrument}. */
  interface Builder<R> extends Instrument.Builder {
    /**
     * Sets a consumer that gets executed every collection interval.
     *
     * <p>Evaluation is deferred until needed, if this {@code AsynchronousInstrument} metric is not
     * exported then it will never be called.
     *
     * @param updater the consumer to be executed before export.
     */
    Builder<R> setUpdater(Consumer<R> updater);

    @Override
    AsynchronousInstrument build();
  }

  /** The result pass to the updater. */
  interface LongResult {
    void observe(long value, Labels labels);
  }

  /** The result pass to the updater. */
  interface DoubleResult {
    void observe(double value, Labels labels);
  }
}
