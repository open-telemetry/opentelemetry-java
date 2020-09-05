/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics;

import io.opentelemetry.common.Labels;
import java.util.Collections;
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
    Builder setConstantLabels(Labels constantLabels);

    /**
     * Builds and returns a {@code Instrument} with the desired options.
     *
     * @return a {@code Instrument} with the desired options.
     */
    Instrument build();
  }
}
