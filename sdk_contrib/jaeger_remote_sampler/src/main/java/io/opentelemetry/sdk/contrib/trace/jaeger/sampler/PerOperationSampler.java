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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.OperationSamplingStrategy;
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

/** {@link PerOperationSampler} samples spans per operation. */
class PerOperationSampler implements Sampler {

  private final Sampler defaultSampler;
  private final Map<String, Sampler> perOperationSampler;

  PerOperationSampler(
      Sampler defaultSampler, List<OperationSamplingStrategy> perOperationSampling) {
    this.defaultSampler = defaultSampler;
    this.perOperationSampler = new LinkedHashMap<>(perOperationSampling.size());
    for (OperationSamplingStrategy opSamplingStrategy : perOperationSampling) {
      this.perOperationSampler.put(
          opSamplingStrategy.getOperation(),
          Samplers.probability(opSamplingStrategy.getProbabilisticSampling().getSamplingRate()));
    }
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
    Sampler sampler = this.perOperationSampler.get(name);
    if (sampler == null) {
      sampler = this.defaultSampler;
    }
    return sampler.shouldSample(
        parentContext, traceId, spanId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return toString();
  }

  @Override
  public String toString() {
    return String.format(
        "PerOperationSampler{default=%s, perOperation=%s}",
        this.defaultSampler, this.perOperationSampler);
  }
}
