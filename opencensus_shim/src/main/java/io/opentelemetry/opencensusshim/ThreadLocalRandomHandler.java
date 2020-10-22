/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.implcore.trace.internal.RandomHandler;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of the {@link RandomHandler} using {@link ThreadLocalRandom}. */
@ThreadSafe
public final class ThreadLocalRandomHandler extends RandomHandler {

  /** Constructs a new {@code ThreadLocalRandomHandler}. */
  public ThreadLocalRandomHandler() {}

  @Override
  public Random current() {
    return ThreadLocalRandom.current();
  }
}
