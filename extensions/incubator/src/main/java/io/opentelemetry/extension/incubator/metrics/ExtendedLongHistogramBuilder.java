/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.LongHistogramBuilder;
import java.util.function.Consumer;

/** Extended {@link LongHistogramBuilder} with experimental APIs. */
public interface ExtendedLongHistogramBuilder extends LongHistogramBuilder {

  /** Specify advice for histogram implementations. */
  default LongHistogramBuilder setAdvice(Consumer<LongHistogramAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
