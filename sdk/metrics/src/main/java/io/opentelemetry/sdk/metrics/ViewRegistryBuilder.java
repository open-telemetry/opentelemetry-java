/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class ViewRegistryBuilder {
  private final EnumMap<InstrumentType, LinkedHashMap<Pattern, View>> configuration =
      new EnumMap<>(InstrumentType.class);
  private static final LinkedHashMap<Pattern, View> EMPTY_CONFIG = new LinkedHashMap<>();

  ViewRegistryBuilder() {
    for (InstrumentType type : InstrumentType.values()) {
      configuration.put(type, EMPTY_CONFIG);
    }
  }

  public ViewRegistry build() {
    return new ViewRegistry(configuration);
  }

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
