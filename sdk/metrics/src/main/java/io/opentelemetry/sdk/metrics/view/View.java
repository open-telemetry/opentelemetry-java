/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** TODO: javadoc. */
@AutoValue
@Immutable
public abstract class View {

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
  public abstract AggregatorConfig getAggregation();

  public abstract LabelsProcessorFactory getLabelsProcessorFactory();

  public static ViewBuilder builder() {
    return new ViewBuilder();
  }

  static View create(
      String name,
      String description,
      AggregatorConfig aggregation,
      LabelsProcessorFactory labelsProcessorFactory) {
    return new AutoValue_View(name, description, aggregation, labelsProcessorFactory);
  }
}
