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

final class ComposableProbabilitySampler implements ComposableSampler {
  private static void validateRatio(double ratio) {
    if (Double.isNaN(ratio) || ratio < 0.0 || ratio > 1.0) {
      throw new IllegalArgumentException("ratio must be in range [0.0, 1.0]");
    }
  }

  private static long calculateThreshold(double ratio) {
    return ImmutableSamplingIntent.MAX_THRESHOLD
        - Math.round(ratio * (double) ImmutableSamplingIntent.MAX_THRESHOLD);
  }

  private final SamplingIntent intent;
  private final String description;

  ComposableProbabilitySampler(double ratio) {
    validateRatio(ratio);

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
              /* adjustedCountReliable= */ false,
              Attributes.empty(),
              ImmutableSamplingIntent.NO_TRACE_STATE_UPDATE);
    } else {
      StringBuilder sb = new StringBuilder();
      OtelTraceState.serializeTh(threshold, sb);
      thresholdStr = sb.toString();

      this.intent =
          SamplingIntent.create(
              threshold,
              /* adjustedCountReliable= */ true,
              Attributes.empty(),
              ImmutableSamplingIntent.NO_TRACE_STATE_UPDATE);
    }
    this.description =
        "ComposableProbabilitySampler{threshold=" + thresholdStr + ", ratio=" + ratio + "}";
  }

  @Override
  public SamplingIntent getSamplingIntent(
      Context parentContext,
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
