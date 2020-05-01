/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.contrib.trace.jaeger.sampler;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * {@link RateLimitingSampler} sampler uses a leaky bucket rate limiter to ensure that traces are
 * sampled with a certain constant rate.
 */
class RateLimitingSampler implements Sampler {
  static final String TYPE = "ratelimiting";
  static final String SAMPLER_TYPE = "sampler.type";
  static final String SAMPLER_PARAM = "sampler.param";

  private final double maxTracesPerSecond;
  private final RateLimiter rateLimiter;
  private final Map<String, AttributeValue> attributes;

  /**
   * Creates rate limiting sampler.
   *
   * @param maxTracesPerSecond the maximum number of sampled traces per second.
   */
  RateLimitingSampler(int maxTracesPerSecond) {
    this.maxTracesPerSecond = maxTracesPerSecond;
    double maxBalance = maxTracesPerSecond < 1.0 ? 1.0 : maxTracesPerSecond;
    this.rateLimiter = new RateLimiter(maxTracesPerSecond, maxBalance, MillisClock.getInstance());
    this.attributes = new LinkedHashMap<>();
    attributes.put(SAMPLER_TYPE, AttributeValue.stringAttributeValue(TYPE));
    attributes.put(SAMPLER_PARAM, AttributeValue.doubleAttributeValue(maxTracesPerSecond));
  }

  @Override
  public Decision shouldSample(
      @Nullable SpanContext parentContext,
      TraceId traceId,
      SpanId spanId,
      String name,
      Kind spanKind,
      Map<String, AttributeValue> attributes,
      List<Link> parentLinks) {
    boolean sampled = this.rateLimiter.checkCredit(1.0);
    if (parentContext != null && parentContext.getTraceFlags().isSampled()) {
      return Samplers.alwaysOn()
          .shouldSample(parentContext, traceId, spanId, name, spanKind, attributes, parentLinks);
    }
    if (parentLinks != null) {
      for (Link parentLink : parentLinks) {
        if (parentLink.getContext().getTraceFlags().isSampled()) {
          return Samplers.alwaysOn()
              .shouldSample(
                  parentContext, traceId, spanId, name, spanKind, attributes, parentLinks);
        }
      }
    }
    return new SamplingDecision(sampled, this.attributes);
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
