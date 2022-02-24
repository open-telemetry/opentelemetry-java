/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InstrumentSelectorTest {

  @Test
  void invalidArgs() {
    assertThatThrownBy(() -> InstrumentSelector.builder().setInstrumentType(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentType");
    assertThatThrownBy(() -> InstrumentSelector.builder().setInstrumentNameFilter(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentNameFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().setInstrumentNamePattern(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentNamePattern");
    assertThatThrownBy(() -> InstrumentSelector.builder().setInstrumentName(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentName");
    assertThatThrownBy(() -> InstrumentSelector.builder().setInstrumentNameRegex(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentNameRegex");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterSelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterSelector");
  }
}
