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
 * Base interface for all measures defined in this package.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Measure {

  /** Builder class for the {@link Measure}. */
  interface Builder<B extends Builder<B, V>, V> {
    /**
     * Sets the detailed description of the measure, used in documentation.
     *
     * <p>Default description is {@code ""}.
     *
     * @param description the detailed description of the {@code Measure}.
     * @return this.
     * @since 0.1.0
     */
    B setDescription(String description);

    /**
     * Sets the units in which {@code Measure} values are measured.
     *
     * <p>The suggested grammar for a unit is as follows:
     *
     * <ul>
     *   <li>Expression = Component { "." Component } {"/" Component };
     *   <li>Component = [ PREFIX ] UNIT [ Annotation ] | Annotation | "1";
     *   <li>Annotation = "{" NAME "}" ;
     * </ul>
     *
     * <p>For example, string “MBy{transmitted}/ms” stands for megabytes per milliseconds, and the
     * annotation transmitted inside {} is just a comment of the unit.
     *
     * <p>Default unit is {@code "1"}.
     *
     * @param unit the units in which {@code Measure} values are measured.
     * @return this.
     * @since 0.1.0
     */
    B setUnit(String unit);

    /**
     * Sets the map of constant labels (they will be added to all the TimeSeries) for the Metric.
     *
     * <p>Default value is {@link Collections#emptyMap()}.
     *
     * @param constantLabels the map of constant labels for the Metric.
     * @return this.
     */
    B setConstantLabels(Map<String, String> constantLabels);

    /**
     * Builds and returns a {@code Measure} with the desired options.
     *
     * @return a {@code Measure} with the desired options.
     */
    V build();
  }
}
