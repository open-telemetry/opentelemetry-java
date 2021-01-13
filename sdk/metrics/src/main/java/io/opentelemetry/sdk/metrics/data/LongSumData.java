/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.Collection;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class LongSumData implements SumData<LongPoint> {
  public static LongSumData create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<LongPoint> points) {
    return new AutoValue_LongSumData(points, isMonotonic, temporality);
  }

  LongSumData() {}
}
