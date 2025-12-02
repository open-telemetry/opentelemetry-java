/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * A histogram metric point.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/data-model.md#histogram
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class ImmutableHistogramData implements HistogramData {

  private static final ImmutableHistogramData EMPTY =
      ImmutableHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());

  public static ImmutableHistogramData empty() {
    return EMPTY;
  }

  ImmutableHistogramData() {}

  public static ImmutableHistogramData create(
      AggregationTemporality temporality, Collection<HistogramPointData> points) {
    return new AutoValue_ImmutableHistogramData(temporality, points);
  }
}
