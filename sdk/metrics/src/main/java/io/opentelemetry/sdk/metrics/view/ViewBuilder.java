/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import java.util.Objects;

public final class ViewBuilder {
  private AggregatorFactory aggregatorFactory;

  ViewBuilder() {}

  public ViewBuilder setAggregatorFactory(AggregatorFactory aggregatorFactory) {
    this.aggregatorFactory = Objects.requireNonNull(aggregatorFactory, "aggregatorFactory");
    return this;
  }

  public View build() {
    return View.create(this.aggregatorFactory);
  }
}
