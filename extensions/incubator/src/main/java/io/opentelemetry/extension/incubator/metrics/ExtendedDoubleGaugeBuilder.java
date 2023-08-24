/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import java.util.function.Consumer;

/** Extended {@link DoubleGaugeBuilder} with experimental APIs. */
public interface ExtendedDoubleGaugeBuilder extends DoubleGaugeBuilder {

  /** Specify advice for gauge implementations. */
  default DoubleGaugeBuilder setAdvice(Consumer<DoubleGaugeAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
