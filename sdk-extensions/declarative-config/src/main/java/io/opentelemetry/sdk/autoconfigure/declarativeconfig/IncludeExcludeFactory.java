/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import java.util.List;
import java.util.function.Predicate;

final class IncludeExcludeFactory implements Factory<IncludeExcludeModel, Predicate<String>> {

  private static final IncludeExcludeFactory INSTANCE = new IncludeExcludeFactory();

  private IncludeExcludeFactory() {}

  static IncludeExcludeFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Predicate<String> create(IncludeExcludeModel model, DeclarativeConfigContext context) {
    List<String> included = model.getIncluded();
    if (included != null && included.isEmpty()) {
      throw new DeclarativeConfigException("included must not be empty");
    }
    List<String> excluded = model.getExcluded();
    if (excluded != null && excluded.isEmpty()) {
      throw new DeclarativeConfigException("excluded must not be empty");
    }

    return IncludeExcludePredicate.createPatternMatching(included, excluded);
  }
}
