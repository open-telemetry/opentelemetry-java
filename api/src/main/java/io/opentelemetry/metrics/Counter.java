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

import io.opentelemetry.metrics.InstrumentWithBinding.BoundInstrument;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Base interface for all the Counter metrics.
 *
 * @param <H> the Bound Counter type.
 * @since 0.1.0
 */
@ThreadSafe
public interface Counter<H extends BoundInstrument> extends InstrumentWithBinding<H> {

  /** Builder class for {@link Counter}. */
  interface Builder extends Instrument.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Map<String, String> constantLabels);

    /**
     * Sets the monotonicity property for this {@code Counter}. If {@code true} only non-negative
     * values are expected.
     *
     * <p>Default value is {@code true}
     *
     * @param monotonic {@code true} only positive values are expected.
     * @return this.
     */
    Builder setMonotonic(boolean monotonic);

    @Override
    Counter<?> build();
  }
}
