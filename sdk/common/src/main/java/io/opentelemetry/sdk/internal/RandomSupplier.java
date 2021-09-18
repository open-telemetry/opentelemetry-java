/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Provides random number generater constructor utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class RandomSupplier {
  private RandomSupplier() {}

  /**
   * Returns the platform default for random number generation.
   *
   * <p>The underlying implementation attempts to use {@link java.util.concurrent.ThreadLocalRandom}
   * on platforms where this is the most efficient.
   */
  public static Supplier<Random> platformDefault() {
    // note: check borrowed from OkHttp's check for Android.
    if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
      return AndroidFriendlyRandomHolder.INSTANCE;
    }
    return ThreadLocalRandom::current;
  }
}
