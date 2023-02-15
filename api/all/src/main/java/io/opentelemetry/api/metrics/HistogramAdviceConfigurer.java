/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.List;

/** Configure advice for implementations of {@link LongHistogram} and {@link DoubleHistogram}. */
public interface HistogramAdviceConfigurer {

  /** Specify recommended set of explicit bucket boundaries for this histogram. */
  HistogramAdviceConfigurer setExplicitBucketBoundaries(List<Double> bucketBoundaries);
}
