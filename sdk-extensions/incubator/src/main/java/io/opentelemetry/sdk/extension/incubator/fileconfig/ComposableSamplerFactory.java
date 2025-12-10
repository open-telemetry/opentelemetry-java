/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableProbabilitySamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableRuleBasedSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.extension.incubator.trace.samplers.ComposableSampler;
import java.util.Map;

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
    if (model.getAlwaysOn() != null) {
      return ComposableSampler.alwaysOn();
    }
    if (model.getAlwaysOff() != null) {
      return ComposableSampler.alwaysOff();
    }
    ExperimentalComposableProbabilitySamplerModel probability = model.getProbability();
    if (probability != null) {
      Double ratio = probability.getRatio();
      if (ratio == null) {
        ratio = 1.0d;
      }
      return ComposableSampler.probability(ratio);
    }
    ExperimentalComposableRuleBasedSamplerModel ruleBased = model.getRuleBased();
    if (ruleBased != null) {
      // TODO: interpret model
      return ComposableSampler.ruleBasedBuilder().build();
    }
    Map.Entry<String, ?> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "composable sampler");
    return context.loadComponent(ComposableSampler.class, keyValue.getKey(), keyValue.getValue());
  }
}
