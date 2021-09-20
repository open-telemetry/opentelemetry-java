/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.Random;
import java.util.function.Supplier;

/**
 * {@link RandomSupplier} instance that doesn't use {@link java.util.concurrent.ThreadLocalRandom},
 * which is broken on most versions of Android (it uses the same seed everytime it starts up).
 */
enum AndroidFriendlyRandomHolder implements Supplier<Random> {
  INSTANCE;

  private static final Random random = new Random();

  @Override
  public Random get() {
    return random;
  }
}
