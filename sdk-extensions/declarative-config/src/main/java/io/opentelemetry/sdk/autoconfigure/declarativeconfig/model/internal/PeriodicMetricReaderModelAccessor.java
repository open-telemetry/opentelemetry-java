/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PeriodicMetricReaderModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link PeriodicMetricReaderModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class PeriodicMetricReaderModelAccessor {

  private PeriodicMetricReaderModelAccessor() {}

  static final String MAX_EXPORT_BATCH_SIZE = "max_export_batch_size/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(MAX_EXPORT_BATCH_SIZE, Integer.class);
  }

  @Nullable
  public static Integer getMaxExportBatchSize(PeriodicMetricReaderModel model) {
    return ExtensionPropertyUtil.get(
        MAX_EXPORT_BATCH_SIZE, model.getExtensionProperties(), Integer.class);
  }

  public static PeriodicMetricReaderModel withMaxExportBatchSize(
      PeriodicMetricReaderModel model, Integer value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(MAX_EXPORT_BATCH_SIZE, value);
    return model;
  }
}
