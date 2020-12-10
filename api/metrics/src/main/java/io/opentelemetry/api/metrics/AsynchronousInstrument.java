/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Labels;
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
  /** A {@code Callback} for a {@code AsynchronousInstrument}. */
  interface Callback<R extends Result> {
    void update(R result);
  }

  /** Builder class for {@link AsynchronousInstrument}. */
  interface Builder<R extends Result> extends Instrument.Builder {
    /**
     * Sets a callback that gets executed every collection interval.
     *
     * <p>Evaluation is deferred until needed, if this {@code AsynchronousInstrument} metric is not
     * exported then it will never be called.
     *
     * @param callback the callback to be executed before export.
     */
    Builder<R> setCallback(Callback<R> callback);

    @Override
    AsynchronousInstrument build();
  }

  interface Result {}

  /** The result for the {@link Callback}. */
  interface LongResult extends Result {
    void observe(long value, Labels labels);
  }

  /** The result for the {@link Callback}. */
  interface DoubleResult extends Result {
    void observe(double value, Labels labels);
  }
}
