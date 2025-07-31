/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.sdk.trace.IdGenerator;

/**
 * An extension to {@link IdGenerator} to allow opting in to the random flag in the draft <a
 * href="https://www.w3.org/TR/trace-context-2/#trace-flags">W3C Trace Context Level 2</a>
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedIdGenerator extends IdGenerator {
  /**
   * Returns {@code true} if the {@link IdGenerator} returns trace IDs with the right-most 7 bytes
   * being random.
   */
  default boolean randomTraceId() {
    // Assume IDs are random since in practice, they are.
    return true;
  }
}
