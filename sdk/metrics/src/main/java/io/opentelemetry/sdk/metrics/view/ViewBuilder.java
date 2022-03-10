/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import java.util.function.Predicate;

/** A builder for {@link View}. */
public interface ViewBuilder {

  /**
   * sets the name of the resulting metric.
   *
   * @param name metric name or {@code null} if the underlying instrument name should be used.
   * @return this Builder.
   */
  ViewBuilder setName(String name);

  /**
   * sets the name of the resulting metric.
   *
   * @param description metric description or {@code null} if the underlying instrument description
   *     should be used.
   * @return this Builder.
   */
  ViewBuilder setDescription(String description);

  /**
   * sets {@link Aggregation}.
   *
   * @param aggregation aggregation to use.
   * @return this Builder.
   */
  ViewBuilder setAggregation(Aggregation aggregation);

  /**
   * Sets a filter for attributes, where only attribute names that pass the supplied {@link
   * Predicate} will be included in the output.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param keyFilter filter for key names to include.
   * @return this Builder.
   */
  ViewBuilder setAttributeFilter(Predicate<String> keyFilter);

  /** Returns a {@link View} with the configuration of this builder. */
  View build();
}
