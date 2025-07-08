/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBasedSamplerModel;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;

final class SamplerFactory implements Factory<SamplerModel, Sampler> {

  private static final SamplerFactory INSTANCE = new SamplerFactory();

  private SamplerFactory() {}

  static SamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Sampler create(SamplerModel model, DeclarativeConfigContext context) {
    if (model.getAlwaysOn() != null) {
      return Sampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return Sampler.alwaysOff();
    }
    TraceIdRatioBasedSamplerModel traceIdRatioBasedModel = model.getTraceIdRatioBased();
    if (traceIdRatioBasedModel != null) {
      Double ratio = traceIdRatioBasedModel.getRatio();
      if (ratio == null) {
        ratio = 1.0d;
      }
      return Sampler.traceIdRatioBased(ratio);
    }
    ParentBasedSamplerModel parentBasedModel = model.getParentBased();
    if (parentBasedModel != null) {
      Sampler root =
          parentBasedModel.getRoot() == null
              ? Sampler.alwaysOn()
              : create(parentBasedModel.getRoot(), context);
      ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(root);
      if (parentBasedModel.getRemoteParentSampled() != null) {
        Sampler sampler = create(parentBasedModel.getRemoteParentSampled(), context);
        builder.setRemoteParentSampled(sampler);
      }
      if (parentBasedModel.getRemoteParentNotSampled() != null) {
        Sampler sampler = create(parentBasedModel.getRemoteParentNotSampled(), context);
        builder.setRemoteParentNotSampled(sampler);
      }
      if (parentBasedModel.getLocalParentSampled() != null) {
        Sampler sampler = create(parentBasedModel.getLocalParentSampled(), context);
        builder.setLocalParentSampled(sampler);
      }
      if (parentBasedModel.getLocalParentNotSampled() != null) {
        Sampler sampler = create(parentBasedModel.getLocalParentNotSampled(), context);
        builder.setLocalParentNotSampled(sampler);
      }
      return builder.build();
    }

    model.getAdditionalProperties().compute("jaeger_remote", (k, v) -> model.getJaegerRemote());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "sampler");
    return context.loadComponent(Sampler.class, keyValue.getKey(), keyValue.getValue());
  }
}
