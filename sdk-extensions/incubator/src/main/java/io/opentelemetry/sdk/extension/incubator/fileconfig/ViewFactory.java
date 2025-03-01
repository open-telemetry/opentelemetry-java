/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.io.Closeable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

final class ViewFactory implements Factory<ViewStreamModel, View> {

  private static final ViewFactory INSTANCE = new ViewFactory();

  private ViewFactory() {}

  static ViewFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public View create(ViewStreamModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    ViewBuilder builder = View.builder();
    if (model.getName() != null) {
      builder.setName(model.getName());
    }
    if (model.getDescription() != null) {
      builder.setDescription(model.getDescription());
    }
    IncludeExcludeModel attributeKeys = model.getAttributeKeys();
    if (attributeKeys != null) {
      addAttributeKeyFilter(builder, attributeKeys.getIncluded(), attributeKeys.getExcluded());
    }
    if (model.getAggregation() != null) {
      builder.setAggregation(
          AggregationFactory.getInstance().create(model.getAggregation(), spiHelper, closeables));
    }
    return builder.build();
  }

  private static void addAttributeKeyFilter(
      ViewBuilder builder, @Nullable List<String> included, @Nullable List<String> excluded) {
    if (included == null && excluded == null) {
      return;
    }
    if (included == null) {
      Set<String> excludedKeys = new HashSet<>(excluded);
      // TODO: set predicate with useful toString implementation
      builder.setAttributeFilter(attributeKey -> !excludedKeys.contains(attributeKey));
      return;
    }
    if (excluded == null) {
      Set<String> includedKeys = new HashSet<>(included);
      builder.setAttributeFilter(includedKeys);
      return;
    }
    Set<String> includedKeys = new HashSet<>(included);
    excluded.forEach(includedKeys::remove);
    builder.setAttributeFilter(includedKeys);
  }
}
