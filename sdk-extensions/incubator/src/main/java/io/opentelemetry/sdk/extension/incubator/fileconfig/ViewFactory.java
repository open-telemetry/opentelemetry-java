/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.util.List;

final class ViewFactory implements Factory<ViewStreamModel, View> {

  private static final ViewFactory INSTANCE = new ViewFactory();

  private ViewFactory() {}

  static ViewFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public View create(ViewStreamModel model, DeclarativeConfigContext context) {
    ViewBuilder builder = View.builder();
    if (model.getName() != null) {
      builder.setName(model.getName());
    }
    if (model.getDescription() != null) {
      builder.setDescription(model.getDescription());
    }
    IncludeExcludeModel attributeKeys = model.getAttributeKeys();
    if (attributeKeys != null) {
      List<String> included = attributeKeys.getIncluded();
      List<String> excluded = attributeKeys.getExcluded();
      if (included != null || excluded != null) {
        builder.setAttributeFilter(IncludeExcludePredicate.createExactMatching(included, excluded));
      }
    }
    if (model.getAggregation() != null) {
      builder.setAggregation(
          AggregationFactory.getInstance().create(model.getAggregation(), context));
    }
    if (model.getAggregationCardinalityLimit() != null) {
      builder.setCardinalityLimit(model.getAggregationCardinalityLimit());
    }
    return builder.build();
  }
}
