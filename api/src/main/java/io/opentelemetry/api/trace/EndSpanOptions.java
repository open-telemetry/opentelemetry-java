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
  private static final EndSpanOptions DEFAULT = builder().build();

  /** The default {@code EndSpanOptions}. */
  static EndSpanOptions getDefault() {
    return DEFAULT;
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new AutoValue_EndSpanOptions.Builder().setEndTimestamp(0);
  }

  /**
   * Returns the end timestamp.
   *
   * <p>Important this is NOT equivalent with System.nanoTime().
   *
   * @return the end timestamp.
   */
  public abstract long getEndTimestamp();

  /** Builder class for {@link EndSpanOptions}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * Sets the end timestamp for the {@link Span}.
     *
     * <p>Important this is NOT equivalent with System.nanoTime().
     *
     * @param endTimestamp the end timestamp in nanos since epoch.
     * @return this.
     */
    public abstract Builder setEndTimestamp(long endTimestamp);

    /**
     * Builds and returns a {@code EndSpanOptions} with the desired settings.
     *
     * @return a {@code EndSpanOptions} with the desired settings.
     */
    public abstract EndSpanOptions build();

    Builder() {}
  }

  EndSpanOptions() {}
}
