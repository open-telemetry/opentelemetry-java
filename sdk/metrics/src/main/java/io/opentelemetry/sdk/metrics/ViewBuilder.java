/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** A builder for {@link View}. */
public final class ViewBuilder {

  @Nullable private String name;
  @Nullable private String description;
  private Aggregation aggregation = Aggregation.defaultAggregation();
  private AttributesProcessor processor = AttributesProcessor.noop();

  ViewBuilder() {}

  /**
   * Sets the name of the resulting metric.
   *
   * @param name metric name or {@code null} if the underlying instrument name should be used.
   * @return this Builder.
   */
  public ViewBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the description of the resulting metric.
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
   * Sets {@link Aggregation}.
   *
   * @param aggregation aggregation to use.
   * @return this Builder.
   */
  public ViewBuilder setAggregation(Aggregation aggregation) {
    if (!(aggregation instanceof AggregatorFactory)) {
      throw new IllegalArgumentException(
          "Custom Aggregation implementations are currently not supported. "
              + "Use one of the standard implementations returned by the static factories in the Aggregation class.");
    }
    this.aggregation = aggregation;
    return this;
  }

  /**
   * Sets a filter for attributes, where only attribute names that pass the supplied {@link
   * Predicate} will be included in the output.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyFilter filter for key names to include.
   * @return this Builder.
   */
  public ViewBuilder setAttributeFilter(Predicate<String> keyFilter) {
    Objects.requireNonNull(keyFilter, "keyFilter");
    return addAttributesProcessor(AttributesProcessor.filterByKeyName(keyFilter));
  }

  ViewBuilder addAttributesProcessor(AttributesProcessor attributesProcessor) {
    this.processor = this.processor.then(attributesProcessor);
    return this;
  }

  /** Returns a {@link View} with the configuration of this builder. */
  public View build() {
    return View.create(name, description, aggregation, processor);
  }
}
