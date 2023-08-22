/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  /** Add the {@code closeable} to the {@code closeables} and return it. */
  static <T extends Closeable> T addAndReturn(List<Closeable> closeables, T closeable) {
    closeables.add(closeable);
    return closeable;
  }

  static <T> T assertNotNull(@Nullable T object, String description) {
    if (object == null) {
      throw new NullPointerException(description + " is null");
    }
    return object;
  }
}
