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
 *   <li>(coming soon) A filter for which measurements can be sampled.
 *   <li>A factory to construct an {@link ExemplarReservoir} per-metric stream. The factory has
 *       access to the aggregation configuration of the stream.
 * </ul>
 */
@AutoValue
@Immutable
public abstract class ExemplarSampler {

  /** Configuration for exemplar storage. */
  public abstract ExemplarReservoirFactory getFactory();

  /**
   * We hide the ability to create custom exemplar samplers until further specification work is
   * stable.
   */
  public static Builder builder() {
    return new AutoValue_ExemplarSampler.Builder()
        .setFactory(ignore -> ExemplarReservoir.noSamples());
  }

  /** Builder for exemplar sampling. */
  @AutoValue.Builder
  public abstract static class Builder {
    /** Sets the factory used to provide {@link ExemplarReservoir}s for metric streams. */
    public abstract Builder setFactory(ExemplarReservoirFactory storageStrategy);

    /** Returns the configured {@link ExemplarSampler}. */
    public abstract ExemplarSampler build();
  }
}
