/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

/** Configure advice for implementation of {@code DoubleGauge}. */
public interface DoubleGaugeAdviceConfigurer {

  /** Specify the recommended set of attribute keys to be used for this gauge. */
  DoubleGaugeAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes);
}
