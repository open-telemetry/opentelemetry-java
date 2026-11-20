/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import java.util.List;

/** Extended {@link LongUpDownCounterBuilder} with experimental APIs. */
public interface ExtendedLongUpDownCounterBuilder extends LongUpDownCounterBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this up down counter.
   */
  default ExtendedLongUpDownCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }

  /**
   * Specify the opt-in advice, which indicates whether this up down counter should be disabled by
   * default and require explicit opt-in via a view to produce metric data.
   */
  default ExtendedLongUpDownCounterBuilder setOptInAdvice(boolean optIn) {
    return this;
  }
}
