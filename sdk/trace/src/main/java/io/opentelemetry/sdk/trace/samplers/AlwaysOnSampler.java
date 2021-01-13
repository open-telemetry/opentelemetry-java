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
enum AlwaysOnSampler implements Sampler {
  INSTANCE;

  // Returns a "yes" {@link SamplingResult} on {@link Span} sampling.
  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
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
