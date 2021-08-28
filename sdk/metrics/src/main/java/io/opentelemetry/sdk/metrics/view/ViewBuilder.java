/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;

/** Builder of metric {@link View}s. */
public final class ViewBuilder {
  private String name = null;
  private String description = null;
  private Aggregation aggregation = Aggregation.defaultAggregation();
  private AttributesProcessor attributesProcessor = AttributesProcessor.noop();

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
   * sets {@link Aggregation}.
   *
   * @param aggregation aggregation to use.
   * @return this Builder.
   */
  public ViewBuilder setAggregation(Aggregation aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  /**
   * sets {@link AttributesProcessor}.
   *
   * @param attributesProcessor attributes processor.
   * @return this Builder.
   */
  public ViewBuilder setAttribtuesProcessor(AttributesProcessor attributesProcessor) {
    this.attributesProcessor = attributesProcessor;
    return this;
  }

  /** Returns the resulting {@link View}. */
  public View build() {
    return View.create(this.name, this.description, this.aggregation, this.attributesProcessor);
  }
}
