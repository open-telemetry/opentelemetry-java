/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import java.util.StringJoiner;
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
      AttributesProcessor attributesProcessor) {
    return new AutoValue_View(name, description, aggregation, attributesProcessor);
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

  /** Returns the attribute processor used for this view. */
  abstract AttributesProcessor getAttributesProcessor();

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
    joiner.add("attributesProcessor=" + getAttributesProcessor());
    return joiner.toString();
  }
}
