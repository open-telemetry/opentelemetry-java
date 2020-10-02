/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The default {@link IdsGenerator} which generates IDs as random numbers using {@link
 * ThreadLocalRandom}.
 */
public final class RandomIdsGenerator implements IdsGenerator {

  private static final long INVALID_ID = 0;

  @Override
  public String generateSpanId() {
    long id;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return SpanId.fromLong(id);
  }

  @Override
  public String generateTraceId() {
    long idHi;
    long idLo;
    ThreadLocalRandom random = ThreadLocalRandom.current();
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return TraceId.fromLongs(idHi, idLo);
  }
}
