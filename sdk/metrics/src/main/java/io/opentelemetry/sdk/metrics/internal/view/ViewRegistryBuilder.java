/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for {@link ViewRegistry}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ViewRegistryBuilder {
  // private final LinkedHashMap<InstrumentSelector, View> reversedEntries = new LinkedHashMap<>();
  private final List<Map.Entry<InstrumentSelector, View>> reversedEntries - new ArrayList<>();

  ViewRegistryBuilder() {}

  /** Returns the {@link ViewRegistry}. */
  public ViewRegistry build() {
    // We add views in reverse order so normal iteration order is the priority of usage.
    // TODO: Verify this is correct with the specification.
    Collections.reverse(reversedEntries);
    LinkedHashMap<InstrumentSelector, View> configuration = new LinkedHashMap<>(reversedEntries.size());
    for (Map.Entry<InstrumentSelector, View> e: reversedEntries) {
      configuration.put(e.getKey(), e.getValue());
    }
    return new ViewRegistry(configuration);
  }

  /**
   * Adds a new view to the registry.
   *
   * @param selector The instruments that should have their defaults altered.
   * @param view The {@link View} metric definition.
   * @return this
   */
  public ViewRegistryBuilder addView(InstrumentSelector selector, View view) {
    reversedEntries.add(new AbstractMap.SimpleEntry<>(selector, view));
    return this;
  }
}
