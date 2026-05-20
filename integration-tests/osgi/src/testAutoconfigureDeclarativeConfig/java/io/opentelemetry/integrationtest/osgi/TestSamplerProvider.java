/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSamplerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;

public class TestSamplerProvider implements ConfigurableSamplerProvider {

  @Override
  public String getName() {
    return "test-noop";
  }

  @Override
  public Sampler createSampler(ConfigProperties config) {
    return new NoopSampler();
  }

  static final class NoopSampler implements Sampler {

    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
      return SamplingResult.recordAndSample();
    }

    @Override
    public String getDescription() {
      return "TestNoopSampler";
    }

    @Override
    public String toString() {
      return "TestNoopSampler";
    }
  }
}
