/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import java.util.StringJoiner;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A view configures how measurements are aggregated and exported as metrics.
 *
 * <p>Views are registered with the SDK {@link
 * SdkMeterProviderBuilder#registerView(InstrumentSelector, View)}.
 *
 * @since 1.14.0
 */
@AutoValue
@Immutable
public abstract class View {

  /** Returns a {@linkplain ViewBuilder builder} for a {@link View}. */
  public static ViewBuilder builder() {
    return new ViewBuilder();
  }

  static View create(
      @Nullable String name,
      @Nullable String description,
      Aggregation aggregation,
      Predicate<String> attributeFilter,
      int cardinalityLimit) {
    return new AutoValue_View(name, description, aggregation, attributeFilter, cardinalityLimit);
  }

  View() {}

  /**
   * Returns the name of the resulting metric, or {@code null} if the matched instrument name should
   * be used.
   */
  @Nullable
  public abstract String getName();

  /**
   * Returns the description of the resulting metric, or {@code null} if the matched instrument
   * description should be used.
   */
  @Nullable
  public abstract String getDescription();

  /** Returns the aggregation of the resulting metric. */
  public abstract Aggregation getAggregation();

  /**
   * Returns the attribute key filter for this view. Attribute keys for which the predicate returns
   * {@code false} are dropped from recorded measurements.
   */
  public abstract Predicate<String> getAttributeFilter();

  /**
   * Returns the cardinality limit for this view.
   *
   * @since 1.44.0
   */
  public abstract int getCardinalityLimit();

  @Override
  public final String toString() {
    StringJoiner joiner = new StringJoiner(", ", "View{", "}");
    if (getName() != null) {
      joiner.add("name=" + getName());
    }
    if (getDescription() != null) {
      joiner.add("description=" + getDescription());
    }
    joiner.add("aggregation=" + getAggregation());
    joiner.add("attributeFilter=" + getAttributeFilter());
    joiner.add("cardinalityLimit=" + getCardinalityLimit());
    return joiner.toString();
  }
}
