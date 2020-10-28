/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** Base interface for all metrics defined in this package. */
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
     * Builds and returns a {@code Instrument} with the desired options.
     *
     * @return a {@code Instrument} with the desired options.
     */
    Instrument build();
  }
}
