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

  private static final Random random = new Random();

  private static final long INVALID_ID = 0;

  @Override
  public String generateSpanId() {
    long id;
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return SpanId.fromLong(id);
  }

  @Override
  public String generateTraceId() {
    long idHi = random.nextLong();
    long idLo;
    do {
      idLo = random.nextLong();
    } while (idLo == INVALID_ID);
    return TraceId.fromLongs(idHi, idLo);
  }
}
