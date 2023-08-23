/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.trace.SpanLimitsBuilder;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class SpanLimitsFactory
    implements Factory<SpanLimits, io.opentelemetry.sdk.trace.SpanLimits> {

  private static final SpanLimitsFactory INSTANCE = new SpanLimitsFactory();

  private SpanLimitsFactory() {}

  static SpanLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public io.opentelemetry.sdk.trace.SpanLimits create(
      @Nullable SpanLimits model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return io.opentelemetry.sdk.trace.SpanLimits.getDefault();
    }

    SpanLimitsBuilder builder = io.opentelemetry.sdk.trace.SpanLimits.builder();
    if (model.getAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributes(model.getAttributeCountLimit());
    }
    if (model.getAttributeValueLengthLimit() != null) {
      builder.setMaxAttributeValueLength(model.getAttributeValueLengthLimit());
    }
    if (model.getEventCountLimit() != null) {
      builder.setMaxNumberOfEvents(model.getEventCountLimit());
    }
    if (model.getLinkCountLimit() != null) {
      builder.setMaxNumberOfLinks(model.getLinkCountLimit());
    }
    if (model.getEventAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributesPerEvent(model.getEventAttributeCountLimit());
    }
    if (model.getLinkAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributesPerLink(model.getLinkAttributeCountLimit());
    }

    return builder.build();
  }
}
