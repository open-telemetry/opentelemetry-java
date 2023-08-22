/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.io.Closeable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

final class ViewFactory implements Factory<Stream, View> {

  private static final ViewFactory INSTANCE = new ViewFactory();

  private ViewFactory() {}

  static ViewFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public View create(@Nullable Stream model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      throw new ConfigurationException("stream must not be null");
    }

    ViewBuilder builder = View.builder();
    if (model.getName() != null) {
      builder.setName(model.getName());
    }
    if (model.getDescription() != null) {
      builder.setDescription(model.getDescription());
    }
    if (model.getAttributeKeys() != null) {
      builder.setAttributeFilter(new AttributeKeyFilter(new HashSet<>(model.getAttributeKeys())));
    }
    if (model.getAggregation() != null) {
      builder.setAggregation(
          AggregationFactory.getInstance().create(model.getAggregation(), spiHelper, closeables));
    }
    return builder.build();
  }

  private static class AttributeKeyFilter implements Predicate<String> {

    private final Set<String> allowedKeys;

    private AttributeKeyFilter(Set<String> allowedKeys) {
      this.allowedKeys = allowedKeys;
    }

    @Override
    public boolean test(String s) {
      return false;
    }

    @Override
    public String toString() {
      return "AttributeKeyFilter{allowedKeys=" + allowedKeys + "}";
    }
  }
}
