/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import java.util.List;

/** Extended {@link DoubleGaugeBuilder} with experimental APIs. */
public interface ExtendedDoubleGaugeBuilder extends DoubleGaugeBuilder {

  /**
   * Specify the attribute advice, which suggests the recommended set of attribute keys to be used
   * for this gauge.
   */
  default ExtendedDoubleGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
    return this;
  }

  /**
   * Specify the opt-in advice, which indicates whether this gauge should be disabled by default and
   * require explicit opt-in via a view to produce metric data.
   */
  default ExtendedDoubleGaugeBuilder setOptInAdvice(boolean optIn) {
    return this;
  }
}
