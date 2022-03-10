/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.metrics.view.ViewBuilder;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * Builder of metric {@link View}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ViewBuilderImpl implements ViewBuilder {
  @Nullable private String name = null;
  @Nullable private String description = null;
  private Aggregation aggregation = Aggregation.defaultAggregation();
  private AttributesProcessor processor = AttributesProcessor.noop();

  public ViewBuilderImpl() {}

  @Override
  public ViewBuilder setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public ViewBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public ViewBuilder setAggregation(Aggregation aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  @Override
  public ViewBuilder setAttributeFilter(Predicate<String> keyFilter) {
    this.processor = this.processor.then(AttributesProcessor.filterByKeyName(keyFilter));
    return this;
  }

  /**
   * Appends key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyFilter Only baggage key values pairs where the key matches this predicate will be
   *     appended.
   * @return this Builder.
   */
  public ViewBuilder appendFilteredBaggageAttributes(Predicate<String> keyFilter) {
    this.processor = this.processor.then(AttributesProcessor.appendBaggageByKeyName(keyFilter));
    return this;
  }

  /**
   * Appends all key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @return this Builder.
   */
  public ViewBuilder appendAllBaggageAttributes() {
    return appendFilteredBaggageAttributes(StringPredicates.ALL);
  }

  @Override
  public View build() {
    return ImmutableView.create(this.name, this.description, this.aggregation, this.processor);
  }
}
