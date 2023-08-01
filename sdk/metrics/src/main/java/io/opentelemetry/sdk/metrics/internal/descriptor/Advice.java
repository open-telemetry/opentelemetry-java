/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class Advice {

  private static final Advice EMPTY_ADVICE = create(null);

  public static Advice empty() {
    return EMPTY_ADVICE;
  }

  /**
   * Creates a new {@link Advice} with the given explicit bucket histogram boundaries.
   *
   * @param explicitBucketBoundaries the explicit bucket histogram boundaries.
   * @return a new {@link Advice} with the given bucket boundaries.
   */
  public static Advice create(@Nullable List<Double> explicitBucketBoundaries) {
    if (explicitBucketBoundaries != null) {
      explicitBucketBoundaries =
          Collections.unmodifiableList(new ArrayList<>(explicitBucketBoundaries));
    }
    return new AutoValue_Advice(explicitBucketBoundaries);
  }

  Advice() {}

  @Nullable
  public abstract List<Double> getExplicitBucketBoundaries();
}
