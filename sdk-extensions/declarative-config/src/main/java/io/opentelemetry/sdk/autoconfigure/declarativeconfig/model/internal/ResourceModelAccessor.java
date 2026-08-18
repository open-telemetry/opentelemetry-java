/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link ResourceModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class ResourceModelAccessor {

  private ResourceModelAccessor() {}

  static final String DETECTION = "detection/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(DETECTION, ExperimentalResourceDetectionModel.class);
  }

  @Nullable
  public static ExperimentalResourceDetectionModel getDetection(ResourceModel model) {
    return ExtensionPropertyUtil.get(
        DETECTION, model.getExtensionProperties(), ExperimentalResourceDetectionModel.class);
  }

  public static ResourceModel withDetection(
      ResourceModel model, ExperimentalResourceDetectionModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(DETECTION, value);
    return model;
  }
}
