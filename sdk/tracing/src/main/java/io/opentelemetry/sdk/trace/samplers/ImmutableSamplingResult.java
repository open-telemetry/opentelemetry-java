/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static io.opentelemetry.api.common.AttributeKey.doubleKey;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableSamplingResult implements SamplingResult {

  static final SamplingResult EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT =
      ImmutableSamplingResult.createWithoutAttributes(SamplingResult.Decision.RECORD_AND_SAMPLE);

  static final SamplingResult EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT =
      ImmutableSamplingResult.createWithoutAttributes(SamplingResult.Decision.DROP);

  static final SamplingResult EMPTY_RECORDED_SAMPLING_RESULT =
      ImmutableSamplingResult.createWithoutAttributes(SamplingResult.Decision.RECORD_ONLY);

  /**
   * Probability value used by a probability-based Span sampling strategy.
   *
   * <p>Note: This will need to be updated if a specification for this value is merged which changes
   * this proposed value. Also, once it's in the spec, we should move it somewhere more visible.
   *
   * <p>See https://github.com/open-telemetry/opentelemetry-specification/pull/570
   */
  // Visible for tests.
  static final AttributeKey<Double> SAMPLING_PROBABILITY = doubleKey("sampling.probability");

  static SamplingResult createSamplingResult(Decision decision, Attributes attributes) {
    return new AutoValue_ImmutableSamplingResult(decision, attributes);
  }

  static SamplingResult createWithProbability(Decision decision, double probability) {
    return new AutoValue_ImmutableSamplingResult(
        decision, Attributes.of(ImmutableSamplingResult.SAMPLING_PROBABILITY, probability));
  }

  private static SamplingResult createWithoutAttributes(Decision decision) {
    return new AutoValue_ImmutableSamplingResult(decision, Attributes.empty());
  }

  @Override
  public abstract Decision getDecision();

  @Override
  public abstract Attributes getAttributes();
}
