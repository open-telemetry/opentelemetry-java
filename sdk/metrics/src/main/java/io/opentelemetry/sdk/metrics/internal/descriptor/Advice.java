/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
@Immutable
public abstract class Advice {

  private static final Advice EMPTY_ADVICE = builder().build();

  public static Advice empty() {
    return EMPTY_ADVICE;
  }

  public static AdviceBuilder builder() {
    return new AutoValue_Advice.Builder();
  }

  Advice() {}

  @Nullable
  public abstract List<Double> getExplicitBucketBoundaries();

  @Nullable
  public abstract List<AttributeKey<?>> getAttributes();

  public boolean hasAttributes() {
    return getAttributes() != null;
  }

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  @AutoValue.Builder
  public abstract static class AdviceBuilder {

    AdviceBuilder() {}

    abstract AdviceBuilder explicitBucketBoundaries(
        @Nullable List<Double> explicitBucketBoundaries);

    /**
     * Sets the explicit bucket histogram boundaries.
     *
     * @param explicitBucketBoundaries the explicit bucket histogram boundaries.
     */
    public AdviceBuilder setExplicitBucketBoundaries(
        @Nullable List<Double> explicitBucketBoundaries) {
      if (explicitBucketBoundaries != null) {
        explicitBucketBoundaries =
            Collections.unmodifiableList(new ArrayList<>(explicitBucketBoundaries));
      }
      return explicitBucketBoundaries(explicitBucketBoundaries);
    }

    abstract AdviceBuilder attributes(@Nullable List<AttributeKey<?>> attributes);

    /**
     * Sets the list of the attribute keys to be used for the resulting instrument.
     *
     * @param attributes the list of the attribute keys.
     */
    public AdviceBuilder setAttributes(@Nullable List<AttributeKey<?>> attributes) {
      if (attributes != null) {
        attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
      }
      return attributes(attributes);
    }

    public abstract Advice build();
  }
}
