/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import java.util.function.Consumer;

/** Extended {@link DoubleUpDownCounterBuilder} with experimental APIs. */
public interface ExtendedDoubleUpDownCounterBuilder extends DoubleUpDownCounterBuilder {

  /** Specify advice for up down counter implementations. */
  default DoubleUpDownCounterBuilder setAdvice(
      Consumer<DoubleUpDownCounterAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
