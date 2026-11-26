/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcMetricExporterModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link OtlpGrpcMetricExporterModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class OtlpGrpcMetricExporterModelAccessor {

  private OtlpGrpcMetricExporterModelAccessor() {}

  static final String PROXY = "proxy/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(PROXY, ExperimentalProxyModel.class);
  }

  @Nullable
  public static ExperimentalProxyModel getProxy(OtlpGrpcMetricExporterModel model) {
    return ExtensionPropertyUtil.get(
        PROXY, model.getExtensionProperties(), ExperimentalProxyModel.class);
  }

  public static OtlpGrpcMetricExporterModel setProxy(
      OtlpGrpcMetricExporterModel model, ExperimentalProxyModel value) {
    requireNonNull(value, "value");
    model.setExtensionProperty(PROXY, value);
    return model;
  }
}
