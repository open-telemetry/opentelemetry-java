/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableParentThresholdSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;

final class ComposableSamplerFactory
    implements Factory<ExperimentalComposableSamplerModel, ComposableSampler> {

  private static final ComposableSamplerFactory INSTANCE = new ComposableSamplerFactory();

  private ComposableSamplerFactory() {}

  static ComposableSamplerFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ComposableSampler create(
      ExperimentalComposableSamplerModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple samplers.
    ConfigKeyValue samplerKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "composable sampler");

    if (model.getAlwaysOn() != null) {
      return ComposableSampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return ComposableSampler.alwaysOff();
    }
    if (model.getProbability() != null) {
      return createProbabilitySampler(model.getProbability());
    }
    if (model.getRuleBased() != null) {
      return ComposableRuleBasedSamplerFactory.getInstance().create(model.getRuleBased(), context);
    }
    if (model.getParentThreshold() != null) {
      return createParentThresholdSampler(model.getParentThreshold(), context);
    }

    return context.loadComponent(ComposableSampler.class, samplerKeyValue);
  }

  private static ComposableSampler createProbabilitySampler(
      ExperimentalComposableProbabilitySamplerModel probabilityModel) {
    Double ratio = probabilityModel.getRatio();
    if (ratio == null) {
      ratio = 1.0d;
    }
    return ComposableSampler.probability(ratio);
  }

  private static ComposableSampler createParentThresholdSampler(
      ExperimentalComposableParentThresholdSamplerModel parentThresholdModel,
      DeclarativeConfigContext context) {
    ExperimentalComposableSamplerModel rootModel =
        FileConfigUtil.requireNonNull(
            parentThresholdModel.getRoot(), "parent threshold sampler root");
    ComposableSampler rootSampler = INSTANCE.create(rootModel, context);
    return ComposableSampler.parentThreshold(rootSampler);
  }
}
