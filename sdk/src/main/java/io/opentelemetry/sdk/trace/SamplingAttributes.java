/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.attributes.DoubleAttributeSetter;

/** Attributes that are applied by {@link Sampler} instances. */
public class SamplingAttributes {

  /**
   * Probability value used by a probability-based Span sampling strategy.
   *
   * <p>Note: This will need to be updated if a specification for this value is merged which changes
   * this proposed value.
   *
   * <p>See https://github.com/open-telemetry/opentelemetry-specification/pull/570
   */
  public static final DoubleAttributeSetter SAMPLING_PROBABILITY =
      DoubleAttributeSetter.create("sampling.probability");

  private SamplingAttributes() {}
}
