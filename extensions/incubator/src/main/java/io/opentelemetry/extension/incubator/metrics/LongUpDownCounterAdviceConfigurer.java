/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import java.util.List;

/** Configure advice for implementation of {@link LongUpDownCounter}. */
public interface LongUpDownCounterAdviceConfigurer {

  /** Specify the recommended set of attribute keys to be used for this up down counter. */
  LongUpDownCounterAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
