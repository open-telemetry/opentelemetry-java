/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link OpenTelemetryConfigurationModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class OpenTelemetryConfigurationModelAccessor {

  private OpenTelemetryConfigurationModelAccessor() {}

  static final String INSTRUMENTATION = "instrumentation/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(INSTRUMENTATION, ExperimentalInstrumentationModel.class);
  }

  @Nullable
  public static ExperimentalInstrumentationModel getInstrumentation(
      OpenTelemetryConfigurationModel model) {
    return ExtensionPropertyUtil.get(
        INSTRUMENTATION, model.getExtensionProperties(), ExperimentalInstrumentationModel.class);
  }

  public static OpenTelemetryConfigurationModel withInstrumentation(
      OpenTelemetryConfigurationModel model, ExperimentalInstrumentationModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(INSTRUMENTATION, value);
    return model;
  }
}
