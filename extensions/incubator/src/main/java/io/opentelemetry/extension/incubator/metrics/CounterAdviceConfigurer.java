/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleCounter;
import java.util.List;

/** Configure advice for implementation of {@link DoubleCounter}. */
public interface CounterAdviceConfigurer {

  /** Specify the recommended set of attribute keys to be used for this counter. */
  CounterAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
