/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.baggage.Baggage;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ExtendedBaggageTest {

  @Test
  void add() {
    String value =
        ExtendedBaggage.current()
            .set("key", "value")
            .call(() -> Baggage.current().getEntryValue("key"));

    assertThat(value).isEqualTo("value");
  }

  @Test
  void addMap() {
    String value =
        ExtendedBaggage.current()
            .setAll(Collections.singletonMap("key", "value"))
            .call(() -> Baggage.current().getEntryValue("key"));

    assertThat(value).isEqualTo("value");
  }
}
