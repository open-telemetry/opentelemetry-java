/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

/** {@link SumData} recorded uses {@code long}s. */
@Immutable
@AutoValue
public abstract class LongSumData implements SumData<LongPointData> {
  public static LongSumData create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<LongPointData> points) {
    return new AutoValue_LongSumData(points, isMonotonic, temporality);
  }

  LongSumData() {}
}
