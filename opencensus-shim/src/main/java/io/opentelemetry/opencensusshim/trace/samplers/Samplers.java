/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.opencensusshim.trace.samplers;

import io.opentelemetry.opencensusshim.trace.Sampler;
import io.opentelemetry.opencensusshim.trace.Span;

/**
 * Static class to access a set of pre-defined {@link Sampler Samplers}.
 *
 * @since 0.1.0
 */
public final class Samplers {
  private static final Sampler ALWAYS_SAMPLE = new AlwaysSampleSampler();
  private static final Sampler NEVER_SAMPLE = new NeverSampleSampler();

  // No instance of this class.
  private Samplers() {}

  /**
   * Returns a {@link Sampler} that always makes a "yes" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler alwaysSample() {
    return ALWAYS_SAMPLE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "no" decision on {@code Span} sampling.
   * @since 0.1.0
   */
  public static Sampler neverSample() {
    return NEVER_SAMPLE;
  }

  /**
   * Returns a {@link Sampler} that makes a "yes" decision with a given probability.
   *
   * @param probability The desired probability of sampling. Must be within [0.0, 1.0].
   * @return a {@code Sampler} that makes a "yes" decision with a given probability.
   * @throws IllegalArgumentException if {@code probability} is out of range
   * @since 0.1.0
   */
  public static Sampler probabilitySampler(double probability) {
    return ProbabilitySampler.create(probability);
  }
}
