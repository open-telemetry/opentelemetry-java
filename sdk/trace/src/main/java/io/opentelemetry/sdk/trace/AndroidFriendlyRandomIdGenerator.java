/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import java.util.Random;

/**
 * {@link IdGenerator} instance that doesn't use {@link java.util.concurrent.ThreadLocalRandom},
 * which is broken on most versions of Android (it uses the same seed everytime it starts up).
 */
enum AndroidFriendlyRandomIdGenerator implements IdGenerator {
  INSTANCE;

  // note: we can't use ThreadLocal.withInitial(..) on Android API 21
  private static final ThreadLocal<Random> threadLocalRandom =
      new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
          return new Random();
        }
      };

  private static final long INVALID_ID = 0;

  @Override
  public String generateSpanId() {
    long id;
    Random random = threadLocalRandom.get();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return SpanId.fromLong(id);
  }

  @Override
  public String generateTraceId() {
    long idHi;
    long idLo;
    Random random = threadLocalRandom.get();
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return TraceId.fromLongs(idHi, idLo);
  }
}
