/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import javax.annotation.concurrent.Immutable;

/**
 * A summary metric value.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time
 */
@Immutable
@AutoValue
public abstract class ImmutableValueAtPercentile implements ValueAtPercentile {
  public static ValueAtPercentile create(double percentile, double value) {
    return new AutoValue_ImmutableValueAtPercentile(percentile, value);
  }

  ImmutableValueAtPercentile() {}
}
