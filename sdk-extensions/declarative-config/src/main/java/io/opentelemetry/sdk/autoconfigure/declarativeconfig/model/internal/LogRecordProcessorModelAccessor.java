/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link LogRecordProcessorModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class LogRecordProcessorModelAccessor {

  private LogRecordProcessorModelAccessor() {}

  static final String EVENT_TO_SPAN_EVENT_BRIDGE = "event_to_span_event_bridge/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(
        EVENT_TO_SPAN_EVENT_BRIDGE,
        ExperimentalEventToSpanEventBridgeLogRecordProcessorModel.class);
  }

  @Nullable
  public static ExperimentalEventToSpanEventBridgeLogRecordProcessorModel getEventToSpanEventBridge(
      LogRecordProcessorModel model) {
    return ExtensionPropertyUtil.get(
        EVENT_TO_SPAN_EVENT_BRIDGE,
        model.getExtensionProperties(),
        ExperimentalEventToSpanEventBridgeLogRecordProcessorModel.class);
  }

  public static LogRecordProcessorModel withEventToSpanEventBridge(
      LogRecordProcessorModel model,
      ExperimentalEventToSpanEventBridgeLogRecordProcessorModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(EVENT_TO_SPAN_EVENT_BRIDGE, value);
    return model;
  }
}
