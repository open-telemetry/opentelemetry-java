/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** {@link PerOperationSampler} samples spans per operation. */
class PerOperationSampler implements Sampler {

  static final String TYPE = "jaeger_remote";
  static final AttributeKey<String> SAMPLER_TYPE = stringKey("sampler.type");
  static final AttributeKey<String> SAMPLER_PARAM = stringKey("sampler.description");

  private final Sampler defaultSampler;
  private final Attributes defaultAttributes;
  private final Map<String, Sampler> perOperationSampler;
  private final Map<String, Attributes> resultAttributes;

  PerOperationSampler(
      Sampler defaultSampler,
      List<SamplingStrategyResponse.OperationSamplingStrategy> perOperationSampling) {
    this.defaultSampler = defaultSampler;
    this.defaultAttributes =
        Attributes.of(
            SAMPLER_TYPE,
            TYPE,
            SAMPLER_PARAM,
            "level=default;" + this.defaultSampler.getDescription());
    this.perOperationSampler = new LinkedHashMap<>(perOperationSampling.size());
    this.resultAttributes = new LinkedHashMap<>(perOperationSampling.size());
    for (SamplingStrategyResponse.OperationSamplingStrategy opSamplingStrategy :
        perOperationSampling) {
      this.perOperationSampler.put(
          opSamplingStrategy.operation,
          Sampler.traceIdRatioBased(opSamplingStrategy.probabilisticSamplingStrategy.samplingRate));
      this.resultAttributes.put(
          opSamplingStrategy.operation,
          Attributes.of(
              SAMPLER_TYPE,
              TYPE,
              SAMPLER_PARAM,
              "level=operation;" + this.defaultSampler.getDescription()));
    }
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    Sampler sampler = this.perOperationSampler.get(name);
    Attributes resultAttributes = this.resultAttributes.get(name);
    if (sampler == null) {
      sampler = this.defaultSampler;
      resultAttributes = defaultAttributes;
    }
    SamplingResult samplingResult =
        sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);

    // If there is parent span, then the sampling decision is carried forward from the parent, do
    // not add the sampling attributes
    if (null == resultAttributes || Span.fromContext(parentContext).getSpanContext().isValid()) {
      return samplingResult;
    }
    return SamplingResult.create(samplingResult.getDecision(), resultAttributes);
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
