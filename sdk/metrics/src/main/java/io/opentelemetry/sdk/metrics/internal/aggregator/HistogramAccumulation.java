/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class HistogramAccumulation {
  /**
   * Creates a new {@link HistogramAccumulation} with the given values. Assume `counts` is read-only
   * so we don't need a defensive-copy here.
   *
   * @return a new {@link HistogramAccumulation} with the given values.
   */
  static HistogramAccumulation create(double sum, long[] counts) {
    return create(sum, counts, Collections.emptyList());
  }

  static HistogramAccumulation create(double sum, long[] counts, List<ExemplarData> exemplars) {
    return new AutoValue_HistogramAccumulation(sum, counts, exemplars);
  }

  HistogramAccumulation() {}

  /**
   * The sum of all measurements recorded.
   *
   * @return the sum of recorded measurements.
   */
  abstract double getSum();

  /**
   * The counts in each bucket. The returned type is a mutable object, but it should be fine because
   * the class is only used internally.
   *
   * @return the counts in each bucket. <b>do not mutate</b> the returned object.
   */
  @SuppressWarnings("mutable")
  abstract long[] getCounts();

  /** Exemplars accumulated during this period. */
  abstract List<ExemplarData> getExemplars();
}
