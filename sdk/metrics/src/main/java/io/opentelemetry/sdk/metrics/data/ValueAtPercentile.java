/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ValueAtPercentile {
  public static ValueAtPercentile create(double percentile, double value) {
    return new AutoValue_ValueAtPercentile(percentile, value);
  }

  ValueAtPercentile() {}

  /**
   * The percentile of a distribution. Must be in the interval [0.0, 100.0].
   *
   * @return the percentile.
   */
  public abstract double getPercentile();

  /**
   * The value at the given percentile of a distribution.
   *
   * @return the value at the percentile.
   */
  public abstract double getValue();
}
