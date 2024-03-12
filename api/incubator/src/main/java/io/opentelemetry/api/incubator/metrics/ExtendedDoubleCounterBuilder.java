/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import java.util.List;

/** Extended {@link DoubleCounterBuilder} with experimental APIs. */
public interface ExtendedDoubleCounterBuilder extends DoubleCounterBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this counter.
   */
  default ExtendedDoubleCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
