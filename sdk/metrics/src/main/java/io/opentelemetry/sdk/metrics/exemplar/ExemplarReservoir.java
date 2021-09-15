/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import java.util.List;

/**
 * An interface for an exemplar reservoir of samples.
 *
 * <p>This represents a reservoir for a specific "point" of metric data.
 */
public interface ExemplarReservoir {

  /** An exemplar reservoir that stores no exemplars. */
  public static ExemplarReservoir noSamples() {
    return NoExemplarReservoir.INSTANCE;
  }

  /** Wraps a {@link ExemplarReservoir} with a measurement pre-filter. */
  public static ExemplarReservoir filtered(ExemplarFilter filter, ExemplarReservoir original) {
    return new FilteredExemplarReservoir(filter, original);
  }

  /** Offers a {@code long} measurement to be sampled. */
  void offerMeasurement(long value, Attributes attributes, Context context);

  /** Offers a {@code double} measurement to be sampled. */
  void offerMeasurement(double value, Attributes attributes, Context context);

  /**
   * Builds (an immutable) list of Exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     Exemplar}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<Exemplar> collectAndReset(Attributes pointAttributes);
}
