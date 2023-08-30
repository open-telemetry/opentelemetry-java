/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleHistogram;
import java.util.List;

/** Configure advice for implementations of {@link DoubleHistogram}. */
public interface DoubleHistogramAdviceConfigurer {

  /** Specify recommended set of explicit bucket boundaries for this histogram. */
  DoubleHistogramAdviceConfigurer setExplicitBucketBoundaries(List<Double> bucketBoundaries);

  /** Specify the recommended set of attribute keys to be used for this histogram. */
  DoubleHistogramAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
