/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Builder of metric {@link View}s. */
public final class ViewBuilder {
  private String name = null;
  private String description = null;
  private Aggregation aggregation = Aggregation.defaultAggregation();
  private AttributesProcessor processor = AttributesProcessor.noop();

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
   * Specify the attributes processor for this view.
   *
   * <p>Note: This resets all attribute filters, baggage appending and other processing.
   *
   * <p>Visible for testing.
   *
   * @param processor The pre-processor for measurement attributes.
   * @return this Builder.
   */
  public ViewBuilder setAttributesProcessor(AttributesProcessor processor) {
    this.processor = processor;
    return this;
  }

  /**
   * Filters measurement attributes using a given filter.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyFilter filter for key names to include.
   * @return this Builder.
   */
  public ViewBuilder filterAttributes(Predicate<String> keyFilter) {
    this.processor = this.processor.then(AttributesProcessor.filterByKeyName(keyFilter));
    return this;
  }

  /**
   * Filters measurement attributes using a given regex.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyPattern the regular expression for selecting attributes by key name.
   * @return this Builder.
   */
  public ViewBuilder filterAttributes(Pattern keyPattern) {
    this.processor =
        this.processor.then(
            AttributesProcessor.filterByKeyName(StringPredicates.regex(keyPattern)));
    return this;
  }

  /**
   * Appends a static set of attributes to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param extraAttributes The static attributes to append to measurements.
   * @return this Builder.
   */
  public ViewBuilder appendAttributes(Attributes extraAttributes) {
    this.processor = this.processor.then(AttributesProcessor.append(extraAttributes));
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
   * Appends key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyPattern Only baggage key values pairs where the key matches this regex will be
   *     appended.
   * @return this Builder.
   */
  public ViewBuilder appendFilteredBaggageAttributesByPattern(Pattern keyPattern) {
    this.processor =
        this.processor.then(
            AttributesProcessor.appendBaggageByKeyName(StringPredicates.regex(keyPattern)));
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

  /** Returns the resulting {@link View}. */
  public View build() {
    return View.create(this.name, this.description, this.aggregation, this.processor);
  }
}
