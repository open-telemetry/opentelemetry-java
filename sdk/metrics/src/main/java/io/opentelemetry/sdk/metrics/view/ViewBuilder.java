/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;

public final class ViewBuilder {
  private AggregatorFactory aggregatorFactory;
  private LabelsProcessorFactory labelsProcessorFactory = LabelsProcessorFactory.noop();

  ViewBuilder() {}

  /**
   * sets {@link AggregatorFactory}.
   *
   * @param aggregatorFactory aggregator factory.
   * @return this Builder.
   */
  public ViewBuilder setAggregatorFactory(AggregatorFactory aggregatorFactory) {
    this.aggregatorFactory = aggregatorFactory;
    return this;
  }

  /**
   * sets {@link LabelsProcessorFactory}.
   *
   * @param labelsProcessorFactory labels processor factory.
   * @return this Builder.
   */
  public ViewBuilder setLabelsProcessorFactory(LabelsProcessorFactory labelsProcessorFactory) {
    this.labelsProcessorFactory = labelsProcessorFactory;
    return this;
  }

  public View build() {
    return View.create(this.aggregatorFactory, this.labelsProcessorFactory);
  }
}
