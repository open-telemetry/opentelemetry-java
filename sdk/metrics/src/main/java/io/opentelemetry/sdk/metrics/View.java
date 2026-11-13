/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import java.util.Objects;
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
      int cardinalityLimit,
      @Nullable Aggregation explicitAggregation,
      @Nullable Predicate<String> explicitAttributeFilter,
      @Nullable Integer explicitCardinalityLimit) {
    return new AutoValue_View(
        name,
        description,
        aggregation,
        attributeFilter,
        cardinalityLimit,
        explicitAggregation,
        explicitAttributeFilter,
        explicitCardinalityLimit);
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
   *
   * @since 1.67.0
   */
  public abstract Predicate<String> getAttributeFilter();

  /**
   * Returns the cardinality limit for this view.
   *
   * @since 1.44.0
   */
  public abstract int getCardinalityLimit();

  @Nullable
  abstract Aggregation getExplicitAggregation();

  @Nullable
  abstract Predicate<String> getExplicitAttributeFilter();

  @Nullable
  abstract Integer getExplicitCardinalityLimit();

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

  @Override
  public final boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof View) {
      View that = (View) o;
      return Objects.equals(this.getName(), that.getName())
          && Objects.equals(this.getDescription(), that.getDescription())
          && this.getAggregation().equals(that.getAggregation())
          && this.getAttributeFilter().equals(that.getAttributeFilter())
          && this.getCardinalityLimit() == that.getCardinalityLimit();
    }
    return false;
  }

  @Override
  public final int hashCode() {
    int hash = 1;
    hash *= 1000003;
    hash ^= (getName() == null) ? 0 : getName().hashCode();
    hash *= 1000003;
    hash ^= (getDescription() == null) ? 0 : getDescription().hashCode();
    hash *= 1000003;
    hash ^= getAggregation().hashCode();
    hash *= 1000003;
    hash ^= getAttributeFilter().hashCode();
    hash *= 1000003;
    hash ^= getCardinalityLimit();
    return hash;
  }
}
