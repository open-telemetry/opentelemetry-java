/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import org.junit.jupiter.api.Test;

class InstrumentSelectorFactoryTest {

  @Test
  void create_Defaults() {
    assertThatThrownBy(
            () ->
                InstrumentSelectorFactory.getInstance()
                    .create(new ViewSelectorModel(), mock(DeclarativeConfigContext.class)))
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
                        .withUnit("ms")
                        .withMeterName("meter-name")
                        .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0")
                        .withMeterVersion("1.0.0"),
                    mock(DeclarativeConfigContext.class)))
        .isEqualTo(
            InstrumentSelector.builder()
                .setName("instrument-name")
                .setType(InstrumentType.COUNTER)
                .setUnit("ms")
                .setMeterName("meter-name")
                .setMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0")
                .setMeterVersion("1.0.0")
                .build());
  }
}
