/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/** A point that includes (optional) exemplars which lead to its creation. */
@Immutable
public interface SampledPointData extends PointData {
  /**
   * (Optional) List of exemplars collected from measurements that were used to form the data point.
   */
  Collection<Exemplar> getExemplars();
}
