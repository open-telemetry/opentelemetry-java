/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.metrics;

import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import java.util.function.Consumer;

/** Extended {@link DoubleHistogramBuilder} with experimental APIs. */
public interface ExtendedDoubleHistogramBuilder extends DoubleHistogramBuilder {

  /** Specify advice for histogram implementations. */
  default DoubleHistogramBuilder setAdvice(
      Consumer<DoubleHistogramAdviceConfigurer> adviceConsumer) {
    return this;
  }
}
