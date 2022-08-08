/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import javax.annotation.Nullable;

/**
 * Holder for the non-public defaultIfNull method. This serves only to mitigate the method being on
 * a public interface.
 */
final class ConfigUtil {

  /** Returns defaultValue if value is null, otherwise value. This is an internal method. */
  static <T> T defaultIfNull(@Nullable T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }

  private ConfigUtil() {}
}
