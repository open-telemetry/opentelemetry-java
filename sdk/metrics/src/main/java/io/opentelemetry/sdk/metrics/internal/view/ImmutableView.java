/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.View;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** TODO: javadoc. */
@AutoValue
@Immutable
public abstract class ImmutableView implements View {

  /** Returns the {@link AttributesProcessor} for the {@link View}. */
  public static AttributesProcessor getAttributesProcessor(View view) {
    if (view instanceof ImmutableView) {
      return ((ImmutableView) view).getAttributesProcessor();
    }
    return AttributesProcessor.NOOP;
  }

  /** Processor of attributes before performing aggregation. */
  abstract AttributesProcessor getAttributesProcessor();

  /** Returns the {@link SourceInfo} for the {@link View}. */
  public static SourceInfo getSourceInfo(View view) {
    if (view instanceof ImmutableView) {
      return ((ImmutableView) view).getSourceInfo();
    }
    return SourceInfo.noSourceInfo();
  }

  /** Information about where the View was defined. */
  abstract SourceInfo getSourceInfo();

  static ImmutableView create(
      @Nullable String name,
      @Nullable String description,
      Aggregation aggregation,
      AttributesProcessor attributesProcessor) {
    // TODO - Add the ability to track when a View was registered via a config file.
    return new AutoValue_ImmutableView(
        name, description, aggregation, attributesProcessor, SourceInfo.fromCurrentStack());
  }
}
