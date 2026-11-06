/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableParentThresholdSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableSamplerModel;
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
    ExperimentalComposableRuleBasedSamplerModel ruleBasedModel = model.getRuleBased();
    if (ruleBasedModel != null) {
      return context.withConfigPath(
          "rule_based",
          () -> ComposableRuleBasedSamplerFactory.getInstance().create(ruleBasedModel, context));
    }
    ExperimentalComposableParentThresholdSamplerModel parentThresholdModel =
        model.getParentThreshold();
    if (parentThresholdModel != null) {
      return context.withConfigPath(
          "parent_threshold", () -> createParentThresholdSampler(parentThresholdModel, context));
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
        FileConfigUtil.requireNonNull(context, parentThresholdModel.getRoot(), "root");
    ComposableSampler rootSampler = INSTANCE.create(rootModel, context);
    return ComposableSampler.parentThreshold(rootSampler);
  }
}
