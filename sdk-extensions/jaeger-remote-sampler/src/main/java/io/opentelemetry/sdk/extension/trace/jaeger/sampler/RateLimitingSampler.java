/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
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
    this.rateLimiter = new RateLimiter(maxTracesPerSecond, maxBalance, SystemClock.getInstance());
    Attributes attributes =
        Attributes.of(SAMPLER_TYPE, TYPE, SAMPLER_PARAM, (double) maxTracesPerSecond);
    this.onSamplingResult =
        SamplingResult.create(SamplingResult.Decision.RECORD_AND_SAMPLE, attributes);
    this.offSamplingResult = SamplingResult.create(SamplingResult.Decision.DROP, attributes);
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<Link> parentLinks) {

    if (Span.fromContext(parentContext).getSpanContext().isSampled()) {
      return Sampler.alwaysOn()
          .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }
    for (SpanData.Link parentLink : parentLinks) {
      if (parentLink.getContext().isSampled()) {
        return Sampler.alwaysOn()
            .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
      }
    }
    return this.rateLimiter.checkCredit(1.0) ? onSamplingResult : offSamplingResult;
  }

  @Override
  public String getDescription() {
    return String.format("RateLimitingSampler{%.2f}", maxTracesPerSecond);
  }

  @Override
  public String toString() {
    return getDescription();
  }

  @VisibleForTesting
  double getMaxTracesPerSecond() {
    return maxTracesPerSecond;
  }
}
