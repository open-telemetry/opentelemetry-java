/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PullMetricExporterModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link PullMetricExporterModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class PullMetricExporterModelAccessor {

  private PullMetricExporterModelAccessor() {}

  static final String PROMETHEUS = "prometheus/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(PROMETHEUS, ExperimentalPrometheusMetricExporterModel.class);
  }

  @Nullable
  public static ExperimentalPrometheusMetricExporterModel getPrometheus(
      PullMetricExporterModel model) {
    return ExtensionPropertyUtil.get(
        PROMETHEUS,
        model.getExtensionProperties(),
        ExperimentalPrometheusMetricExporterModel.class);
  }

  public static PullMetricExporterModel withPrometheus(
      PullMetricExporterModel model, ExperimentalPrometheusMetricExporterModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(PROMETHEUS, value);
    return model;
  }
}
