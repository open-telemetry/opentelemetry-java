/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import java.util.List;

/** Extended {@link LongCounterBuilder} with experimental APIs. */
public interface ExtendedLongCounterBuilder extends LongCounterBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this counter.
   */
  default ExtendedLongCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
