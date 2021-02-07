/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
enum AlwaysOffSampler implements Sampler {
  INSTANCE;

  // Returns a "no" {@link SamplingResult} on {@link Span} sampling.
  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return ImmutableSamplingResult.EMPTY_NOT_SAMPLED_OR_RECORDED_SAMPLING_RESULT;
  }

  @Override
  public String getDescription() {
    return "AlwaysOffSampler";
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
