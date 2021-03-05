/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import javax.annotation.concurrent.ThreadSafe;

/** Interface used by the {@link SdkTracer} to generate new {@link SpanId}s and {@link TraceId}s. */
@ThreadSafe
public interface IdGenerator {

  /**
   * Returns a {@link IdGenerator} that generates purely random IDs, which is the default for
   * OpenTelemetry.
   *
   * <p>The underlying implementation uses {@link java.util.concurrent.ThreadLocalRandom} for
   * randomness but may change in the future.
   */
  static IdGenerator random() {
    return RandomIdGenerator.INSTANCE;
  }

  /**
   * Generates a new valid {@code SpanId}.
   *
   * @return a new valid {@code SpanId}.
   */
  String generateSpanId();

  /**
   * Generates a new valid {@code TraceId}.
   *
   * @return a new valid {@code TraceId}.
   */
  String generateTraceId();
}
