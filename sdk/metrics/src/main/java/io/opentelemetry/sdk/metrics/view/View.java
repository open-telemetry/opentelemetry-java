/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.internal.view.ViewBuilderImpl;
import javax.annotation.Nullable;

/** A configuration for a metric stream transformation. */
public interface View {
  /** Returns a {@linkplain ViewBuilder builder} for a {@link View}. */
  static ViewBuilder builder() {
    return new ViewBuilderImpl();
  }

  /**
   * The name of the resulting metric to generate, or {@code null} if the same as the instrument.
   */
  @Nullable
  String getName();

  /**
   * The name of the resulting metric to generate, or {@code null} if the same as the instrument.
   */
  @Nullable
  String getDescription();

  /** The aggregation used for this view. */
  Aggregation getAggregation();
}
