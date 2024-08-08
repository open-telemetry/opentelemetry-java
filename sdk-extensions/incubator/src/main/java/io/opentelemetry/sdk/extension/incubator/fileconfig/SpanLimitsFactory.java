/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.trace.SpanLimitsBuilder;
import java.io.Closeable;
import java.util.List;

final class SpanLimitsFactory
    implements Factory<SpanLimitsAndAttributeLimits, io.opentelemetry.sdk.trace.SpanLimits> {

  private static final SpanLimitsFactory INSTANCE = new SpanLimitsFactory();

  private SpanLimitsFactory() {}

  static SpanLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public io.opentelemetry.sdk.trace.SpanLimits create(
      SpanLimitsAndAttributeLimits model, SpiHelper spiHelper, List<Closeable> closeables) {
    SpanLimitsBuilder builder = io.opentelemetry.sdk.trace.SpanLimits.builder();

    AttributeLimits attributeLimitsModel = model.getAttributeLimits();
    if (attributeLimitsModel != null) {
      if (attributeLimitsModel.getAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributes(attributeLimitsModel.getAttributeCountLimit());
      }
      if (attributeLimitsModel.getAttributeValueLengthLimit() != null) {
        builder.setMaxAttributeValueLength(attributeLimitsModel.getAttributeValueLengthLimit());
      }
    }

    SpanLimits spanLimitsModel = model.getSpanLimits();
    if (spanLimitsModel != null) {
      if (spanLimitsModel.getAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributes(spanLimitsModel.getAttributeCountLimit());
      }
      if (spanLimitsModel.getAttributeValueLengthLimit() != null) {
        builder.setMaxAttributeValueLength(spanLimitsModel.getAttributeValueLengthLimit());
      }
      if (spanLimitsModel.getEventCountLimit() != null) {
        builder.setMaxNumberOfEvents(spanLimitsModel.getEventCountLimit());
      }
      if (spanLimitsModel.getLinkCountLimit() != null) {
        builder.setMaxNumberOfLinks(spanLimitsModel.getLinkCountLimit());
      }
      if (spanLimitsModel.getEventAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributesPerEvent(spanLimitsModel.getEventAttributeCountLimit());
      }
      if (spanLimitsModel.getLinkAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributesPerLink(spanLimitsModel.getLinkAttributeCountLimit());
      }
    }

    return builder.build();
  }
}
