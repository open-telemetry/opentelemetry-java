/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.jaeger.sampler;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Sampling.OperationSamplingStrategy;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
          Sampler.traceIdRatioBased(
              opSamplingStrategy.getProbabilisticSampling().getSamplingRate()));
    }
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      Kind spanKind,
      ReadableAttributes attributes,
      List<Link> parentLinks) {
    Sampler sampler = this.perOperationSampler.get(name);
    if (sampler == null) {
      sampler = this.defaultSampler;
    }
    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return String.format(
        "PerOperationSampler{default=%s, perOperation=%s}",
        this.defaultSampler, this.perOperationSampler);
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
