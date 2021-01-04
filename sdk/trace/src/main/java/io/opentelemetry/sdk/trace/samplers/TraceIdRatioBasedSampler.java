/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * We assume the lower 64 bits of the traceId's are randomly distributed around the whole (long)
 * range. We convert an incoming probability into an upper bound on that value, such that we can
 * just compare the absolute value of the id and the bound to see if we are within the desired
 * probability range. Using the low bits of the traceId also ensures that systems that only use 64
 * bit ID's will also work with this sampler.
 */
@AutoValue
@Immutable
abstract class TraceIdRatioBasedSampler implements Sampler {

  TraceIdRatioBasedSampler() {}

  static Sampler create(double ratio) {
    if (ratio < 0.0 || ratio > 1.0) {
      throw new IllegalArgumentException("ratio must be in range [0.0, 1.0]");
    }
    if (ratio == 0.0) {
      return Sampler.alwaysOff();
    }
    if (ratio == 1.0) {
      return Sampler.alwaysOn();
    }
    long idUpperBound = (long) (ratio * Long.MAX_VALUE);
    return new AutoValue_TraceIdRatioBasedSampler(
        ratio,
        idUpperBound,
        ImmutableSamplingResult.createWithProbability(
            SamplingResult.Decision.RECORD_AND_SAMPLE, ratio),
        ImmutableSamplingResult.createWithProbability(SamplingResult.Decision.DROP, ratio));
  }

  abstract double getRatio();

  abstract long getIdUpperBound();

  abstract SamplingResult getPositiveSamplingResult();

  abstract SamplingResult getNegativeSamplingResult();

  @Override
  public final SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      Attributes attributes,
      List<Link> parentLinks) {
    // Always sample if we are within probability range. This is true even for child spans (that
    // may have had a different sampling samplingResult made) to allow for different sampling
    // policies,
    // and dynamic increases to sampling probabilities for debugging purposes.
    // Note use of '<' for comparison. This ensures that we never sample for probability == 0.0,
    // while allowing for a (very) small chance of *not* sampling if the id == Long.MAX_VALUE.
    // This is considered a reasonable tradeoff for the simplicity/performance requirements (this
    // code is executed in-line for every Span creation).
    return Math.abs(TraceId.getTraceIdRandomPart(traceId)) < getIdUpperBound()
        ? getPositiveSamplingResult()
        : getNegativeSamplingResult();
  }

  @Override
  public final String getDescription() {
    return String.format("TraceIdRatioBased{%.6f}", getRatio());
  }

  @Override
  public final String toString() {
    return getDescription();
  }
}
