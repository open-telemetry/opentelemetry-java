/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * A builder for {@link View}.
 *
 * @since 1.14.0
 */
public final class ViewBuilder {

  @Nullable private String name;
  @Nullable private String description;
  private Aggregation aggregation = Aggregation.defaultAggregation();
  private AttributesProcessor processor = AttributesProcessor.noop();

  ViewBuilder() {}

  /**
   * Sets the name of the resulting metric.
   *
   * @param name metric name or {@code null} if the matched instrument name should be used.
   */
  public ViewBuilder setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the description of the resulting metric.
   *
   * @param description metric description or {@code null} if the matched instrument description
   *     should be used.
   */
  public ViewBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets {@link Aggregation}.
   *
   * @param aggregation aggregation to use.
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
   * Sets a filter for attributes keys.
   *
   * <p>Only attribute keys that pass the supplied {@link Predicate} will be included in the output.
   *
   * @param keyFilter filter for attribute keys to include.
   */
  public ViewBuilder setAttributeFilter(Predicate<String> keyFilter) {
    Objects.requireNonNull(keyFilter, "keyFilter");
    return addAttributesProcessor(AttributesProcessor.filterByKeyName(keyFilter));
  }

  /**
   * Add an attribute processor.
   *
   * <p>Note: not currently stable but additional attribute processors can be configured via {@link
   * SdkMeterProviderUtil#appendAllBaggageAttributes(ViewBuilder)}.
   */
  ViewBuilder addAttributesProcessor(AttributesProcessor attributesProcessor) {
    this.processor = this.processor.then(attributesProcessor);
    return this;
  }

  /** Returns a {@link View} with the configuration of this builder. */
  public View build() {
    return View.create(name, description, aggregation, processor);
  }
}
