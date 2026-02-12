/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvironmentSetterTest {

  @Test
  void set() {
    Map<String, String> carrier = new HashMap<>();
    EnvironmentSetter.INSTANCE.set(carrier, "traceparent", "val1");
    EnvironmentSetter.INSTANCE.set(carrier, "TRACESTATE", "val2");
    EnvironmentSetter.INSTANCE.set(carrier, "Baggage", "val3");

    assertThat(carrier).containsEntry("TRACEPARENT", "val1");
    assertThat(carrier).containsEntry("TRACESTATE", "val2");
    assertThat(carrier).containsEntry("BAGGAGE", "val3");
  }

  @Test
  void set_null() {
    Map<String, String> carrier = new HashMap<>();
    EnvironmentSetter.INSTANCE.set(null, "key", "val");
    EnvironmentSetter.INSTANCE.set(carrier, null, "val");
    EnvironmentSetter.INSTANCE.set(carrier, "key", null);
    assertThat(carrier).isEmpty();
  }

  @Test
  void testToString() {
    assertThat(EnvironmentSetter.INSTANCE.toString()).isEqualTo("EnvironmentSetter");
  }
}
