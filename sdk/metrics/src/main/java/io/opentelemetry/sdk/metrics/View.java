/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** A configuration for a metric stream transformation. */
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
   * The name of the resulting metric to generate, or {@code null} if the same as the instrument.
   */
  @Nullable
  public abstract String getName();

  /**
   * The name of the resulting metric to generate, or {@code null} if the same as the instrument.
   */
  @Nullable
  public abstract String getDescription();

  /** The aggregation used for this view. */
  public abstract Aggregation getAggregation();

  /** The attribute processor used for this view. */
  abstract AttributesProcessor getAttributesProcessor();
}
