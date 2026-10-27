/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import java.util.List;

/** Extended {@link DoubleHistogramBuilder} with experimental APIs. */
public interface ExtendedDoubleHistogramBuilder extends DoubleHistogramBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this histogram.
   */
  default ExtendedDoubleHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }

  /**
   * Specify the opt-in advice, which indicates whether this histogram should be disabled by default
   * and require explicit opt-in via a view to produce metric data.
   */
  default ExtendedDoubleHistogramBuilder setOptInAdvice(boolean optIn) {
    return this;
  }
}
