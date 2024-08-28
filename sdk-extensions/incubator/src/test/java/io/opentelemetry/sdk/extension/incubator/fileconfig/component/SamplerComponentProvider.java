/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;

public class SamplerComponentProvider implements ComponentProvider<Sampler> {
  @Override
  public Class<Sampler> getType() {
    return Sampler.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public Sampler create(StructuredConfigProperties config) {
    return new TestSampler(config);
  }

  public static class TestSampler implements Sampler {

    public final StructuredConfigProperties config;

    private TestSampler(StructuredConfigProperties config) {
      this.config = config;
    }

    @Override
    public SamplingResult shouldSample(
        Context parentContext,
        String traceId,
        String name,
        SpanKind spanKind,
        Attributes attributes,
        List<LinkData> parentLinks) {
      return SamplingResult.recordOnly();
    }

    @Override
    public String getDescription() {
      return "test";
    }
  }
}
