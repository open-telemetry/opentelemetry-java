/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalProbabilitySamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.CompositeSampler;
import io.opentelemetry.sdk.trace.samplers.ParentBasedSamplerBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;

final class SamplerFactory implements Factory<SamplerModel, Sampler> {

  private static final SamplerFactory INSTANCE = new SamplerFactory();

  private SamplerFactory() {}

  static SamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Sampler create(SamplerModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple samplers.
    ConfigKeyValue samplerKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "sampler");

    if (model.getAlwaysOn() != null) {
      return Sampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return Sampler.alwaysOff();
    }
    if (model.getTraceIdRatioBased() != null) {
      return createTraceIdRatioBasedSampler(model.getTraceIdRatioBased());
    }
    if (model.getParentBased() != null) {
      return createParedBasedSampler(model.getParentBased(), context);
    }
    if (model.getProbabilityDevelopment() != null) {
      return createProbabilitySampler(model.getProbabilityDevelopment());
    }
    if (model.getCompositeDevelopment() != null) {
      return CompositeSampler.wrap(
          ComposableSamplerFactory.getInstance().create(model.getCompositeDevelopment(), context));
    }

    return context.loadComponent(Sampler.class, samplerKeyValue);
  }

  private static Sampler createTraceIdRatioBasedSampler(TraceIdRatioBasedSamplerModel model) {
    Double ratio = model.getRatio();
    if (ratio == null) {
      ratio = 1.0d;
    }
    return Sampler.traceIdRatioBased(ratio);
  }

  private static Sampler createParedBasedSampler(
      ParentBasedSamplerModel parentBasedModel, DeclarativeConfigContext context) {
    Sampler root =
        parentBasedModel.getRoot() == null
            ? Sampler.alwaysOn()
            : INSTANCE.create(parentBasedModel.getRoot(), context);
    ParentBasedSamplerBuilder builder = Sampler.parentBasedBuilder(root);
    if (parentBasedModel.getRemoteParentSampled() != null) {
      Sampler sampler = INSTANCE.create(parentBasedModel.getRemoteParentSampled(), context);
      builder.setRemoteParentSampled(sampler);
    }
    if (parentBasedModel.getRemoteParentNotSampled() != null) {
      Sampler sampler = INSTANCE.create(parentBasedModel.getRemoteParentNotSampled(), context);
      builder.setRemoteParentNotSampled(sampler);
    }
    if (parentBasedModel.getLocalParentSampled() != null) {
      Sampler sampler = INSTANCE.create(parentBasedModel.getLocalParentSampled(), context);
      builder.setLocalParentSampled(sampler);
    }
    if (parentBasedModel.getLocalParentNotSampled() != null) {
      Sampler sampler = INSTANCE.create(parentBasedModel.getLocalParentNotSampled(), context);
      builder.setLocalParentNotSampled(sampler);
    }
    return builder.build();
  }

  private static Sampler createProbabilitySampler(
      ExperimentalProbabilitySamplerModel probabilityModel) {
    Double ratio = probabilityModel.getRatio();
    if (ratio == null) {
      ratio = 1.0d;
    }
    return CompositeSampler.wrap(ComposableSampler.probability(ratio));
  }
}
