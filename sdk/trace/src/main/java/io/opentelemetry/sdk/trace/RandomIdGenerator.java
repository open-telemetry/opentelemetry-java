/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanIdHex;
import io.opentelemetry.api.trace.TraceIdHex;
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
    return SpanIdHex.fromLong(id);
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
    return TraceIdHex.fromLongs(idHi, idLo);
  }
}
