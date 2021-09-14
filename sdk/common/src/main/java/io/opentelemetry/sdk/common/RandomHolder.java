/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import java.util.Random;
import javax.annotation.concurrent.ThreadSafe;

/** Interface that pulls the most efficient random per-platform. */
@ThreadSafe
public interface RandomHolder {

  /** Returns the random number generator to use. */
  Random getRandom();

  /**
   * Returns the platform default for random number generation.
   *
   * <p>The underlying implementation attempts to use {@link java.util.concurrent.ThreadLocalRandom}
   * on platforms where this is the most efficient.
   */
  static RandomHolder platformDefault() {
    // note: check borrowed from OkHttp's check for Android.
    if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
      return androidFriendly();
    }
    return ThreadLocalRandomHolder.INSTANCE;
  }

  /**
   * Returns an android-friendly random holder.
   *
   * <p>On android, ThreadLocalRandom is instantiated with the same initial seed on all new threads
   * leading to poor randomness.
   */
  static RandomHolder androidFriendly() {
    return AndroidFriendlyRandomHolder.INSTANCE;
  }

  /**
   * Returns a custom random number holder.
   *
   * @param random The implementation of random to use.
   */
  static RandomHolder create(Random random) {
    return () -> random;
  }
}
