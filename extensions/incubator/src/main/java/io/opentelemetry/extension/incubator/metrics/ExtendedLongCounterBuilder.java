/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.LongCounterBuilder;
import java.util.function.Consumer;

/** Extended {@link LongCounterBuilder} with experimental APIs. */
public interface ExtendedLongCounterBuilder extends LongCounterBuilder {

  /** Specify advice for counter implementations. */
  default LongCounterBuilder setAdvice(Consumer<LongCounterAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
