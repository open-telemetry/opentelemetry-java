/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

/**
 * Builder for {@link ViewRegistry}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ViewRegistryBuilder {
  private final EnumMap<InstrumentType, LinkedHashMap<Pattern, View>> configuration =
      new EnumMap<>(InstrumentType.class);
  private static final LinkedHashMap<Pattern, View> EMPTY_CONFIG = new LinkedHashMap<>();

  ViewRegistryBuilder() {
    for (InstrumentType type : InstrumentType.values()) {
      configuration.put(type, EMPTY_CONFIG);
    }
  }

  /** Returns the {@link ViewRegistry}. */
  public ViewRegistry build() {
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
    LinkedHashMap<Pattern, View> parentConfiguration =
        configuration.get(selector.getInstrumentType());
    configuration.put(
        selector.getInstrumentType(),
        newLinkedHashMap(selector.getInstrumentNamePattern(), view, parentConfiguration));
    return this;
  }

  private static LinkedHashMap<Pattern, View> newLinkedHashMap(
      Pattern pattern, View view, LinkedHashMap<Pattern, View> parentConfiguration) {
    LinkedHashMap<Pattern, View> result = new LinkedHashMap<>();
    result.put(pattern, view);
    result.putAll(parentConfiguration);
    return result;
  }
}
