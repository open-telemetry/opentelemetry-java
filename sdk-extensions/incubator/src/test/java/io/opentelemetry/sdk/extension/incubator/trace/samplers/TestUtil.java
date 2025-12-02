/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.trace.TraceId;
import java.util.Random;
import java.util.function.Supplier;

final class TestUtil {

  static Supplier<String> traceIdGenerator() {
    // Generate a fixed set of random trace IDs for reliable sampling tests.
    Random random = new Random(0xabcd1234L);
    return () -> {
      long a = random.nextLong();
      long b;
      do {
        b = random.nextLong();
      } while (b == 0);
      return TraceId.fromLongs(a, b);
    };
  }

  private TestUtil() {}
}
