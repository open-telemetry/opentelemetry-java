/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

/** Configure advice for implementation of {@code LongGauge}. */
public interface LongGaugeAdviceConfigurer {

  /** Specify the recommended set of attribute keys to be used for this gauge. */
  LongGaugeAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
