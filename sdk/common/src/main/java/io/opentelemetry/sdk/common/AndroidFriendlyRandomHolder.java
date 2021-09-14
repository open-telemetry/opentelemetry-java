/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import java.util.Random;

/**
 * {@link RandomHolder} instance that doesn't use {@link java.util.concurrent.ThreadLocalRandom},
 * which is broken on most versions of Android (it uses the same seed everytime it starts up).
 */
enum AndroidFriendlyRandomHolder implements RandomHolder {
  INSTANCE;

  private static final Random random = new Random();

  @Override
  public Random getRandom() {
    return random;
  }
}
