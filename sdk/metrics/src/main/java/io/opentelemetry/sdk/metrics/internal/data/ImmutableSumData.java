/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * {@link SumData} recorded uses {@code double}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableSumData<T extends PointData> implements SumData<T> {

  static final ImmutableSumData<DoublePointData> EMPTY =
      ImmutableSumData.create(
          /* isMonotonic= */ false, AggregationTemporality.CUMULATIVE, Collections.emptyList());

  // Type doesn't matter for an empty list.
  @SuppressWarnings("unchecked")
  public static <T extends PointData> ImmutableSumData<T> empty() {
    return (ImmutableSumData<T>) EMPTY;
  }

  public static <T extends PointData> ImmutableSumData<T> create(
      boolean isMonotonic, AggregationTemporality temporality, Collection<T> points) {
    return new AutoValue_ImmutableSumData<>(points, isMonotonic, temporality);
  }

  ImmutableSumData() {}
}
