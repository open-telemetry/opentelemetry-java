/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.Collections;
import java.util.List;

/**
 * An interface for an exemplar resorvoir of samples.
 *
 * <p>This represents a resorovoir for a specifc "point" of metric data.
 */
public interface ExemplarReservoir {

  /** Offers a measurement to be sampled. */
  public void offerMeasurementLong(long value, Attributes attributes, Context context);
  /** Offers a measurement to be sampled. */
  public void offerMeasurementDouble(double value, Attributes attributes, Context context);

  /**
   * Builds (an immutable) list of Exemplars for exporting from the current resorvoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     Exemplar}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point.
   */
  public List<Exemplar> collectAndReset(Attributes pointAttributes);

  /** An exemplar resorvoir that stores no exemplars. */
  public static final ExemplarReservoir EMPTY =
      new ExemplarReservoir() {
        @Override
        public void offerMeasurementLong(long value, Attributes attributes, Context context) {
          // Store nothing.
        }

        @Override
        public void offerMeasurementDouble(double value, Attributes attributes, Context context) {
          // Store nothing.
        }

        @Override
        public List<Exemplar> collectAndReset(Attributes pointAttributes) {
          return Collections.emptyList();
        }
      };
}
