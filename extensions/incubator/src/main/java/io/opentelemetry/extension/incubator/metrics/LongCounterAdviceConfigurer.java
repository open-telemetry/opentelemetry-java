/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongCounter;
import java.util.List;

/** Configure advice for implementation of {@link LongCounter}. */
public interface LongCounterAdviceConfigurer {

  /** Specify the recommended set of attribute keys to be used for this counter. */
  LongCounterAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
