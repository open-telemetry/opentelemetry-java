/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for {@link ViewRegistry}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ViewRegistryBuilder {
  private final List<RegisteredView> orderedViews = new ArrayList<>();

  ViewRegistryBuilder() {}

  /** Returns the {@link ViewRegistry}. */
  public ViewRegistry build() {
    return new ViewRegistry(Collections.unmodifiableList(orderedViews));
  }

  /**
   * Adds a new view to the registry.
   *
   * @param selector The instruments that should have their defaults altered.
   * @param view The {@link View} metric definition.
   * @return this
   */
  public ViewRegistryBuilder addView(InstrumentSelector selector, View view) {
    orderedViews.add(RegisteredView.create(selector, view));
    return this;
  }
}
