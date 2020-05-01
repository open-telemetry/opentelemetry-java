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

import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Base interface for all metrics defined in this package.
 *
 * @since 0.1.0
 */
@ThreadSafe
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface Instrument {
  /** The {@code Builder} class for the {@code Instrument}. */
  interface Builder {
    /**
     * Sets the description of the {@code Instrument}.
     *
     * <p>Default value is {@code ""}.
     *
     * @param description the description of the Instrument.
     * @return this.
     */
    Builder setDescription(String description);

    /**
     * Sets the unit of the {@code Instrument}.
     *
     * <p>Default value is {@code "1"}.
     *
     * @param unit the unit of the Instrument.
     * @return this.
     */
    Builder setUnit(String unit);

    /**
     * Sets the map of constant labels (they will be added to all the Bound Instruments) for the
     * Instrument.
     *
     * <p>Default value is {@link Collections#emptyMap()}.
     *
     * @param constantLabels the map of constant labels for the Instrument.
     * @return this.
     */
    Builder setConstantLabels(Map<String, String> constantLabels);

    /**
     * Builds and returns a {@code Instrument} with the desired options.
     *
     * @return a {@code Instrument} with the desired options.
     */
    Instrument build();
  }
}
