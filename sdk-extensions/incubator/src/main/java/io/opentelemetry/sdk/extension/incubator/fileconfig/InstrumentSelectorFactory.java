/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentSelectorBuilder;
import io.opentelemetry.sdk.metrics.InstrumentType;

final class InstrumentSelectorFactory implements Factory<ViewSelectorModel, InstrumentSelector> {

  private static final InstrumentSelectorFactory INSTANCE = new InstrumentSelectorFactory();

  private InstrumentSelectorFactory() {}

  static InstrumentSelectorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public InstrumentSelector create(ViewSelectorModel model, DeclarativeConfigContext context) {
    InstrumentSelectorBuilder builder = InstrumentSelector.builder();
    if (model.getInstrumentName() != null) {
      builder.setName(model.getInstrumentName());
    }
    if (model.getInstrumentType() != null) {
      InstrumentType instrumentType;
      try {
        instrumentType = InstrumentType.valueOf(model.getInstrumentType().name());
      } catch (IllegalArgumentException e) {
        throw new DeclarativeConfigException(
            "Unrecognized instrument type: " + model.getInstrumentType(), e);
      }
      builder.setType(instrumentType);
    }
    if (model.getUnit() != null) {
      builder.setUnit(model.getUnit());
    }
    if (model.getMeterName() != null) {
      builder.setMeterName(model.getMeterName());
    }
    if (model.getMeterSchemaUrl() != null) {
      builder.setMeterSchemaUrl(model.getMeterSchemaUrl());
    }
    if (model.getMeterVersion() != null) {
      builder.setMeterVersion(model.getMeterVersion());
    }

    try {
      return builder.build();
    } catch (IllegalArgumentException e) {
      throw new DeclarativeConfigException("Invalid selector", e);
    }
  }
}
