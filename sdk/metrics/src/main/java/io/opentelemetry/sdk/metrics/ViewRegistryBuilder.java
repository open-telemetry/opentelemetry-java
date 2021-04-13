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

class ViewRegistryBuilder {
  private final EnumMap<InstrumentType, LinkedHashMap<Pattern, View>> configuration =
      new EnumMap<>(InstrumentType.class);
  private static final LinkedHashMap<Pattern, View> EMPTY_CONFIG = new LinkedHashMap<>();

  ViewRegistryBuilder() {
    configuration.put(InstrumentType.COUNTER, EMPTY_CONFIG);
    configuration.put(InstrumentType.UP_DOWN_COUNTER, EMPTY_CONFIG);
    configuration.put(InstrumentType.VALUE_RECORDER, EMPTY_CONFIG);
    configuration.put(InstrumentType.SUM_OBSERVER, EMPTY_CONFIG);
    configuration.put(InstrumentType.UP_DOWN_SUM_OBSERVER, EMPTY_CONFIG);
    configuration.put(InstrumentType.VALUE_OBSERVER, EMPTY_CONFIG);
  }

  ViewRegistry build() {
    return new ViewRegistry(configuration);
  }

  ViewRegistryBuilder addView(InstrumentSelector selector, View view) {
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
