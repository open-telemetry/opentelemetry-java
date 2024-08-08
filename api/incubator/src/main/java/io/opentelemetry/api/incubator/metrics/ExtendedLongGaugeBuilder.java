/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import java.util.List;

/** Extended {@link LongGaugeBuilder} with experimental APIs. */
public interface ExtendedLongGaugeBuilder extends LongGaugeBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this gauge.
   */
  default ExtendedLongGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
