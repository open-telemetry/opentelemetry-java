/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentSelectorBuilder;
import io.opentelemetry.sdk.metrics.InstrumentType;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class InstrumentSelectorFactory implements Factory<Selector, InstrumentSelector> {

  private static final InstrumentSelectorFactory INSTANCE = new InstrumentSelectorFactory();

  private InstrumentSelectorFactory() {}

  static InstrumentSelectorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public InstrumentSelector create(
      @Nullable Selector model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      throw new ConfigurationException("selector must not be null");
    }

    InstrumentSelectorBuilder builder = InstrumentSelector.builder();
    if (model.getInstrumentName() != null) {
      builder.setName(model.getInstrumentName());
    }
    if (model.getInstrumentType() != null) {
      InstrumentType instrumentType;
      try {
        instrumentType = InstrumentType.valueOf(model.getInstrumentType().name());
      } catch (IllegalArgumentException e) {
        throw new ConfigurationException(
            "Unrecognized instrument type: " + model.getInstrumentType(), e);
      }
      builder.setType(instrumentType);
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
      throw new ConfigurationException("Invalid selector", e);
    }
  }
}
