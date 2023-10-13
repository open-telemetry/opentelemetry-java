/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import java.util.List;

/** Extended {@link DoubleHistogramBuilder} with experimental APIs. */
public interface ExtendedDoubleHistogramBuilder extends DoubleHistogramBuilder {

  /**
   * Specify the explicit bucket buckets boundaries advice, which suggests the recommended set of
   * explicit bucket boundaries for this histogram.
   */
  default ExtendedDoubleHistogramBuilder setExplicitBucketBoundariesAdvice(
      List<Double> bucketBoundaries) {
    return this;
  }

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this histogram.
   */
  default ExtendedDoubleHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
