/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.LongGaugeBuilder;
import java.util.function.Consumer;

/** Extended {@link LongGaugeBuilder} with experimental APIs. */
public interface ExtendedLongGaugeBuilder extends LongGaugeBuilder {

  /** Specify advice for gauge implementations. */
  default LongGaugeBuilder setAdvice(Consumer<LongGaugeAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
