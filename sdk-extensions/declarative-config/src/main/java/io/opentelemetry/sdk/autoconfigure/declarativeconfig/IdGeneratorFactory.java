/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IdGeneratorModel;
import io.opentelemetry.sdk.trace.IdGenerator;

final class IdGeneratorFactory implements Factory<IdGeneratorModel, IdGenerator> {

  private static final IdGeneratorFactory INSTANCE = new IdGeneratorFactory();

  private IdGeneratorFactory() {}

  static IdGeneratorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public IdGenerator create(IdGeneratorModel model, DeclarativeConfigContext context) {
    // We don't use the variable till later but call validate first to confirm there are not
    // multiple IdGenerators.
    ConfigKeyValue processorKeyValue =
        FileConfigUtil.validateSingleKeyValue(context, model, "id generator");

    if (model.getRandom() != null) {
      return IdGenerator.random();
    }

    return context.loadComponent(IdGenerator.class, processorKeyValue);
  }
}
