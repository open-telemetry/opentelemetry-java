/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link OtlpHttpMetricExporterModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class OtlpHttpMetricExporterModelAccessor {

  private OtlpHttpMetricExporterModelAccessor() {}

  static final String PROXY = "proxy/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(PROXY, ExperimentalProxyModel.class);
  }

  @Nullable
  public static ExperimentalProxyModel getProxy(OtlpHttpMetricExporterModel model) {
    return ExtensionPropertyUtil.get(
        PROXY, model.getExtensionProperties(), ExperimentalProxyModel.class);
  }

  public static OtlpHttpMetricExporterModel setProxy(
      OtlpHttpMetricExporterModel model, ExperimentalProxyModel value) {
    requireNonNull(value, "value");
    model.setExtensionProperty(PROXY, value);
    return model;
  }
}
