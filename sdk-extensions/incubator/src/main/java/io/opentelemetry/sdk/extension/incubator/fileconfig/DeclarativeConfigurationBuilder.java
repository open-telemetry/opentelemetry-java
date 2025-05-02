/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.function.Function;

/** Builder for the declarative configuration. */
public class DeclarativeConfigurationBuilder implements DeclarativeConfigurationCustomizer {
  private Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel>
      modelCustomizer = Function.identity();

  @Override
  public void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer) {
    modelCustomizer = mergeCustomizer(modelCustomizer, customizer);
  }

  private static <I, O1, O2> Function<I, O2> mergeCustomizer(
      Function<? super I, ? extends O1> first, Function<? super O1, ? extends O2> second) {
    return (I configured) -> {
      O1 firstResult = first.apply(configured);
      return second.apply(firstResult);
    };
  }

  /** Customize the configuration model. */
  public OpenTelemetryConfigurationModel customizeModel(
      OpenTelemetryConfigurationModel configurationModel) {
    return modelCustomizer.apply(configurationModel);
  }
}
