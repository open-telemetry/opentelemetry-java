/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;

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
      builder.setAttributeFilter(
          IncludeExcludeFactory.getInstance().create(attributeKeys, context));
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
