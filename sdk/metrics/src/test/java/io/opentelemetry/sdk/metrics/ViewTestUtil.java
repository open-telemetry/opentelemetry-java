/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesFilters;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import java.util.function.UnaryOperator;

/**
 * Test utility providing package-private access to {@link View} properties for tests outside the
 * package.
 */
public final class ViewTestUtil {
  private ViewTestUtil() {}

  /** Creates a {@link RegisteredView} with explicit properties extracted from the {@link View}. */
  public static RegisteredView createRegisteredView(
      InstrumentSelector selector,
      View view,
      UnaryOperator<Attributes> attributesFilter,
      int cardinalityLimit,
      SourceInfo sourceInfo) {
    return RegisteredView.create(
        selector,
        view,
        attributesFilter,
        cardinalityLimit,
        sourceInfo,
        view.getExplicitAggregation(),
        view.getExplicitAttributeFilter(),
        view.getExplicitCardinalityLimit());
  }

  /** Creates a {@link RegisteredView} with default attributes filter and cardinality limit. */
  public static RegisteredView createRegisteredView(InstrumentSelector selector, View view) {
    return createRegisteredView(
        selector,
        view,
        AttributesFilters.ALLOW_ALL,
        view.getCardinalityLimit(),
        SourceInfo.fromCurrentStack());
  }
}
