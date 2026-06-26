/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.JsonProviderUtil;
import io.opentelemetry.sdk.common.export.JsonProvider;
import javax.annotation.Nullable;

/**
 * Lazy holder for the resolved {@link JsonProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class JsonProviderHolder {

  private static final Object lock = new Object();
  @Nullable private static volatile JsonProvider jsonProvider;

  static JsonProvider get() {
    if (jsonProvider == null) {
      synchronized (lock) {
        if (jsonProvider == null) {
          jsonProvider =
              JsonProviderUtil.resolveJsonProvider(
                  ComponentLoader.forClassLoader(JsonProviderHolder.class.getClassLoader()));
        }
      }
    }
    return jsonProvider;
  }

  private JsonProviderHolder() {}
}
