/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class InstrumentSelectorFactoryTest {

  @Test
  void create_Defaults() {
    assertThatThrownBy(
            () ->
                InstrumentSelectorFactory.getInstance()
                    .create(
                        new ViewSelectorModel(), mock(SpiHelper.class), Collections.emptyList()))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage("Invalid selector");
  }

  @Test
  void create() {
    assertThat(
            InstrumentSelectorFactory.getInstance()
                .create(
                    new ViewSelectorModel()
                        .withInstrumentName("instrument-name")
                        .withInstrumentType(ViewSelectorModel.InstrumentType.COUNTER)
                        .withMeterName("meter-name")
                        .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0")
                        .withMeterVersion("1.0.0"),
                    mock(SpiHelper.class),
                    Collections.emptyList()))
        .isEqualTo(
            InstrumentSelector.builder()
                .setName("instrument-name")
                .setType(InstrumentType.COUNTER)
                .setMeterName("meter-name")
                .setMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0")
                .setMeterVersion("1.0.0")
                .build());
  }
}
