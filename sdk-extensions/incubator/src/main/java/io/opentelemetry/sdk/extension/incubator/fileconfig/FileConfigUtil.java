/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  /** Add the {@code closeable} to the {@code closeables} and return it. */
  static <T> T addAndReturn(List<Closeable> closeables, T closeable) {
    if (closeable instanceof Closeable) {
      closeables.add((Closeable) closeable);
    }
    return closeable;
  }

  static <T> T assertNotNull(@Nullable T object, String description) {
    if (object == null) {
      throw new NullPointerException(description + " is null");
    }
    return object;
  }

  static <T> T requireNonNull(@Nullable T object, String description) {
    if (object == null) {
      throw new ConfigurationException(description + " is required but is null");
    }
    return object;
  }

  /**
   * Find a registered {@link ComponentProvider} which {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(StructuredConfigProperties)} with the given {@code model}.
   *
   * @throws ConfigurationException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(StructuredConfigProperties)} throws
   */
  static <T> T loadComponent(SpiHelper spiHelper, Class<T> type, String name, Object model) {
    // Map model to generic structured config properties
    StructuredConfigProperties config =
        FileConfiguration.toConfigProperties(model, spiHelper.getComponentLoader());
    return spiHelper.loadComponent(type, name, config);
  }
}
