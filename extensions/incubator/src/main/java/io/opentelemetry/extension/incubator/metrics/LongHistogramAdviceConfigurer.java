/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongHistogram;
import java.util.List;

/** Configure advice for implementations of {@link LongHistogram}. */
public interface LongHistogramAdviceConfigurer {

  /** Specify recommended set of explicit bucket boundaries for this histogram. */
  LongHistogramAdviceConfigurer setExplicitBucketBoundaries(List<Long> bucketBoundaries);
}
