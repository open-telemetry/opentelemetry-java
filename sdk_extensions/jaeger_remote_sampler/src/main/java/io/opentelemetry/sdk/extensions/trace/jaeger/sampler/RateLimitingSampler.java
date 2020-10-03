/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import static io.opentelemetry.common.AttributeKey.doubleKey;
import static io.opentelemetry.common.AttributeKey.stringKey;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import java.util.List;

/**
 * {@link RateLimitingSampler} sampler uses a leaky bucket rate limiter to ensure that traces are
 * sampled with a certain constant rate.
 */
class RateLimitingSampler implements Sampler {
  static final String TYPE = "ratelimiting";
  static final AttributeKey<String> SAMPLER_TYPE = stringKey("sampler.type");
  static final AttributeKey<Double> SAMPLER_PARAM = doubleKey("sampler.param");

  private final double maxTracesPerSecond;
  private final RateLimiter rateLimiter;
  private final SamplingResult onSamplingResult;
  private final SamplingResult offSamplingResult;

  /**
   * Creates rate limiting sampler.
   *
   * @param maxTracesPerSecond the maximum number of sampled traces per second.
   */
  RateLimitingSampler(int maxTracesPerSecond) {
    this.maxTracesPerSecond = maxTracesPerSecond;
    double maxBalance = maxTracesPerSecond < 1.0 ? 1.0 : maxTracesPerSecond;
    this.rateLimiter = new RateLimiter(maxTracesPerSecond, maxBalance, MillisClock.getInstance());
    Attributes attributes =
        Attributes.of(SAMPLER_TYPE, TYPE, SAMPLER_PARAM, (double) maxTracesPerSecond);
    this.onSamplingResult = Samplers.samplingResult(Decision.RECORD_AND_SAMPLE, attributes);
    this.offSamplingResult = Samplers.samplingResult(Decision.DROP, attributes);
  }

  @Override
  public SamplingResult shouldSample(
      SpanContext parentContext,
      String traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<SpanData.Link> parentLinks) {
    if (parentContext.isSampled()) {
      return Samplers.alwaysOn()
          .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }
    if (parentLinks != null) {
      for (SpanData.Link parentLink : parentLinks) {
        if (parentLink.getContext().isSampled()) {
          return Samplers.alwaysOn()
              .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
        }
      }
    }
    return this.rateLimiter.checkCredit(1.0) ? onSamplingResult : offSamplingResult;
  }

  @Override
  public String getDescription() {
    return this.toString();
  }

  @Override
  public String toString() {
    return String.format("RateLimitingSampler{%.2f}", maxTracesPerSecond);
  }

  @VisibleForTesting
  double getMaxTracesPerSecond() {
    return maxTracesPerSecond;
  }
}
