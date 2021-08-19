/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;

/** Builder of metric {@link View}s. */
public final class ViewBuilder {
  private String name = null;
  private String description = null;
  private AggregatorFactory aggregatorFactory;
  private LabelsProcessorFactory labelsProcessorFactory = LabelsProcessorFactory.noop();

  ViewBuilder() {}

  /**
   * sets the name of the resulting metric.
   *
   * @param name metric name or {@code null} if the underlying instrument name should be used.
   * @return this Builder.
   */
  public ViewBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * sets the name of the resulting metric.
   *
   * @param description metric description or {@code null} if the underlying instrument description
   *     should be used.
   * @return this Builder.
   */
  public ViewBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

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

  /** Returns the resulting {@link View}. */
  public View build() {
    return View.create(
        this.name, this.description, this.aggregatorFactory, this.labelsProcessorFactory);
  }
}
