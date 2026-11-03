/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.sdk.trace.ReadableSpan;
import javax.annotation.Nullable;

/**
 * Extended {@link ReadableSpan} with experimental APIs.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedReadableSpan extends ReadableSpan {

  // TODO: once span type stabilizes, move getType() to ReadableSpan as a default method

  /**
   * Returns the span type, or {@code null} if not set.
   *
   * @return the span type, or {@code null} if not set.
   */
  @Nullable
  String getType();
}
