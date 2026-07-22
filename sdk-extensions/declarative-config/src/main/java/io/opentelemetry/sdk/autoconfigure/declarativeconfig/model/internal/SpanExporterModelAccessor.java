/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link SpanExporterModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SpanExporterModelAccessor {

  private SpanExporterModelAccessor() {}

  static final String OTLP_FILE = "otlp_file/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(OTLP_FILE, ExperimentalOtlpFileExporterModel.class);
  }

  @Nullable
  public static ExperimentalOtlpFileExporterModel getOtlpFile(SpanExporterModel model) {
    return ExtensionPropertyUtil.get(
        OTLP_FILE, model.getExtensionProperties(), ExperimentalOtlpFileExporterModel.class);
  }

  public static SpanExporterModel withOtlpFile(
      SpanExporterModel model, ExperimentalOtlpFileExporterModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(OTLP_FILE, value);
    return model;
  }
}
