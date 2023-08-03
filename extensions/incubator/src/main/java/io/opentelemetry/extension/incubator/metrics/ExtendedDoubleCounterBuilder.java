/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import java.util.function.Consumer;

/** Extended {@link DoubleCounterBuilder} with experimental APIs. */
public interface ExtendedDoubleCounterBuilder extends DoubleCounterBuilder {

  /** Specify advice for counter implementations. */
  default DoubleCounterBuilder setAdvice(Consumer<CounterAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
