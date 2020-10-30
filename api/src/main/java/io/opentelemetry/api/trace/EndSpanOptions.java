/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * A class that enables overriding the default values used when ending a {@link Span}. Allows
 * overriding the endTimestamp.
 */
@Immutable
@AutoValue
public abstract class EndSpanOptions {
  private static final EndSpanOptions DEFAULT = createWithEndTimestamp(0L);

  /**
   * Returns a {@link EndSpanOptions} indicating the span ended at the given {@code endTimestamp},
   * in nanoseconds.
   */
  public static EndSpanOptions createWithEndTimestamp(long endTimestamp) {
    return new AutoValue_EndSpanOptions(endTimestamp);
  }

  /** The default {@code EndSpanOptions}. */
  static EndSpanOptions getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the end timestamp.
   *
   * <p>Important this is NOT equivalent with System.nanoTime().
   *
   * @return the end timestamp.
   */
  public abstract long getEndTimestamp();

  EndSpanOptions() {}
}
