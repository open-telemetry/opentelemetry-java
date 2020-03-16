/*
 * Copyright 2020, OpenTelemetry Authors
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

import io.opentelemetry.metrics.InstrumentWithBinding.BoundInstrument;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Base interface for all metrics with bounds defined in this package.
 *
 * @param <B> the specific type of Bound Instrument this instrument can provide.
 * @since 0.3.0
 */
@ThreadSafe
public interface InstrumentWithBinding<B extends BoundInstrument> extends Instrument {
  /**
   * Returns a {@code Bound Instrument} associated with the specified labels. Multiples requests
   * with the same set of labels may return the same {@code Bound Instrument} instance.
   *
   * <p>It is recommended that callers keep a reference to the Bound Instrument instead of always
   * calling this method for every operation.
   *
   * @param labelKeyValuePairs the set of labels, as key-value pairs.
   * @return a {@code Bound Instrument}
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.1.0
   */
  B bind(String... labelKeyValuePairs);

  interface BoundInstrument {
    /**
     * Unbinds the current {@code Bound} from the Instrument.
     *
     * <p>After this method returns the current instance {@code Bound} is considered invalid (not
     * being managed by the instrument).
     *
     * @since 0.3.0
     */
    void unbind();
  }
}
