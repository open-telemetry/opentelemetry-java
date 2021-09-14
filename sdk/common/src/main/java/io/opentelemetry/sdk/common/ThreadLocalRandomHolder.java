/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** {@link RandomHolder} that uses {@link ThreadLocalRandom}. */
enum ThreadLocalRandomHolder implements RandomHolder {
  INSTANCE;

  @Override
  public Random getRandom() {
    return ThreadLocalRandom.current();
  }
}
