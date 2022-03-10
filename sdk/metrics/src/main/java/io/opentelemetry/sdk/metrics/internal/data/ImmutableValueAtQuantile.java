/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import javax.annotation.concurrent.Immutable;

/**
 * A summary metric value.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time
 */
@Immutable
@AutoValue
public abstract class ImmutableValueAtQuantile implements ValueAtQuantile {
  public static ValueAtQuantile create(double quantile, double value) {
    return new AutoValue_ImmutableValueAtQuantile(quantile, value);
  }

  ImmutableValueAtQuantile() {}
}
