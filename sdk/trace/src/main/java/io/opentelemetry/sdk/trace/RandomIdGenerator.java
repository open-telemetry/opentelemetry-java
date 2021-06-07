/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import java.util.concurrent.ThreadLocalRandom;

enum RandomIdGenerator implements IdGenerator {
  INSTANCE;

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
    ThreadLocalRandom random = ThreadLocalRandom.current();
    long idHi = random.nextLong();
    long idLo;
    do {
      idLo = random.nextLong();
    } while (idLo == INVALID_ID);
    return TraceId.fromLongs(idHi, idLo);
  }
}
