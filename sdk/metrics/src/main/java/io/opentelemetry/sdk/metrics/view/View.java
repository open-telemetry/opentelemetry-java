/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
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
  public abstract Aggregation getAggregation();

  /** Processor of attributes before performing aggregation. */
  public abstract AttributesProcessor getAttributesProcessor();

  /** Information about where the View was defined. */
  public abstract SourceInfo getSourceInfo();

  public static ViewBuilder builder() {
    return new ViewBuilder();
  }

  static View create(
      @Nullable String name,
      @Nullable String description,
      Aggregation aggregation,
      AttributesProcessor attributesProcessor) {
    // TODO - Add the ability to track when a View was registered via a config file.
    return new AutoValue_View(
        name, description, aggregation, attributesProcessor, SourceInfo.fromCurrentStack());
  }
}
