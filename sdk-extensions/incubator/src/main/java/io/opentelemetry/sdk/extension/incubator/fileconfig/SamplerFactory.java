/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.JaegerRemote;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBased;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class SamplerFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler, Sampler> {

  private static final SamplerFactory INSTANCE = new SamplerFactory();

  private SamplerFactory() {}

  static SamplerFactory getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("NullAway") // Override superclass non-null response
  @Override
  @Nullable
  public Sampler create(
      @Nullable io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return Sampler.parentBased(Sampler.alwaysOn());
    }

    if (model.getAlwaysOn() != null) {
      return Sampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return Sampler.alwaysOff();
    }
    TraceIdRatioBased traceIdRatioBasedModel = model.getTraceIdRatioBased();
    if (traceIdRatioBasedModel != null) {
      Double ratio = traceIdRatioBasedModel.getRatio();
      if (ratio == null) {
        ratio = 1.0d;
      }
      return Sampler.traceIdRatioBased(ratio);
    }
    ParentBased parentBasedModel = model.getParentBased();
    if (parentBasedModel != null) {
      Sampler root =
          parentBasedModel.getRoot() == null
              ? Sampler.alwaysOn()
              : create(parentBasedModel.getRoot(), spiHelper, closeables);
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(root);
      if (parentBasedModel.getRemoteParentSampled() != null) {
        Sampler sampler =
            requireNonNull(
                create(parentBasedModel.getRemoteParentSampled(), spiHelper, closeables),
                "sampler required for remote parent sampled sampler");
        builder.setRemoteParentSampled(sampler);
      }
      if (parentBasedModel.getRemoteParentNotSampled() != null) {
        Sampler sampler =
            requireNonNull(
                create(parentBasedModel.getRemoteParentNotSampled(), spiHelper, closeables),
                "sampler required for remote parent not sampled sampler");
        builder.setRemoteParentNotSampled(sampler);
      }
      if (parentBasedModel.getLocalParentSampled() != null) {
        Sampler sampler =
            requireNonNull(
                create(parentBasedModel.getLocalParentSampled(), spiHelper, closeables),
                "sampler required for local parent sampled sampler");
        builder.setLocalParentSampled(sampler);
      }
      if (parentBasedModel.getLocalParentNotSampled() != null) {
        Sampler sampler =
            requireNonNull(
                create(parentBasedModel.getLocalParentNotSampled(), spiHelper, closeables),
                "sampler required for local parent not sampled sampler");
        builder.setLocalParentNotSampled(sampler);
      }
      return builder.build();
    }

    JaegerRemote jaegerRemoteModel = model.getJaegerRemote();
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
    }

    return null;
  }

  static Sampler requireNonNull(@Nullable Sampler sampler, String errorMessage) {
    if (sampler == null) {
      throw new ConfigurationException(errorMessage);
    }
    return sampler;
  }
}
