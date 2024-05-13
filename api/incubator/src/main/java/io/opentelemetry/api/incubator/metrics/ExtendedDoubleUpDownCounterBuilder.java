/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import java.util.List;

/** Extended {@link DoubleUpDownCounterBuilder} with experimental APIs. */
public interface ExtendedDoubleUpDownCounterBuilder extends DoubleUpDownCounterBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this up down counter.
   */
  default ExtendedDoubleUpDownCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }
}
