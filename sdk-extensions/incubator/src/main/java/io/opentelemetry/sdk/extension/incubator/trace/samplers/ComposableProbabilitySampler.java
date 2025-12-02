/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.util.List;
import java.util.function.Function;

final class ComposableProbabilitySampler implements ComposableSampler {
  private static long calculateThreshold(double ratio) {
    return ImmutableSamplingIntent.MAX_THRESHOLD
        - Math.round(ratio * (double) ImmutableSamplingIntent.MAX_THRESHOLD);
  }

  private final SamplingIntent intent;
  private final String description;

  ComposableProbabilitySampler(double ratio) {
    long threshold = calculateThreshold(ratio);
    String thresholdStr;
    if (threshold == ImmutableSamplingIntent.MAX_THRESHOLD) {
      thresholdStr = "max";

      // Same as ComposableAlwaysOffSampler, notably the threshold is not considered reliable.
      // The spec mentions returning an instance of ComposableAlwaysOffSampler in this case but
      // it seems clearer if the description of the sampler matches the user's request.
      this.intent =
          SamplingIntent.create(
              ImmutableSamplingIntent.INVALID_THRESHOLD,
              /* thresholdReliable= */ false,
              Attributes.empty(),
              Function.identity());
    } else {
      StringBuilder sb = new StringBuilder();
      OtelTraceState.serializeTh(threshold, sb);
      thresholdStr = sb.toString();

      this.intent =
          SamplingIntent.create(
              threshold, /* thresholdReliable= */ true, Attributes.empty(), Function.identity());
    }
    this.description =
        "ComposableTraceIdRatioBasedSampler{threshold=" + thresholdStr + ", ratio=" + ratio + "}";
  }

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    return intent;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return this.getDescription();
  }
}
