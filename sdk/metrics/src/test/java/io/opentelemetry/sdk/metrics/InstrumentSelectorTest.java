/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InstrumentSelectorTest {

  @Test
  void invalidArgs() {
    assertThatThrownBy(() -> InstrumentSelector.builder().setType(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentType");
    assertThatThrownBy(() -> InstrumentSelector.builder().setName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
    assertThatThrownBy(() -> InstrumentSelector.builder().setUnit(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterName");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterVersion(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterVersion");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterSchemaUrl(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterSchemaUrl");
    assertThatThrownBy(() -> InstrumentSelector.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument selector must contain selection criteria");
  }

  @Test
  void instrumentType() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            .setType(InstrumentType.COUNTER)
            .setType(InstrumentType.OBSERVABLE_COUNTER)
            .build();
    assertThat(selector.getInstrumentType()).isEqualTo(InstrumentType.OBSERVABLE_COUNTER);
  }

  @Test
  void instrumentName() {
    InstrumentSelector selector =
        InstrumentSelector.builder().setName("foo").setName("bar").build();
    assertThat(selector.getInstrumentName()).isEqualTo("bar");
  }

  @Test
  void instrumentUnit() {
    InstrumentSelector selector = InstrumentSelector.builder().setName("ms").setName("s").build();
    assertThat(selector.getInstrumentName()).isEqualTo("s");
  }

  @Test
  void meterName() {
    InstrumentSelector selector =
        InstrumentSelector.builder().setMeterName("example1").setMeterName("example2").build();
    assertThat(selector.getMeterName()).isEqualTo("example2");
  }

  @Test
  void meterVersion() {
    InstrumentSelector selector =
        InstrumentSelector.builder().setMeterVersion("1.2.3").setMeterVersion("4.5.6").build();
    assertThat(selector.getMeterVersion()).isEqualTo("4.5.6");
  }

  @Test
  void meterSchemaUrl() {
    InstrumentSelector selector =
        InstrumentSelector.builder()
            .setMeterSchemaUrl("http://foo.com")
            .setMeterSchemaUrl("http://bar.com")
            .build();
    assertThat(selector.getMeterSchemaUrl()).isEqualTo("http://bar.com");
  }

  @Test
  void stringRepresentation() {
    assertThat(InstrumentSelector.builder().setName("name").build().toString())
        .isEqualTo("InstrumentSelector{instrumentName=name}");
    assertThat(
            InstrumentSelector.builder()
                .setType(InstrumentType.COUNTER)
                .setName("name")
                .setUnit("unit")
                .setMeterName("meter")
                .setMeterVersion("version")
                .setMeterSchemaUrl("http://url.com")
                .build()
                .toString())
        .isEqualTo(
            "InstrumentSelector{"
                + "instrumentType=COUNTER, "
                + "instrumentName=name, "
                + "instrumentUnit=unit, "
                + "meterName=meter, "
                + "meterVersion=version, "
                + "meterSchemaUrl=http://url.com"
                + "}");
  }
}
