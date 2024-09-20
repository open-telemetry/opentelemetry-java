/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerRemoteModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBasedModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBasedModel;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class SamplerFactory implements Factory<SamplerModel, Sampler> {

  private static final SamplerFactory INSTANCE = new SamplerFactory();

  private SamplerFactory() {}

  static SamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Sampler create(SamplerModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model.getAlwaysOn() != null) {
      return Sampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return Sampler.alwaysOff();
    }
    TraceIdRatioBasedModel traceIdRatioBasedModel = model.getTraceIdRatioBased();
    if (traceIdRatioBasedModel != null) {
      Double ratio = traceIdRatioBasedModel.getRatio();
      if (ratio == null) {
        ratio = 1.0d;
      }
      return Sampler.traceIdRatioBased(ratio);
    }
    ParentBasedModel parentBasedModel = model.getParentBased();
    if (parentBasedModel != null) {
      Sampler root =
          parentBasedModel.getRoot() == null
              ? Sampler.alwaysOn()
              : create(parentBasedModel.getRoot(), spiHelper, closeables);
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(root);
      if (parentBasedModel.getRemoteParentSampled() != null) {
        Sampler sampler = create(parentBasedModel.getRemoteParentSampled(), spiHelper, closeables);
        builder.setRemoteParentSampled(sampler);
      }
      if (parentBasedModel.getRemoteParentNotSampled() != null) {
        Sampler sampler =
            create(parentBasedModel.getRemoteParentNotSampled(), spiHelper, closeables);
        builder.setRemoteParentNotSampled(sampler);
      }
      if (parentBasedModel.getLocalParentSampled() != null) {
        Sampler sampler = create(parentBasedModel.getLocalParentSampled(), spiHelper, closeables);
        builder.setLocalParentSampled(sampler);
      }
      if (parentBasedModel.getLocalParentNotSampled() != null) {
        Sampler sampler =
            create(parentBasedModel.getLocalParentNotSampled(), spiHelper, closeables);
        builder.setLocalParentNotSampled(sampler);
      }
      return builder.build();
    }

    JaegerRemoteModel jaegerRemoteModel = model.getJaegerRemote();
    if (jaegerRemoteModel != null) {
      model.getAdditionalProperties().put("jaeger_remote", jaegerRemoteModel);
    }

    if (!model.getAdditionalProperties().isEmpty()) {
      Map<String, Object> additionalProperties = model.getAdditionalProperties();
      if (additionalProperties.size() > 1) {
        throw new ConfigurationException(
            "Invalid configuration - multiple samplers exporters set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> exporterKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () -> new IllegalStateException("Missing sampler. This is a programming error."));
      Sampler sampler =
          FileConfigUtil.loadComponent(
              spiHelper, Sampler.class, exporterKeyValue.getKey(), exporterKeyValue.getValue());
      return FileConfigUtil.addAndReturn(closeables, sampler);
    } else {
      throw new ConfigurationException("sampler must be set");
    }
  }
}
