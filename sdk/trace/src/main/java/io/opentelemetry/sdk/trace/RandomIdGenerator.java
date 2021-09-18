/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.internal.RandomSupplier;
import java.util.Random;
import java.util.function.Supplier;

enum RandomIdGenerator implements IdGenerator {
  INSTANCE;

  private static final long INVALID_ID = 0;
  private static final Supplier<Random> randomSupplier = RandomSupplier.platformDefault();

  @Override
  public String generateSpanId() {
    long id;
    Random random = randomSupplier.get();
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return SpanId.fromLong(id);
  }

  @Override
  public String generateTraceId() {
    Random random = randomSupplier.get();
    long idHi = random.nextLong();
    long idLo;
    do {
      idLo = random.nextLong();
    } while (idLo == INVALID_ID);
    return TraceId.fromLongs(idHi, idLo);
  }
}
