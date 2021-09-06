/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * An interface that provides the two configuration components to exemplar sampling.
 *
 * <ul>
 *   <li>(coming soon) A filter for which measurements should be sampled.
 *   <li>A reservoir to store exemplars (and can do additional filtering)
 * </ul>
 */
@AutoValue
@Immutable
public abstract class ExemplarSampler {

  /** Configuration for exemplar storage. */
  public abstract ExemplarStorageStrategy getStorage();

  /**
   * We hide the ability to create custom exemplar samplers until further specification work is
   * stable.
   */
  public static Builder builder() {
    return new AutoValue_ExemplarSampler.Builder().setStorage(ExemplarStorageStrategy.ALWAYS_OFF);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setStorage(ExemplarStorageStrategy storageStrategy);

    public abstract ExemplarSampler build();
  }
}
