/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code AsynchronousInstrument} is an interface that defines a type of instruments that are used
 * to report measurements asynchronously.
 *
 * <p>They are reported by a callback, once per collection interval, and lack Context. They are
 * permitted to report only one value per distinct label set per period. If the application observes
 * multiple values for the same label set, in a single callback, the last value is the only value
 * kept.
 *
 * @param <R> the callback Result type.
 * @since 0.1.0
 */
@ThreadSafe
public interface AsynchronousInstrument<R> extends Instrument {
  /**
   * A {@code Callback} for a {@code AsynchronousInstrument}.
   *
   * @since 0.1.0
   */
  interface Callback<R> {
    void update(R result);
  }

  /**
   * Sets a callback that gets executed every collection interval.
   *
   * <p>Evaluation is deferred until needed, if this {@code AsynchronousInstrument} metric is not
   * exported then it will never be called.
   *
   * @param metricUpdater the callback to be executed before export.
   * @since 0.1.0
   */
  void setCallback(Callback<R> metricUpdater);

  /** Builder class for {@link AsynchronousInstrument}. */
  interface Builder extends Instrument.Builder {
    @Override
    AsynchronousInstrument<?> build();
  }
}
