/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;

public interface ExemplarReservoir<T extends ExemplarData> {

  /**
   * Returns an immutable list of Exemplars for exporting from the current reservoir.
   *
   * <p>Additionally, clears the reservoir for the next sampling period.
   *
   * @param pointAttributes the {@link Attributes} associated with the metric point. {@link
   *     ExemplarData}s should filter these out of their final data state.
   * @return An (immutable) list of sampled exemplars for this point. Implementers are expected to
   *     filter out {@code pointAttributes} from the original recorded attributes.
   */
  List<T> collectAndReset(Attributes pointAttributes);
}
