/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/** {@link SumData} recorded uses {@code double}s. */
@Immutable
@AutoValue
public abstract class DoubleSumData implements SumData<DoublePointData> {

  static final DoubleSumData DEFAULT =
      DoubleSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());

  DoubleSumData() {}

  public static DoubleSumData create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<DoublePointData> points) {
    return new AutoValue_DoubleSumData(points, isMonotonic, temporality);
  }
}
