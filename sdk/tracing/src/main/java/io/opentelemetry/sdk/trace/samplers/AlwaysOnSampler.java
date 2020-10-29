/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
enum AlwaysOnSampler implements Sampler {
  INSTANCE;

  // Returns a "yes" {@link SamplingResult} on {@link Span} sampling.
  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Span.Kind spanKind,
      ReadableAttributes attributes,
      List<SpanData.Link> parentLinks) {
    return ImmutableSamplingResult.EMPTY_RECORDED_AND_SAMPLED_SAMPLING_RESULT;
  }

  @Override
  public String getDescription() {
    return "AlwaysOnSampler";
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
