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

  private static final SamplingResult EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT_NEW =
      ImmutableSamplingResult.createWithoutAttributes(SamplingDecision.RECORD_AND_SAMPLE);

  private static final SamplingResult EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT_NEW =
      ImmutableSamplingResult.createWithoutAttributes(SamplingDecision.DROP);

  private static final SamplingResult EMPTY_RECORDED_SAMPLING_RESULT_NEW =
      ImmutableSamplingResult.createWithoutAttributes(SamplingDecision.RECORD_ONLY);

  static SamplingResult getEmptyRecordedAndSampledSamplingResult() {
    return EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT_NEW;
  }

  static SamplingResult getEmptyNotSampledOrRecordedSamplingResult() {
    return EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT_NEW;
  }

  static SamplingResult getEmptyRecordedSamplingResult() {
    return EMPTY_RECORDED_SAMPLING_RESULT_NEW;
  }

  static SamplingResult createSamplingResult(SamplingDecision decision, Attributes attributes) {
    return new AutoValue_ImmutableSamplingResult(decision, attributes);
  }

  private static SamplingResult createWithoutAttributes(SamplingDecision decision) {
    return new AutoValue_ImmutableSamplingResult(decision, Attributes.empty());
  }

  @Override
  public abstract SamplingDecision getDecision();

  @Override
  public abstract Attributes getAttributes();
}
