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
 *   <li>A filter for which measurements should be sampled.
 *   <li>A resorvoir to store exemplars (and can do additional filtering)
 * </ul>
 */
@AutoValue
@Immutable
public abstract class ExemplarSampler {

  /** Configuration for exemplar storage. */
  public abstract ExemplarStorageStrategy getStorage();
  /** Measurement filter for which to include in exemplars. */
  public abstract ExemplarFilter getFilter();

  /** A sampler which will never store Exemplars. */
  public static final ExemplarSampler ALWAYS_OFF =
      builder()
          .setFilter(ExemplarFilter.ALWAYS_OFF)
          .setStorage(ExemplarStorageStrategy.ALWAYS_OFF)
          .build();

  /** Sample measurements that were recorded during a sampled trace. */
  public static final ExemplarSampler WITH_SAMPLED_TRACES =
      builder()
          .setFilter(ExemplarFilter.WITH_TRACES)
          .setStorage(ExemplarStorageStrategy.DEFAULT)
          .build();

  /**
   * We hide the ability to create custom exemplar samplers until further specification work is
   * stable.
   */
  private static Builder builder() {
    return new AutoValue_ExemplarSampler.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setStorage(ExemplarStorageStrategy storageStrategy);

    public abstract Builder setFilter(ExemplarFilter filter);

    public abstract ExemplarSampler build();
  }
}
