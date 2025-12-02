/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

/**
 * An interface for an exemplar reservoir of double samples.
 *
 * <p>This represents a reservoir for a specific "point" of metric data.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see LongExemplarReservoir
 * @see ExemplarReservoirFactory
 */
public interface DoubleExemplarReservoir {

  /** Offers a measurement to be sampled. */
  void offerDoubleMeasurement(double value, Attributes attributes, Context context);

  /**
   * Returns an immutable list of exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     ExemplarData}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<DoubleExemplarData> collectAndResetDoubles(Attributes pointAttributes);
}
