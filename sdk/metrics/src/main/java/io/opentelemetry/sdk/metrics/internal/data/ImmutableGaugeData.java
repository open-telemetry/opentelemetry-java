/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * {@link GaugeData} recorded uses {@code double}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableGaugeData<T extends PointData> implements GaugeData<T> {
  private static final ImmutableGaugeData<DoublePointData> EMPTY =
      ImmutableGaugeData.create(Collections.emptyList());

  // Type doesn't matter for an empty list.
  @SuppressWarnings("unchecked")
  public static <T extends PointData> ImmutableGaugeData<T> empty() {
    return (ImmutableGaugeData<T>) EMPTY;
  }

  public static <T extends PointData> ImmutableGaugeData<T> create(Collection<T> points) {
    return new AutoValue_ImmutableGaugeData<>(points);
  }

  ImmutableGaugeData() {}

  @Override
  public abstract Collection<T> getPoints();
}
