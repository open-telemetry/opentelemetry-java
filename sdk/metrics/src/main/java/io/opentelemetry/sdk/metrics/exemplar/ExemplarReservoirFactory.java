/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.sdk.metrics.view.Aggregation;

/** A factory for {@link ExemplarReservoir}s that can leverage Aggregation configuration. */
public interface ExemplarReservoirFactory {
  /**
   * Constructs a new sampling reservoir for a given metric stream.
   *
   * @param aggregation The aggregation configuration.
   */
  ExemplarReservoir createReservoir(Aggregation aggregation);
}
