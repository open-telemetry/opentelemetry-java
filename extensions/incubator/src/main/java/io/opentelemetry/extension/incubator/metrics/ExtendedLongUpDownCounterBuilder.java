/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import java.util.function.Consumer;

/** Extended {@link LongUpDownCounterBuilder} with experimental APIs. */
public interface ExtendedLongUpDownCounterBuilder extends LongUpDownCounterBuilder {

  /** Specify advice for up down counter implementations. */
  default LongUpDownCounterBuilder setAdvice(
      Consumer<LongUpDownCounterAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
