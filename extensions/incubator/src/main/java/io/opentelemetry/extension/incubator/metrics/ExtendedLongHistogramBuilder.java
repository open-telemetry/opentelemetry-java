/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import java.util.List;

/** Extended {@link LongHistogramBuilder} with experimental APIs. */
public interface ExtendedLongHistogramBuilder extends LongHistogramBuilder {

  /**
   * Specify the explicit bucket buckets boundaries advice, which suggests the recommended set of
   * explicit bucket boundaries for this histogram.
   */
  default ExtendedLongHistogramBuilder setExplicitBucketBoundariesAdvice(
      List<Long> bucketBoundaries) {
    return this;
  }

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this histogram.
   */
  default ExtendedLongHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
