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
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#histogram
 *
 * <p><i>Note: This is called "DoubleHistogram" to reflect which primitives are used to record it,
 * however "Histogram" is the equivalent OTLP type.</i>
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
