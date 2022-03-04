/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class InstrumentSelectorTest {

  @Test
  void invalidArgs() {
    assertThatThrownBy(() -> InstrumentSelector.builder().setType(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentType");
    assertThatThrownBy(() -> InstrumentSelector.builder().setName((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("nameFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().setName((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterSelector(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterSelector");
  }
}
