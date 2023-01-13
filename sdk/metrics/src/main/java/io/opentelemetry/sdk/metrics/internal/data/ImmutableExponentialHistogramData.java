/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * Auto value implementation of {@link ExponentialHistogramData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableExponentialHistogramData implements ExponentialHistogramData {

  private static final ExponentialHistogramData EMPTY =
      create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

  public static ExponentialHistogramData empty() {
    return EMPTY;
  }

  /** Returns a new {@link ExponentialHistogramData}. */
  public static ExponentialHistogramData create(
      AggregationTemporality temporality, Collection<ExponentialHistogramPointData> points) {
    return new AutoValue_ImmutableExponentialHistogramData(temporality, points);
  }

  ImmutableExponentialHistogramData() {}
}
