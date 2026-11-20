/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Internal representation of a {@link View} and {@link InstrumentSelector}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class RegisteredView {

  public static RegisteredView create(
      InstrumentSelector selector,
      View view,
      UnaryOperator<Attributes> attributesFilter,
      int cardinalityLimit,
      SourceInfo viewSourceInfo,
      @Nullable Aggregation explicitAggregation,
      @Nullable Predicate<String> explicitAttributeFilter,
      @Nullable Integer explicitCardinalityLimit) {
    return new AutoValue_RegisteredView(
        selector,
        view,
        attributesFilter,
        cardinalityLimit,
        viewSourceInfo,
        explicitAggregation,
        explicitAttributeFilter,
        explicitCardinalityLimit);
  }

  public static RegisteredView create(
      InstrumentSelector selector,
      View view,
      UnaryOperator<Attributes> attributesFilter,
      int cardinalityLimit,
      SourceInfo viewSourceInfo) {
    return create(
        selector, view, attributesFilter, cardinalityLimit, viewSourceInfo, null, null, null);
  }

  RegisteredView() {}

  /** Instrument filter for applying this view. */
  public abstract InstrumentSelector getInstrumentSelector();

  /** The view to apply. */
  public abstract View getView();

  /**
   * The attributes filter applied to recorded measurements. Built from the view's {@link
   * View#getAttributeFilter()} or, for the instrument-default view with attribute advice, from the
   * advice's attribute keys.
   */
  public abstract UnaryOperator<Attributes> getAttributesFilter();

  /** The view's cardinality limit. */
  public abstract int getCardinalityLimit();

  /** The {@link SourceInfo} from where the view was registered. */
  public abstract SourceInfo getViewSourceInfo();

  @Nullable
  public abstract Aggregation getExplicitAggregation();

  @Nullable
  public abstract Predicate<String> getExplicitAttributeFilter();

  @Nullable
  public abstract Integer getExplicitCardinalityLimit();

  @Override
  public final String toString() {
    return "RegisteredView{"
        + "instrumentSelector="
        + getInstrumentSelector()
        + ", view="
        + getView()
        + "}";
  }
}
