/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import com.google.auto.value.AutoValue;
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

  static SamplingResult createSamplingResult(Decision decision, Attributes attributes) {
    return new AutoValue_ImmutableSamplingResult(decision, attributes);
  }

  private static SamplingResult createWithoutAttributes(Decision decision) {
    return new AutoValue_ImmutableSamplingResult(decision, Attributes.empty());
  }

  @Override
  public abstract Decision getDecision();

  @Override
  public abstract Attributes getAttributes();
}
